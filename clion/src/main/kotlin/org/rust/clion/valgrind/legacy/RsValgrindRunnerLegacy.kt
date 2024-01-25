/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.clion.valgrind.legacy

import com.intellij.execution.configurations.RunProfile
import com.intellij.openapi.util.NlsContexts
import org.rust.RsBundle
import org.rust.cargo.runconfig.buildtool.CargoBuildManager.isBuildToolWindowAvailable
import org.rust.cargo.runconfig.command.CargoCommandConfiguration
import org.rust.cargo.runconfig.legacy.RsAsyncRunner
import org.rust.clion.valgrind.RsValgrindConfigurationExtension
import org.rust.clion.valgrind.ValgrindExecutor

@NlsContexts.DialogTitle
private val ERROR_MESSAGE_TITLE: String = RsBundle.message("dialog.title.unable.to.run.valgrind")

/**
 * This runner is used if [isBuildToolWindowAvailable] is false.
 */
class RsValgrindRunnerLegacy : RsAsyncRunner(ValgrindExecutor.EXECUTOR_ID, ERROR_MESSAGE_TITLE) {
    override fun getRunnerId(): String = RUNNER_ID

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        if (profile !is CargoCommandConfiguration) return false
        return RsValgrindConfigurationExtension.isEnabledFor(profile) && super.canRun(executorId, profile)
    }

    companion object {
        const val RUNNER_ID: String = "RsValgrindRunnerLegacy"
    }
}
