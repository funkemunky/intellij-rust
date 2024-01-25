/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.cargo.toolchain.tools

import com.intellij.execution.configurations.GeneralCommandLine
import org.rust.cargo.toolchain.RsToolchainBase
import org.rust.openapiext.GeneralCommandLine
import org.rust.openapiext.withWorkDirectory
import java.nio.file.Path

abstract class RsTool(toolName: String, val toolchain: RsToolchainBase) {
    open val executable: Path = toolchain.pathToExecutable(toolName)

    protected fun createBaseCommandLine(
        vararg parameters: String,
        workingDirectory: Path? = null,
        environment: Map<String, String> = emptyMap()
    ): GeneralCommandLine = createBaseCommandLine(
        parameters.toList(),
        workingDirectory = workingDirectory,
        environment = environment
    )

    protected open fun createBaseCommandLine(
        parameters: List<String>,
        workingDirectory: Path? = null,
        environment: Map<String, String> = emptyMap()
    ): GeneralCommandLine = GeneralCommandLine(executable)
        .withWorkDirectory(workingDirectory)
        .withParameters(parameters)
        .withEnvironment(environment)
        .withCharset(Charsets.UTF_8)
        .also { toolchain.patchCommandLine(it, withSudo = false) }
}

abstract class CargoBinary(binaryName: String, toolchain: RsToolchainBase) : RsTool(binaryName, toolchain) {
    override val executable: Path = toolchain.pathToCargoExecutable(binaryName)
}

abstract class RustupComponent(componentName: String, toolchain: RsToolchainBase) : RsTool(componentName, toolchain)
