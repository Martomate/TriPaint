package tripaint.view.gui

import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import tripaint.view.gui.DialogUtils.getValueFromCustomDialog
import tripaint.view.gui.DialogUtils.makeGridPane

object AskForXYDialog {
    fun askForXY(title: String, headerText: String): Pair<Int, Int>? {
        val xCoordTF = RestrictedTextField.intTF()
        xCoordTF.promptText = "0"
        val yCoordTF = RestrictedTextField.intTF()
        yCoordTF.promptText = "0"

        fun coordsFromTF(): Result<Pair<Int, Int>> {
            val xt = xCoordTF.text
            val yt = yCoordTF.text

            return runCatching {
                val xOffset = if (xt != "") xt.toInt() else 0
                val yOffset = if (yt != "") yt.toInt() else 0
                Pair(xOffset, yOffset)
            }
        }

        return getValueFromCustomDialog(
            title = title,
            headerText = headerText,
            content = listOf(
                makeGridPane(
                    listOf(
                        listOf(Label("X coordinate:"), xCoordTF),
                        listOf(Label("Y coordinate:"), yCoordTF)
                    )
                )
            ),
            resultConverter = { r ->
                when (r) {
                    ButtonType.OK -> coordsFromTF().getOrDefault(null)
                    else -> null
                }
            },
            buttons = listOf(ButtonType.OK, ButtonType.CANCEL)
        )
    }
}
