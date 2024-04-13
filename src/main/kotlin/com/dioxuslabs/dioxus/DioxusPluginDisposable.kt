package com.dioxuslabs.dioxus

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class DioxusPluginDisposable : Disposable {
    companion object {
        fun getInstance(project: Project): DioxusPluginDisposable {
            return project.getService(DioxusPluginDisposable::class.java)
        }
    }

    override fun dispose() {
    }
}