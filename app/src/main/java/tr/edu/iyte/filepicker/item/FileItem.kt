package tr.edu.iyte.filepicker.item

/**
 * FileItem interface that represents all available list items in the picker.
 */
internal interface FileItem {
    /**
     * Name of the item
     */
    val name: String

    /**
     * Flag for whether this item is a directory or a file
     */
    val isDirectory: Boolean
}