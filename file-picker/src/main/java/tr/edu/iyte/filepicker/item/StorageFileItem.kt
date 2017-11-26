package tr.edu.iyte.filepicker.item

/**
 * A [FileItem] that represents data storage units (SD Cards, Internal Storage)
 * @constructor Constructs a StorageFileItem which storage units
 * @property name Name of the storage unit
 * @property path Path of the storage unit
 * @property isInternal Indicates whether this storage unit is internal or not
 * @property isDirectory Flag for whether this is a directory or a file. A storage unit is always a directory.
 */
internal data class StorageFileItem(override val name: String,
                                    val path: String,
                                    val isInternal: Boolean) : FileItem {
    override val isDirectory = true
}