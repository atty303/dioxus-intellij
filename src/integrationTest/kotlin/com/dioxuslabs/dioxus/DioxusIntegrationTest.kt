package com.dioxuslabs.dioxus

import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.ui.components.common.codeEditor
import com.intellij.driver.sdk.ui.components.common.editor
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.invokeOpenFileAction
import com.intellij.driver.sdk.ui.components.common.popups.searchEverywherePopup
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.framework.detection.FileContentPattern.fileContent
import com.intellij.ide.starter.ci.CIServer
import com.intellij.ide.starter.ci.NoCIServer
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.io.File
import java.nio.file.Files
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
            PluginConfigurator(this).installPluginFromPluginManager("com.jetbrains.rust", "251.23774.463")
            PluginConfigurator(this).installPluginFromFolder(File(pathToPlugin))
        }.runIdeWithDriver().useDriverAndCloseIde {
            waitForIndicators(5.minutes)
//            Thread.sleep(30.minutes.inWholeMilliseconds)
            ideFrame {
                invokeAction("SearchEverywhere")
                searchEverywherePopup {
                    searchAndChooseFirst("main.rs")
                }
                codeEditor {
//                    goToPosition(0, 0)
//                    keyboard { space() }
                    keyboard { space() }
                    invokeAction("SaveAll")
                    waitForIndicators(1.minutes)
                }

                val fileContent = Files.readString(Path("src/integrationTest/resources/rust-project/src/main.rs"))
                System.err.println("取得したファイルの内容:\n$fileContent")
            }
        }
    }
}
