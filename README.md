# FilePicker

A themable file picker library for Android. 

### Usage:
```kotlin
class SimpleActivity : Activity() {
  override fun onCreate(Bundle savedInstance) {
    FilePicker(this, FilePickerMode.FILE_PICK) { filePath ->
      // do something with the selected file
    }.show()
  }
}
```

### TODO:

- [ ] Fix themes.
