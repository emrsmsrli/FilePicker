package tr.edu.iyte.filepicker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import tr.edu.iyte.filepicker.helper.*
import tr.edu.iyte.filepicker.item.*
import java.io.File
import java.util.*

/**
 * A generic FilePicker class for showing all the way from root storage directories.
 * @param mode Mode for the picker to select files or folders
 * @param onFileSelectedListener A listener object for item click events
 */
class FilePicker(private val mode: FilePickerMode = FilePickerMode.FOLDER_PICK,
                 private val onFileSelectedListener: (String) -> Unit) : Loggable {
    private val stack = Stack<String>()
    private var path = ""
    private lateinit var dialog: AlertDialog
    private lateinit var subTitle: TextView
    private lateinit var upButton: Button
    private lateinit var pickerAdapter: FilePickerAdapter

    /**
     * Shows the FilePicker with the given [context].
     * Picker starts with showing all available
     * storage devices.
     * @param context A context for loading string and icon resources
     */
    @SuppressLint("InflateParams")
    fun show(context: Context) {
        val extDirectory = Environment.getExternalStorageDirectory()
        path = extDirectory.absolutePath
        pickerAdapter = FilePickerAdapter(context) onItemClick@{ file ->
            if(file is UpFileItem) {
                path = stack.pop()
                info("up, now in $path")

                val upDir = File(path)
                subTitle.text = path

                pickerAdapter.clear()
                if(stack.isEmpty()) {
                    pickerAdapter.addAll(getChildrenFiles(file = upDir), includeUp = false)
                } else {
                    pickerAdapter.addAll(getChildrenFiles(file = upDir), includeUp = true)
                }

                return@onItemClick
            }

            if(!file.isDirectory) {
                path += "${File.separator}${file.name}"
                onFileSelectedListener(path)
                dialog.dismiss()
                return@onItemClick
            }

            stack.push(path)

            path += "${File.separator}${file.name}"
            info("down, now in $path")

            val dir = File(path)
            subTitle.text = path

            pickerAdapter.clear()
            val children = getChildrenFiles(file = dir)
            if(children.isEmpty()) {
                context.toast(context.getString(R.string.no_subdirectories))
            }

            pickerAdapter.addAll(children, includeUp = true)
        }

        dialog = AlertDialog.Builder(context)
                .setNeutralButton(R.string.new_folder, null)
                .also {
                    val inflater = LayoutInflater.from(context)
                    val titleView = inflater.inflate(R.layout.custom_dialog_title, null)
                    titleView.find<TextView>(R.id.title).text =
                            when(mode) {
                                FilePickerMode.FOLDER_PICK -> context.getString(R.string.select_file)
                                FilePickerMode.FILE_PICK   -> context.getString(R.string.select_folder)
                            }
                    subTitle = titleView.find(R.id.subtitle)
                    subTitle.text = path

                    it.setCustomTitle(titleView)

                    val customView = inflater.inflate(R.layout.custom_dialog_view, null)
                    val recycler = customView.find<RecyclerView>(R.id.recycler)
                    recycler.layoutManager = LinearLayoutManager(context)
                    recycler.adapter = pickerAdapter
                    val decoration = DividerItemDecoration(context, LinearLayout.VERTICAL)
                    decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.line_divider))
                    recycler.addItemDecoration(decoration)
                    it.setView(customView)

                    when(mode) {
                        FilePickerMode.FOLDER_PICK -> {
                            it.setNegativeButton(android.R.string.cancel) {
                                dialog, _ -> dialog.dismiss()
                            }.setPositiveButton(android.R.string.ok) { dialog, _ ->
                                onFileSelectedListener(path)
                                dialog.dismiss()
                            }
                        }
                        FilePickerMode.FILE_PICK -> {
                            it.setPositiveButton(android.R.string.cancel) {
                                dialog, _ -> dialog.dismiss()
                            }
                        }
                    }
                }.show()
        pickerAdapter.addAll(getChildrenFiles(extDirectory))

        upButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        upButton.setOnClickListener {
            NewFolderDialog().show(context)
        }

        verbose("dialog init complete")
    }

    private fun getChildrenFiles(file: File): List<FileItem> {
        val children = file.listFiles()

        fun getFileName(path: String): String {
            val parts = path.split(File.separator)
            return parts[parts.size - 1]
        }

        fun filter(isDir: Boolean)
                = children.filter { it.isDirectory == isDir }
                .map { StandartFileItem(getFileName(it.absolutePath), isDir) }
                .sortedBy { it.name.toLowerCase() }
        val directoryNames = filter(isDir = true)

        return when(mode) {
            FilePickerMode.FOLDER_PICK -> directoryNames
            FilePickerMode.FILE_PICK   -> directoryNames + filter(isDir = false)
        }
    }

    private inner class NewFolderDialog {
        private lateinit var layout: TextInputLayout
        private lateinit var textField: TextInputEditText

        @SuppressLint("InflateParams")
        fun show(context: Context) {
            val dialog = with(AlertDialog.Builder(context)) {
                setPositiveButton(android.R.string.ok, null)
                setNegativeButton(android.R.string.cancel) { di, _ -> di.dismiss() }
                layout = LayoutInflater.from(context).inflate(R.layout.text_input, null) as TextInputLayout
                textField = layout.find(R.id.reply_text)
                textField.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if(!s.isBlank())
                            layout.error = null
                    }
                })
                setView(layout)
                show()
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if(textField.text.isBlank())
                    layout.error = context.getString(R.string.folder_name_empty)
                else {
                    val fName = textField.text.toString()
                    File("$path${File.separator}$fName").mkdir()
                    pickerAdapter.newFolder(fName)
                    dialog.dismiss()
                }
            }
        }
    }
}