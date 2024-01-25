/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.actions

import com.google.gson.Gson
import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TestDialog
import okhttp3.mockwebserver.MockResponse
import org.intellij.lang.annotations.Language
import org.rust.*
import org.rust.cargo.RsWithToolchainTestBase
import org.rust.cargo.project.workspace.CargoWorkspace.Edition
import org.rust.cargo.toolchain.tools.rustc

class ShareInPlaygroundActionTest : RsWithToolchainTestBase() {

    private val mockServerFixture: MockServerFixture = MockServerFixture()

    override fun setUp() {
        super.setUp()
        mockServerFixture.setUp()
    }

    override fun tearDown() {
        mockServerFixture.tearDown()
        super.tearDown()
    }

    fun `test share whole file`() = doTest(Edition.EDITION_2018, """
        fn main() {
            println!("Hello!");/*caret*/
        }
    """, """
        fn main() {
            println!("Hello!");
        }
    """)

    fun `test share selected code`() = doTest(Edition.EDITION_2018, """
        <selection>fn main() {
            println!("Hello!");
        }</selection>
        fn foo() {}
    """, """
        fn main() {
            println!("Hello!");
        }
    """)

    fun `test correct edition`() = doTest(Edition.EDITION_2015, """
        fn main() {
            println!("Hello!");/*caret*/
        }
    """, """
        fn main() {
            println!("Hello!");
        }
    """)

    fun `test failed request`() {
        val notificationContent = launchAction(Edition.EDITION_2018, """
            fn main() {
                println!("Hello!");/*caret*/
            }
        """, TestDialog.OK) {
            MockResponse().setResponseCode(404)
        }
        assertEquals(notificationContent, RsBundle.message("action.Rust.ShareInPlayground.notification.error"))
    }

    fun `test do not perform network request without user confirmation`() {
        launchAction(Edition.EDITION_2018, """
            fn main() {
                println!("Hello!");/*caret*/
            }
        """, TestDialog.NO) {
            error("Unexpected network request without user confirmation")
        }
    }

    private fun doTest(
        edition: Edition,
        @Language("Rust") code: String,
        @Language("Rust") codeToShare: String
    ) {
        configure(code, edition)
        val channel = rustupFixture.toolchain?.rustc()?.queryVersion()?.channel?.channel
            ?: error("Can't get toolchain channel")
        doTest(project, mockServerFixture, codeToShare, edition, channel, ::actionLauncher)
    }

    private fun launchAction(
        edition: Edition,
        @Language("Rust") code: String,
        testDialog: TestDialog,
        handler: ResponseHandler
    ): String? {
        configure(code, edition)
        return launchAction(project, mockServerFixture, testDialog, ::actionLauncher, handler)
    }

    private fun configure(@Language("Rust") code: String, edition: Edition) {
        val testProject = fileTree {
            toml("Cargo.toml", """
                [package]
                name = "hello"
                version = "0.1.0"
                authors = []
                edition = "${edition.presentation}"
            """)
            dir("src") {
                rust("main.rs", code)
            }
        }.create()

        val file = testProject.psiFile(testProject.fileWithCaretOrSelection)
        myFixture.configureFromExistingVirtualFile(file.virtualFile)
    }

    private fun actionLauncher() {
        myFixture.launchAnAction("Rust.ShareInPlayground")
    }

    companion object {
        private const val MOCK_GIST_ID = "1234567890"
        private val HREF_REGEX = """href="(.*)"""".toRegex()

        fun doTest(
            project: Project,
            mockServerFixture: MockServerFixture,
            @Language("Rust") codeToShare: String,
            expectedEdition: Edition,
            expectedChannel: String,
            actionLauncher: () -> Unit
        ) {
            var requestCode: String? = null
            val notificationContent = launchAction(project, mockServerFixture, TestDialog.OK, actionLauncher) {
                val gson = Gson()
                requestCode = gson.fromJson(it.body.inputStream().reader(), ShareInPlaygroundAction.PlaygroundCode::class.java).code

                val response = gson.toJson(mapOf(
                    "id" to MOCK_GIST_ID,
                    "url" to "https://gist.github.com/$MOCK_GIST_ID",
                    "code" to requestCode
                ))

                MockResponse()
                    .setBody(response)
                    .addHeader("Content-Type", "application/json")
            }
            check(notificationContent != null) { "Notification is not shown" }

            assertEquals(codeToShare.trimIndent(), requestCode)

            val url = HREF_REGEX.find(notificationContent)?.groups?.get(1)?.value
                ?: error("Notification content doesn't contain hyperlink")
            assertEquals("https://play.rust-lang.org/?version=$expectedChannel&edition=${expectedEdition.presentation}&gist=$MOCK_GIST_ID", url)
        }

        private fun launchAction(
            project: Project,
            mockServerFixture: MockServerFixture,
            testDialog: TestDialog,
            actionLauncher: () -> Unit,
            handler: ResponseHandler
        ): String? {
            mockServerFixture.withHandler(handler)

            var notificationContent: String? = null
            project.messageBus.connect(mockServerFixture.testRootDisposable).subscribe(Notifications.TOPIC, object : Notifications {
                override fun notify(notification: Notification) {
                    notificationContent = notification.content
                }
            })

            withTestDialog(testDialog) {
                withMockPlaygroundHost(mockServerFixture.baseUrl) {
                    actionLauncher()
                }
            }

            return notificationContent
        }
    }
}
