package tr.edu.iyte.filepicker

/**
 * Enumeration for changing the mode of a FilePicker.
 * @see FOLDER_PICK
 * @see FILE_PICK
 */
enum class FilePickerMode {
    /**
     * This option enables [FilePicker] to show folders only.
     * When you want to save some file somewhere, choose this.
     */
    FOLDER_PICK,

    /**
     * This option enables [FilePicker] to show files alongside with folders.
     * When you want to load some file, choose this.
     */
    FILE_PICK
}