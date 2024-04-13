use std::ffi::{c_int, CStr, CString};
use std::os::raw::c_char;
use std::ptr::null_mut;

use dioxus_autofmt::{IndentOptions, IndentType};

#[no_mangle]
pub extern "C" fn format(raw: *const c_char, use_tabs: c_int, indent_size: c_int, split_line_attributes: c_int) -> *mut c_char {
    if let Ok(contents) = unsafe { CStr::from_ptr(raw) }.to_str() {
        let r = std::panic::catch_unwind(move || {
            let edits = dioxus_autofmt::fmt_file(contents, IndentOptions::new(
                if use_tabs == 1 { IndentType::Tabs } else { IndentType::Spaces },
                indent_size as usize,
                split_line_attributes == 1,
            ));
            dioxus_autofmt::apply_formats(contents, edits)
        });
        match r {
            Ok(out) => CString::new(out).expect("Failed to create CString").into_raw(),
            Err(_) => {
                null_mut()
            }
        }
    } else {
        null_mut()
    }
}
