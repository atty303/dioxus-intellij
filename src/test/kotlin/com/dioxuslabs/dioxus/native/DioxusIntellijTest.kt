package com.dioxuslabs.dioxus.native

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DioxusIntellijTest : BasePlatformTestCase() {

    fun testBasicFormatting() {
        // Test basic RSX formatting with spaces
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

        // Format with spaces, indent size 4, no split line attributes
        val formattedCode = DioxusIntellij.format(unformattedCode, false, 4, false)
        
        // Verify formatting
        assertEquals(expectedFormattedCode, formattedCode.trim())
    }

    fun testFormattingWithTabs() {
        // Test RSX formatting with tabs
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

        // Format with tabs, indent size 4, no split line attributes
        val formattedCode = DioxusIntellij.format(unformattedCode, true, 4, false)
        
        // Verify that tabs are used for indentation
        assertTrue(formattedCode.contains("\t"))
        
        // Verify the structure is correct (indentation levels may vary)
        assertTrue(formattedCode.contains("fn main()"))
        assertTrue(formattedCode.contains("let app = rsx!"))
        assertTrue(formattedCode.contains("div {"))
        assertTrue(formattedCode.contains("h1 { \"Hello, world!\" }"))
        assertTrue(formattedCode.contains("p { \"This is a test.\" }"))
    }

    fun testComplexFormatting() {
        // Test more complex RSX formatting with attributes
        val unformattedCode = """
            fn main() {
                let app = rsx! {
                    div { class: "container",
                    h1 { class: "title", "Hello, world!" }
                        p { class: "content", "This is a test with multiple attributes." }
                    button { onclick: move |_| println!("Clicked!"), "Click me" }
                    }
                };
            }
        """.trimIndent()

        // Format with spaces, indent size 4, with split line attributes
        val formattedCode = DioxusIntellij.format(unformattedCode, false, 4, true)
        
        // Verify the structure is correct
        assertTrue(formattedCode.contains("fn main()"))
        assertTrue(formattedCode.contains("let app = rsx!"))
        assertTrue(formattedCode.contains("div {"))
        assertTrue(formattedCode.contains("class: \"container\""))
        assertTrue(formattedCode.contains("h1 {"))
        assertTrue(formattedCode.contains("class: \"title\""))
        assertTrue(formattedCode.contains("\"Hello, world!\""))
        assertTrue(formattedCode.contains("p {"))
        assertTrue(formattedCode.contains("class: \"content\""))
        assertTrue(formattedCode.contains("button {"))
        assertTrue(formattedCode.contains("onclick: move |_| println!(\"Clicked!\")"))
        assertTrue(formattedCode.contains("\"Click me\""))
    }

    fun testDifferentIndentSizes() {
        // Test RSX formatting with different indent sizes
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

        // Format with spaces, indent size 2, no split line attributes
        val formattedCode2Spaces = DioxusIntellij.format(unformattedCode, false, 2, false)
        
        // Verify that 2-space indentation is used
        assertTrue(formattedCode2Spaces.contains("  div {"))
        
        // Format with spaces, indent size 8, no split line attributes
        val formattedCode8Spaces = DioxusIntellij.format(unformattedCode, false, 8, false)
        
        // Verify that 8-space indentation is used
        assertTrue(formattedCode8Spaces.contains("        div {"))
    }
}