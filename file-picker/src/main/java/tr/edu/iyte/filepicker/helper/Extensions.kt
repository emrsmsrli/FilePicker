package tr.edu.iyte.filepicker.helper

import android.app.Fragment as F
import android.support.v4.app.Fragment as Fv4
import android.content.Context
import android.support.annotation.IdRes
import android.view.View
import android.util.Log
import android.widget.Toast
import tr.edu.iyte.filepicker.FilePicker
import tr.edu.iyte.filepicker.FilePickerMode
import tr.edu.iyte.filepicker.style.FilePickerStyle

internal interface Loggable {
    val tag: String
        get() = getTag(javaClass)
}

private fun getTag(clazz: Class<*>): String {
    val tag = clazz.simpleName
    return if (tag.length <= 23) {
        tag
    } else {
        tag.substring(0, 23)
    }
}

internal fun Loggable.info(message: Any) {
    if (Log.isLoggable(tag, Log.INFO)) {
        Log.i(tag, message.toString())
    }
}

internal fun Loggable.verbose(message: Any) {
    if (Log.isLoggable(tag, Log.VERBOSE)) {
        Log.v(tag, message.toString())
    }
}

internal val Int.opaque: Int
    get() = this or 0xFF000000.toInt()

internal fun Int.withAlpha(alpha: Int): Int {
    require(alpha in 0..0xFF)
    return this and 0x00FFFFFF or (alpha shl 24)
}

internal inline fun <reified T : View> View.find(@IdRes id: Int): T = findViewById(id)
internal fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

/**
 * Convenience method for easily creating and showing a [FilePicker].
 * @receiver A [Context] object
 * @param mode Mode for the picker to select files or folders. One of [FilePickerMode]s is applicable
 * @param style A [FilePickerStyle] object for defining colors of the elements of a FilePicker
 * @param onFileSelectedListener A listener object for item click events
 * @see FilePickerMode.FILE_PICK
 * @see FilePickerMode.FOLDER_PICK
 */
fun Context.filePicker(mode: FilePickerMode = FilePickerMode.FILE_PICK,
                       style: FilePickerStyle = FilePickerStyle.STYLE_LIGHT,
                       onFileSelectedListener: (String) -> Unit)
        = FilePicker(this, mode, style, onFileSelectedListener).show()

/**
 * Convenience method for easily creating and showing a [FilePicker].
 * @receiver A [android.app.Fragment] object
 * @param mode Mode for the picker to select files or folders. One of [FilePickerMode]s is applicable
 * @param style A [FilePickerStyle] object for defining colors of the elements of a FilePicker
 * @param onFileSelectedListener A listener object for item click events
 * @see FilePickerMode.FILE_PICK
 * @see FilePickerMode.FOLDER_PICK
 */
fun F.filePicker(mode: FilePickerMode = FilePickerMode.FILE_PICK,
                 style: FilePickerStyle = FilePickerStyle.STYLE_LIGHT,
                        onFileSelectedListener: (String) -> Unit)
        = FilePicker(activity, mode, style, onFileSelectedListener).show()

/**
 * Convenience method for easily creating and showing a [FilePicker].
 * @receiver A [android.support.v4.app.Fragment] object
 * @param mode Mode for the picker to select files or folders. One of [FilePickerMode]s is applicable
 * @param style A [FilePickerStyle] object for defining colors of the elements of a FilePicker
 * @param onFileSelectedListener A listener object for item click events
 * @see FilePickerMode.FILE_PICK
 * @see FilePickerMode.FOLDER_PICK
 */
fun Fv4.filePicker(mode: FilePickerMode = FilePickerMode.FILE_PICK,
                   style: FilePickerStyle = FilePickerStyle.STYLE_LIGHT,
                   onFileSelectedListener: (String) -> Unit)
        = FilePicker(activity, mode, style, onFileSelectedListener).show()