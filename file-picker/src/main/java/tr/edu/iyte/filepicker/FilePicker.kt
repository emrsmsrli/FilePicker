package tr.edu.iyte.filepicker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.*
import android.os.Build
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
import tr.edu.iyte.filepicker.style.FilePickerButtonStyle
import tr.edu.iyte.filepicker.style.FilePickerStyle
import java.io.File
import java.util.*

/**
 * A generic FilePicker class for showing all the way from root storage directories.
 * @constructor Constructs a FilePicker object.
 * @property context A context for loading resources
 * @property mode Mode for the picker to select files or folders
 * @property style A style object for coloring the FilePicker
 * @property onFileSelectedListener A listener object for item click events
 * @see filePicker
 * @see FilePickerStyle.STYLE_LIGHT
 * @see FilePickerStyle.STYLE_DARK
 * @see FilePickerStyle.STYLE_AMOLED
 * @see FilePicker.show
 */
class FilePicker(private val context: Context,
                 private val mode: FilePickerMode = FilePickerMode.FILE_PICK,
                 private val style: FilePickerStyle = FilePickerStyle.STYLE_LIGHT,
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

            if(!Environment.isExternalStorageRemovable(storageFile)) {
                StorageFileItem(name = context.getString(R.string.file_picker_internal_storage),
                        path = storageFile.path,
                        isInternal = true)
            } else {
                StorageFileItem(name = context.getString(R.string.file_picker_external_storage),
                        path = storageFile.path,
                        isInternal = false)
            }
        }.sortedBy { it.name }
    }

    /**
     * Shows the FilePicker.
     * Picker starts with showing all available
     * storage devices.
     */
    @SuppressLint("InflateParams")
    fun show() {
        pickerAdapter = FilePickerAdapter(context, style.itemStyle) onItemClick@{ file ->
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
                    titleView.setBackgroundColor(style.titleBackgroundColor)

                    val title = titleView.find<TextView>(R.id.title)
                    title.setTextColor(style.titleTextColor)
                    title.text = when(mode) {
                        FilePickerMode.FILE_PICK -> context.getString(R.string.file_picker_select_file)
                        FilePickerMode.FOLDER_PICK   -> context.getString(R.string.file_picker_select_folder)
                    }

                    subTitle = titleView.find(R.id.subtitle)
                    subTitle.text = path
                    subTitle.setTextColor(style.titleSecondaryTextColor)

                    it.setCustomTitle(titleView)

                    val customView = inflater.inflate(R.layout.file_picker_custom_dialog_view, null)
                    val recycler = customView.find<RecyclerView>(R.id.recycler)
                    recycler.layoutManager = LinearLayoutManager(context)
                    recycler.adapter = pickerAdapter

                    val decoration = DividerItemDecoration(context, LinearLayout.VERTICAL)
                    val dividerDrawable = ContextCompat.getDrawable(context, R.drawable.file_picker_line_divider)
                    dividerDrawable.setColorFilter(style.listDividerColor, PorterDuff.Mode.SRC_IN)
                    decoration.setDrawable(dividerDrawable)
                    recycler.addItemDecoration(decoration)

                    customView.setBackgroundColor(style.listBackgroundColor)
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

        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        okButton.setTextColor(ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
                intArrayOf(style.buttonStyle.textDisabledColor, style.buttonStyle.textColor)))
        newFolderButton.setTextColor(style.buttonStyle.textColor)
        cancelButton?.setTextColor(style.buttonStyle.textColor)

        okButton.background = newRippleBackground(style = style.buttonStyle)
        newFolderButton.background = newRippleBackground(style = style.buttonStyle)
        cancelButton?.background = newRippleBackground(style = style.buttonStyle)

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
            // TODO implement styling for newfolderdialog
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

        fun newRippleBackground(style: FilePickerButtonStyle): Drawable {
            val bg = RippleDrawable(
                    ColorStateList.valueOf(style.backgroundRippleColor),
                    ColorDrawable(style.backgroundColor),
                    InsetDrawable(GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.WHITE , Color.WHITE)),
                            12, 18, 12, 18)
            )

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                bg.setPadding(36, 30, 36, 30)

            return bg
        }
    }
}