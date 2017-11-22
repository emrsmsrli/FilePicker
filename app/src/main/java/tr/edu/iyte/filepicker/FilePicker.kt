package tr.edu.iyte.filepicker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import android.view.View
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
 * @see Context.filePicker
 */
class FilePicker(private val context: Context,
                 private val mode: FilePickerMode = FilePickerMode.FILE_PICK,
                 private val onFileSelectedListener: (String) -> Unit) : Loggable {
    private val stack = Stack<String>()
    private var path = "root"
    private val storages: List<StorageFileItem>
    private lateinit var dialog: AlertDialog
    private lateinit var subTitle: TextView
    private lateinit var newFolderButton: Button
    private lateinit var okButton: Button
    private lateinit var pickerAdapter: FilePickerAdapter

    init {
        val reg = "/Android".toRegex()
        storages = context.externalCacheDirs.map {
            val storageFile = File(it.path.split(reg)[0])

            val name = if(Environment.isExternalStorageRemovable(storageFile))
                context.getString(R.string.file_picker_internal_storage)
            else context.getString(R.string.file_picker_external_storage)

            StorageFileItem(name = name, path = storageFile.path)
        }.sortedBy { it.name }
    }

    /**
     * Shows the FilePicker with the given [context].
     * Picker starts with showing all available
     * storage devices.
     */
    @SuppressLint("InflateParams")
    fun show() {
        pickerAdapter = FilePickerAdapter(context) onItemClick@{ file ->
            if(file is UpFileItem) {
                path = stack.pop()
                info("up, now in $path")

                subTitle.text = path

                pickerAdapter.clear()
                if(stack.isEmpty()) {
                    pickerAdapter.addAll(storages, includeUp = false)
                    toggleVisibility(newFolderButton, shouldBeVisible = false)
                    if(mode == FilePickerMode.FOLDER_PICK)
                        toggleEnable(okButton, shouldEnable = false)
                } else {
                    pickerAdapter.addAll(getChildrenFiles(file = File(path)), includeUp = true)
                }

                return@onItemClick
            }

            if(!file.isDirectory) {
                path += "${File.separator}${file.name}"
                onFileSelectedListener(path)
                dialog.dismiss()
                return@onItemClick
            }

            if(stack.isEmpty()) {
                toggleVisibility(newFolderButton, shouldBeVisible = true)
                if(mode == FilePickerMode.FOLDER_PICK)
                    toggleEnable(okButton, shouldEnable = true)
            }

            stack.push(path)

            if(stack.size == 1 && file is StorageFileItem) // this means we go in one of the storages
                path = file.path
            else
                path += "${File.separator}${file.name}"
            info("down, now in $path")

            val dir = File(path)
            subTitle.text = path

            pickerAdapter.clear()
            val children = getChildrenFiles(file = dir)
            if(children.isEmpty()) {
                context.toast(context.getString(R.string.file_picker_no_subdirectories))
            }

            pickerAdapter.addAll(children, includeUp = true)
        }

        dialog = AlertDialog.Builder(context)
                .setNeutralButton(R.string.file_picker_folder_new, null)
                .also {
                    val inflater = LayoutInflater.from(context)
                    val titleView = inflater.inflate(R.layout.file_picker_custom_dialog_title, null)
                    titleView.find<TextView>(R.id.title).text =
                            when(mode) {
                                FilePickerMode.FILE_PICK -> context.getString(R.string.file_picker_select_file)
                                FilePickerMode.FOLDER_PICK   -> context.getString(R.string.file_picker_select_folder)
                            }
                    subTitle = titleView.find(R.id.subtitle)
                    subTitle.text = path

                    it.setCustomTitle(titleView)

                    val customView = inflater.inflate(R.layout.file_picker_custom_dialog_view, null)
                    val recycler = customView.find<RecyclerView>(R.id.recycler)
                    recycler.layoutManager = LinearLayoutManager(context)
                    recycler.adapter = pickerAdapter
                    val decoration = DividerItemDecoration(context, LinearLayout.VERTICAL)
                    decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.file_picker_line_divider))
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
        pickerAdapter.addAll(storages)

        okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        if(mode == FilePickerMode.FOLDER_PICK)
            toggleEnable(okButton, shouldEnable = false)

        newFolderButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        newFolderButton.setOnClickListener {
            NewFolderDialog(context)
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

    @SuppressLint("InflateParams")
    private inner class NewFolderDialog(context: Context) {
        private val layout: TextInputLayout
                = LayoutInflater
                    .from(context)
                    .inflate(R.layout.file_picker_text_input, null) as TextInputLayout
        private val textField: TextInputEditText = layout.find(R.id.reply_text)

        init {
            val dialog = with(AlertDialog.Builder(context)) {
                setPositiveButton(android.R.string.ok, null)
                setNegativeButton(android.R.string.cancel) { di, _ -> di.dismiss() }
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
                    layout.error = context.getString(R.string.file_picker_folder_name_empty)
                else {
                    val fName = textField.text.toString()
                    File("$path${File.separator}$fName").mkdir()
                    pickerAdapter.newFolder(fName)
                    dialog.dismiss()
                }
            }
        }
    }

    internal companion object {
        private const val ANIM_TIME = 200L
        private const val FULL_ALPHA = 1f
        private const val NO_ALPHA = 0f

        fun toggleVisibility(v: View, shouldBeVisible: Boolean) {
            with(v.animate()) {
                duration = ANIM_TIME
                if(shouldBeVisible) {
                    alpha(FULL_ALPHA)
                    setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            super.onAnimationStart(animation)
                            v.visibility = View.VISIBLE
                        }
                    })
                } else {
                    alpha(NO_ALPHA)
                    setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            v.visibility = View.GONE
                            super.onAnimationEnd(animation)
                        }
                    })
                }
                start()
            }
        }

        fun toggleEnable(b: Button, shouldEnable: Boolean) {
            b.isEnabled = shouldEnable
        }
    }
}