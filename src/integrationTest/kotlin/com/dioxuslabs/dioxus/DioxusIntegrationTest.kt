package com.dioxuslabs.dioxus

import com.dioxuslabs.dioxus.actions.DioxusCheckOnSaveAction
import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.ui.components.common.codeEditor
import com.intellij.driver.sdk.ui.components.common.editor
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.dialog
import com.intellij.driver.sdk.ui.components.elements.tree

import com.intellij.driver.sdk.waitForIndicators
import com.intellij.framework.detection.FileContentPattern.fileContent
import com.intellij.ide.starter.ci.CIServer
import com.intellij.ide.starter.ci.NoCIServer
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.GitHubProject
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.report.sarif.driver
import com.intellij.ide.starter.runner.Starter
import com.intellij.openapi.actionSystem.ex.ActionUtil.invokeAction
import com.intellij.openapi.application.ReadAction

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes

class PluginTest {
    init {
        di = DI {
            extend(di)
            bindSingleton<CIServer>(overrides = true) {
                object : CIServer by NoCIServer {
                    override fun reportTestFailure(
                        testName: String,
                        message: String,
                        details: String,
                        linkToLogs: String?
                    ) {
                        fail { "$testName fails: $message. \n$details" }
                    }
                }
            }
        }
    }
    @Test
    fun simpleTest() {
        val result = Starter.newContext(
            "testExample",
            TestCase(
                IdeProductProvider.IU,
                LocalProjectInfo(Path("src/integrationTest/resources/rust-project"))
            ).withVersion("2025.1")
        ).apply {
            val pathToPlugin = System.getProperty("path.to.build.plugin")
            PluginConfigurator(this).installPluginFromFolder(File(pathToPlugin))
        }.runIdeWithDriver().useDriverAndCloseIde {
            waitForIndicators(5.minutes)
//            Thread.sleep(30.minutes.inWholeMilliseconds)
            ideFrame {
                invokeAction("ShowSettings")

                editor("src/main.rs")
                codeEditor {

                    invokeAction("SaveAll")
                    waitForIndicators(1.minutes)
                }

                val fileContent = Files.readString(Path("src/integrationTest/resources/rust-project/src/main.rs"))
                println("取得したファイルの内容:\n$fileContent")
            }
        }
    }
}

class BackendService {
    fun getFileContent(projectBasePath: String, relativePath: String): String {
        val filePath = Paths.get(projectBasePath, relativePath).toString()
        // ReadAction を使用して VFS から安全にファイルを取得
        return ReadAction.compute<String, Throwable> {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
                ?: throw IllegalStateException("File not found: $filePath")
            // VfsUtil を使ってファイルの内容を読み込む
            VfsUtil.loadText(virtualFile)
        }
    }
}
