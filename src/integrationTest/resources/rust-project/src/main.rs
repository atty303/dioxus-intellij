use dioxus::prelude::*;

fn main() {
    println!("Hello, world!");
}

fn _foo() -> Element {
    rsx! {
        h1 {
            "Hello, world!"
        }
    div { "Text" }
    }
}
