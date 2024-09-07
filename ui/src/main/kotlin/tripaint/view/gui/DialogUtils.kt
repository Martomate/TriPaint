package tripaint.view.gui

import javafx.application.Platform
import javafx.beans.property.StringProperty
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.view.image.TriImageForPreview

@JvmInline
value class TextFieldRestriction(private val f: (String) -> Boolean) {
    operator fun invoke(s: String) = f(s)

    companion object {
        private fun isTrue(pred: () -> Boolean): Boolean = runCatching { pred() }.getOrDefault(false)

        fun customIntRestriction(pred: (Int) -> Boolean): TextFieldRestriction = TextFieldRestriction { s ->
            isTrue { pred(s.toInt()) } || isTrue { pred((s + "1").toInt()) }
        }

        val intRestriction: TextFieldRestriction = customIntRestriction { _ -> true }
        val uintRestriction: TextFieldRestriction = customIntRestriction { it >= 0 }
    }
}

object RestrictedTextField {
    fun intTF(): TextField = restrict(TextField(), TextFieldRestriction.intRestriction)
    fun uintTF(): TextField = restrict(TextField(), TextFieldRestriction.uintRestriction)

    fun restrict(tf: TextField, contentAllowed: TextFieldRestriction): TextField {
        tf.textProperty().addListener { ob, oldVal, newVal ->
            val property = ob as StringProperty
            property.value = if (contentAllowed(newVal)) newVal else oldVal
        }
        return tf
    }
}

object DialogUtils {
    fun makeGridPane(content: List<List<Node>>): GridPane {
        val gridPane = GridPane()
        gridPane.vgap = 10.0
        gridPane.hgap = 10.0
        for (i in content.indices) {
            for (j in content[i].indices) {
                gridPane.add(content[i][j], j, i)
            }
        }
        return gridPane
    }

    fun <R> getValueFromCustomDialog(
        title: String,
        headerText: String? = null,
        contentText: String? = null,
        graphic: Node? = null,
        content: List<Region> = listOf(),
        resultConverter: (ButtonType) -> R?,
        nodeWithFocus: Node? = null,
        buttons: List<ButtonType> = listOf(ButtonType.OK, ButtonType.CANCEL)
    ): R? {
        val dialog = Dialog<R>()
        dialog.title = title
        dialog.headerText = headerText
        dialog.contentText = contentText
        dialog.graphic = graphic

        val contentBox = VBox(*content.toTypedArray())
        contentBox.spacing = 10.0

        dialog.dialogPane.content = contentBox
        dialog.setResultConverter { b -> resultConverter(b) }

        for (b in buttons) {
            dialog.dialogPane.buttonTypes.add(b)
        }

        if (nodeWithFocus != null) {
            dialog.setOnShowing { _ -> Platform.runLater { nodeWithFocus.requestFocus() } }
        }

        return dialog.showAndWait().orElse(null)
    }

    fun <T> getValueFromDialog(
        imagePool: ImagePool,
        images: List<GridCell>,
        title: String,
        headerText: String,
        contentText: String,
        restriction: (String) -> Boolean,
        stringToValue: (String) -> T,
        refreshPreviewFn: ((T, ImageGrid) -> Unit)? = null
    ): T? {
        val (previewPane, updatePreview) = makeImagePreviewList(images, imagePool)

        val dialog = TextInputDialog()
        dialog.title = title
        dialog.headerText = headerText
        dialog.contentText = contentText
        dialog.graphic = previewPane
        RestrictedTextField.restrict(dialog.editor, TextFieldRestriction(restriction))

        if (refreshPreviewFn != null) {
            dialog.editor.textProperty().addListener { _, _, s ->
                if (restriction(s)) {
                    val value = stringToValue(s)
                    updatePreview { imageGrid -> refreshPreviewFn(value, imageGrid) }
                } else {
                    updatePreview { _ -> }
                }
            }
        }

        return dialog.showAndWait().map(stringToValue).orElse(null)
    }

    fun makeImagePreviewList(
        images: List<GridCell>,
        imagePool: ImagePool
    ): Pair<ScrollPane, (effect: (ImageGrid) -> Unit) -> Unit> {
        return ImagePreviewList.fromImageContent(images, TriImageForPreview.previewSize) { imagePool.locationOf(it) }
    }
}
