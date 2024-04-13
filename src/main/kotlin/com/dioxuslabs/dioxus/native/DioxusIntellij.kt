package com.dioxuslabs.dioxus.native

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

object DioxusIntellij {
    interface CLibrary : Library {
        companion object {
            val INSTANCE = Native.load("dioxus_intellij", CLibrary::class.java)!!
        }

        fun format(contents: String, useTabs: Int, indentSize: Int, splitLineAttributes: Int): Pointer
    }

    fun format(contents: String, useTabs: Boolean, indentSize: Int, splitLineAttributes: Boolean): String {
        val p = CLibrary.INSTANCE.format(contents, if (useTabs) 1 else 0, indentSize, if (splitLineAttributes) 1 else 0)
        if (p == Pointer.NULL) {
//            throw RuntimeException("Failed to format the contents")
            return contents
        }
        val r = p.getString(0)
        Native.free(Pointer.nativeValue(p))
        return r
    }
}
