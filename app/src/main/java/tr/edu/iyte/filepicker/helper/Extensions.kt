package tr.edu.iyte.filepicker.helper

import android.content.Context
import android.support.annotation.IdRes
import android.view.View
import android.util.Log
import android.widget.Toast
import tr.edu.iyte.filepicker.FilePicker
import tr.edu.iyte.filepicker.FilePickerMode

interface Loggable {
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

fun Loggable.info(message: Any) {
    if (Log.isLoggable(tag, Log.INFO)) {
        Log.i(tag, message.toString())
    }
}

fun Loggable.verbose(message: Any) {
    if (Log.isLoggable(tag, Log.VERBOSE)) {
        Log.v(tag, message.toString())
    }
}

inline fun <reified T : View> View.find(@IdRes id: Int): T = findViewById(id)
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.filePicker(mode: FilePickerMode = FilePickerMode.FILE_PICK,
                       onFileSelectedListener: (String) -> Unit)
    = FilePicker(this, mode, onFileSelectedListener).show()