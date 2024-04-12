use std::ffi::CString;
use std::os::raw::c_char;

#[no_mangle]
pub extern "C" fn format(doc: *mut c_char) -> *mut c_char {
    let doc = unsafe { CString::from_raw(doc) }.into_string().expect("Failed to convert to string");
    let r = format!("Hello, {}!", doc);
    CString::new(r).expect("Failed to create CString").into_raw()
}
