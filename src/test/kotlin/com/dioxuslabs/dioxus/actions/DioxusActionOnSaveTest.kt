package com.dioxuslabs.dioxus.actions

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptions
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rust.lang.RsFileType
import java.io.File

class DioxusActionOnSaveTest : BasePlatformTestCase() {

    private lateinit var tempDir: File
    private lateinit var rustFile: VirtualFile
    private lateinit var psiFile: PsiFile
    private lateinit var document: Document

    override fun setUp() {
        super.setUp()
        
        // Create a temporary directory for test files
        tempDir = FileUtil.createTempDirectory("dioxus-test", null)
        
        // Create a Rust file with unformatted RSX code
        val unformattedCode = """
            fn main() {
                let app = rsx! {
                    div {
                    h1 { "Hello, world!" }
                        p { "This is a test." }
                    }
                };
            }
        """.trimIndent()

        // Write the content to a file
        val file = File(tempDir, "test.rs")
        FileUtil.writeToFile(file, unformattedCode)

        // Refresh the VFS to see the new file
        rustFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)!!

        // Get the PSI file and document
        psiFile = myFixture.psiManager.findFile(rustFile)!!
        document = FileDocumentManager.getInstance().getDocument(rustFile)!!

        // Enable "Format on Save" option
        val formatOnSaveOptions = FormatOnSaveOptions.getInstance(project)
        formatOnSaveOptions.isRunOnSaveEnabled = true
    }

    override fun tearDown() {
        // Clean up
        FileUtil.delete(tempDir)
        super.tearDown()
    }

    fun testActionOnSave() {
        // Expected formatted code
        val expectedFormattedCode = """
            fn main() {
                let app = rsx! {
                    div {
                        h1 { "Hello, world!" }
                        p { "This is a test." }
                    }
                };
            }
        """.trimIndent()
        
        // Create and run the action
        val action = DioxusCheckOnSaveAction()
        action.processDocuments(project, arrayOf(document))
        
        // Wait for the background task to complete
        Thread.sleep(1000)
        
        // Refresh the document from disk
        FileDocumentManager.getInstance().reloadFiles(rustFile)
        
        // Verify that the code is formatted correctly
        val actualFormattedCode = document.text.trim()
        assertEquals(expectedFormattedCode, actualFormattedCode)
    }
}