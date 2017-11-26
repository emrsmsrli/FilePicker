package tr.edu.iyte.filepicker.style

import android.graphics.Color
import tr.edu.iyte.filepicker.helper.opaque
import tr.edu.iyte.filepicker.helper.withAlpha

/**
 * A style definition for a FilePicker. Lets you define colors
 * of the background colors and text colors.
 * @constructor Constructs a set of styles for a FilePicker
 * @property titleBackgroundColor Background color of the title
 * @property titleTextColor Color of the title text
 * @property titleSecondaryTextColor Color of the subtitle text
 * @property listBackgroundColor Background color of the list
 * @property listDividerColor Color of the list item dividers
 * @property buttonStyle Contains style information about buttons
 * @param buttonBackgroundColor Background color of the buttons
 * @param buttonBackgroundRippleColor Background ripple color of the buttons
 * @param buttonTextColor Color of the color texts
 * @property itemStyle Contains style information about list items
 * @param listItemBackgroundColor Background color of the list items
 * @param listItemBackgroundRippleColor Background ripple color of items
 * @param listItemDrawableTintColor Color of the list item icons
 * @param listItemTextColor Color of the list item texts
 * @param listItemSecondaryTextColor Color of the secondary texts of list items
 * @see Color.rgb
 * @see Color.argb
 * @see FilePickerStyle.STYLE_LIGHT
 * @see FilePickerStyle.STYLE_DARK
 * @see FilePickerStyle.STYLE_AMOLED
 */
class FilePickerStyle(val titleBackgroundColor: Int,
                      val titleTextColor: Int,
                      val titleSecondaryTextColor: Int,
                      val listBackgroundColor: Int,
                      val listDividerColor: Int,
                      buttonBackgroundColor: Int,
                      buttonBackgroundRippleColor: Int,
                      buttonTextColor: Int,
                      buttonTextDisabledColor: Int,
                      listItemBackgroundColor: Int,
                      listItemBackgroundRippleColor: Int,
                      listItemDrawableTintColor: Int,
                      listItemTextColor: Int,
                      listItemSecondaryTextColor: Int) {
    val itemStyle = FilePickerItemStyle(
            backgroundColor = listItemBackgroundColor,
            backgroundRippleColor = listItemBackgroundRippleColor,
            drawableTintColor = listItemDrawableTintColor,
            textColor = listItemTextColor,
            secondaryTextColor = listItemSecondaryTextColor
    )

    val buttonStyle = FilePickerButtonStyle(
            backgroundColor = buttonBackgroundColor,
            backgroundRippleColor = buttonBackgroundRippleColor,
            textColor = buttonTextColor,
            textDisabledColor = buttonTextDisabledColor
    )

    companion object {
        /**
         * Predefined light style for a FilePicker
         */
        val STYLE_LIGHT = FilePickerStyle(
                titleBackgroundColor = Color.TRANSPARENT,
                titleTextColor = Color.BLACK.withAlpha(alpha = 0xDE),
                titleSecondaryTextColor = Color.BLACK.withAlpha(alpha = 0x8A),
                buttonBackgroundColor = Color.WHITE,
                buttonBackgroundRippleColor = Color.BLACK.withAlpha(alpha = 0x1F),
                buttonTextColor = 0x596AC6.opaque,
                buttonTextDisabledColor = Color.BLACK.withAlpha(alpha = 0x24),
                listBackgroundColor = Color.WHITE,
                listDividerColor = 0xEEEEEE.opaque,
                listItemBackgroundColor = Color.TRANSPARENT,
                listItemBackgroundRippleColor = Color.BLACK.withAlpha(alpha = 0x1F),
                listItemDrawableTintColor = 0x3F51B5.opaque,
                listItemTextColor = Color.BLACK.withAlpha(alpha = 0xDE),
                listItemSecondaryTextColor = Color.BLACK.withAlpha(alpha = 0x8A)
        )

        /**
         * Predefined dark style for a FilePicker
         */
        val STYLE_DARK = FilePickerStyle(
                titleBackgroundColor = 0,
                titleTextColor = 0,
                titleSecondaryTextColor = 0,
                buttonBackgroundColor = 0,
                buttonBackgroundRippleColor = 0,
                buttonTextColor = 0,
                buttonTextDisabledColor = 0,
                listBackgroundColor = 0,
                listDividerColor = 0,
                listItemBackgroundColor = 0,
                listItemBackgroundRippleColor = 0,
                listItemDrawableTintColor = 0,
                listItemTextColor = 0,
                listItemSecondaryTextColor = 0
        )

        /**
         * Predefined dark style for a FilePicker with a pure black background
         */
        val STYLE_AMOLED = FilePickerStyle(
                titleBackgroundColor = 0,
                titleTextColor = 0,
                titleSecondaryTextColor = 0,
                buttonBackgroundColor = 0,
                buttonBackgroundRippleColor = 0,
                buttonTextColor = 0,
                buttonTextDisabledColor = 0,
                listBackgroundColor = 0,
                listDividerColor = 0,
                listItemBackgroundColor = 0,
                listItemBackgroundRippleColor = 0,
                listItemDrawableTintColor = 0,
                listItemTextColor = 0,
                listItemSecondaryTextColor = 0
        )
    }
}