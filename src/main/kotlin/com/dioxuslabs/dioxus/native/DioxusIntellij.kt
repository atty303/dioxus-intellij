package com.dioxuslabs.dioxus.native

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

object DioxusIntellij {
    interface CLibrary : Library {
        companion object {
            val INSTANCE = Native.load("dioxus_intellij", CLibrary::class.java)!!
        }

        fun format(doc: String): Pointer
    }

    fun format(doc: String): String {
        val r0 = CLibrary.INSTANCE.format(doc)
        val r = r0.getString(0)
        Native.free(Pointer.nativeValue(r0))
        return r
    }
}
