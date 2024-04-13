package com.dioxuslabs.dioxus.actions

import com.dioxuslabs.dioxus.native.DioxusIntellij
import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptions
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutput
import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Disposer
import org.rust.cargo.project.settings.RustfmtProjectSettingsService
import org.rust.cargo.project.settings.rustSettings
import org.rust.cargo.project.settings.rustfmtSettings
import org.rust.cargo.runconfig.CargoCommandRunner
import org.rust.cargo.toolchain.CargoCommandLine
import org.rust.cargo.toolchain.tools.Cargo
import org.rust.lang.RsFileType
import org.rust.lang.core.psi.rustPsiManager
import org.rust.openapiext.execute
import org.rust.openapiext.pathAsPath
import org.rust.openapiext.toPsiFile

private data class IndentOption(val useTabs: Boolean, val indentSize: Int)

private fun loadIndentOptions(project: Project, path: String): IndentOption {
    val style = CodeStyle.getSettings(project)
    val default = IndentOption(
        style.OTHER_INDENT_OPTIONS.USE_TAB_CHARACTER,
        style.OTHER_INDENT_OPTIONS.INDENT_SIZE
    )

    val toolchain = project.rustSettings.toolchain
    if (toolchain == null || !project.rustfmtSettings.useRustfmt) {
        return default
    }

    val projectDir = project.guessProjectDir()?.pathAsPath ?: return default

    val cmd = CargoCommandLine("fmt", projectDir, listOf("--", "--print-config", "current", path))
    val runner: (CapturingProcessHandler) -> ProcessOutput = { p ->
        p.runProcess()
    }
    val disposable = Disposer.newDisposable()
    val r = Cargo(toolchain).toGeneralCommandLine(project, cmd).execute(disposable, runner = runner)
    disposable.dispose()

    val out = r.ok() ?: return default

    val lines = out.stdoutLines
    val useTabs = lines.find { it.startsWith("hard_tabs = ") }?.substringAfter("hard_tabs = ")?.trim()?.toBoolean() ?: default.useTabs
    val indentSize = lines.find { it.startsWith("tab_spaces = ") }?.substringAfter("tab_spaces = ")?.trim()?.toInt() ?: default.indentSize

    return IndentOption(useTabs, indentSize)
}

class DioxusCheckOnSaveAction : ActionsOnSaveFileDocumentManagerListener.ActionOnSave() {
    override fun isEnabledForProject(project: Project): Boolean {
        return FormatOnSaveOptions.getInstance(project).isRunOnSaveEnabled
    }

    override fun processDocuments(project: Project, documents: Array<Document>) {
        documents.mapNotNull { it.toPsiFile(project) }.filter { it.fileType is RsFileType }.forEach { file ->
            val indentOption = loadIndentOptions(project, file.virtualFile.path)
            val formatted = DioxusIntellij.format(file.text, indentOption.useTabs, indentOption.indentSize, false)
            WriteCommandAction.writeCommandAction(project).withName("Format RSX").run<Exception> {
                file.fileDocument.setText(formatted)
            }
        }
    }
}