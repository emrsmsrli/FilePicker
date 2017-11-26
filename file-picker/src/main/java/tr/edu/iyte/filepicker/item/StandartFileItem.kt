package tr.edu.iyte.filepicker.item

/**
 * A [FileItem] that represents files and folders
 * @constructor Constructs a StandartFileItem which covers folders and files
 * @property name Name of the file (or folder)
 * @property isDirectory Flag for whether this is a directory or a file
 */
internal data class StandartFileItem(override val name: String,
                                     override val isDirectory: Boolean = false) : FileItem