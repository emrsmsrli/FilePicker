package tr.edu.iyte.filepicker.item

/**
 * A [FileItem] that represents files and folders
 * @constructor Constructs a UpFileItem which covers Up functionality
 * @property name Name of the file. Always refers to @strings.up
*/
internal data class UpFileItem(override val name: String) : FileItem {

    /**
     * Flag for whether this is a directory or a file. Up is always a directory
     */
    override val isDirectory = true
}