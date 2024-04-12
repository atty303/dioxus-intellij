package com.dioxuslabs.dioxus.actions

import com.dioxuslabs.dioxus.native.DioxusIntellij
import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptions
import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

class DioxusCheckOnSaveAction : ActionsOnSaveFileDocumentManagerListener.ActionOnSave() {
    override fun isEnabledForProject(project: Project): Boolean {
        return FormatOnSaveOptions.getInstance(project).isRunOnSaveEnabled
    }

    override fun processDocuments(project: Project, documents: Array<Document>) {
        documents.toList().forEach { doc ->
            val formatted = DioxusIntellij.format(doc.text)
            WriteCommandAction.writeCommandAction(project).withName("Dioxus Format").run<Exception> {
                doc.setText(formatted)
//            FileDocumentManager.getInstance().saveDocument(doc)
            }
        }
    }
}