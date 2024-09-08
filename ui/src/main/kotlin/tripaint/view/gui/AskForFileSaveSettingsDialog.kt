package tripaint.view.gui

import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.layout.*
import javafx.util.StringConverter
import tripaint.color.Color
import tripaint.coords.StorageCoords
import tripaint.image.ImageStorage
import tripaint.image.format.StorageFormat
import tripaint.view.JavaFxExt.toFXColor
import tripaint.view.gui.DialogUtils.getValueFromCustomDialog
import tripaint.view.gui.DialogUtils.makeGridPane
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

object AskForFileSaveSettingsDialog {
    fun askForFileSaveSettings(
        storage: ImageStorage,
        file: File,
        formats: List<Pair<StorageFormat, String>>,
        initiallySelectedFormat: Int
    ): Pair<StorageCoords, StorageFormat>? {
        val imageSize = storage.imageSize
        val (previewFile, previewWidth, previewHeight) = Triple(file, imageSize, imageSize)

        val xCoordTF = RestrictedTextField.uintTF()
        xCoordTF.promptText = "0"

        val yCoordTF = RestrictedTextField.uintTF()
        yCoordTF.promptText = "0"

        val formatMap: Map<StorageFormat, String> = mapOf(*formats.toTypedArray())

        val formatChooser = run {
            val b = ChoiceBox(FXCollections.observableArrayList(*formats.map { it.first }.toTypedArray()))
            b.selectionModel.select(initiallySelectedFormat)
            b.converter = object : StringConverter<StorageFormat>() {
                override fun toString(t: StorageFormat): String = formatMap[t]!!
                override fun fromString(s: String): StorageFormat = throw UnsupportedOperationException()
            }
            b
        }

        fun resultFromInputs(): Result<Pair<StorageCoords, StorageFormat>> {
            val xt = xCoordTF.text
            val yt = yCoordTF.text
            val format = formatChooser.selectionModel.selectedItem

            return runCatching {
                val xOffset = if (xt != "") xt.toInt() else 0
                val yOffset = if (yt != "") yt.toInt() else 0
                Pair(StorageCoords.from(xOffset, yOffset), format)
            }
        }

        val previewPane = StackPane()
        previewPane.setMinSize(previewWidth.toDouble(), previewHeight.toDouble())
        previewPane.setMaxSize(previewWidth.toDouble(), previewHeight.toDouble())

        val previewImage = WritableImage(imageSize, imageSize)
        val pixelFormat = PixelFormat.getIntArgbInstance()
        previewPane.children.add(ImageView(previewImage))

        val previewBorder = Pane()
        previewBorder.setMinSize(previewWidth.toDouble(), previewHeight.toDouble())
        previewBorder.setMaxSize(previewWidth.toDouble(), previewHeight.toDouble())
        previewPane.children.add(previewBorder)

        run {
            val stroke = BorderStroke(
                Color.Red.toFXColor(),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                BorderWidths.DEFAULT
            )
            previewBorder.border = Border(stroke)
        }

        val previewStack = Pane()
        try {
            val wholeImage = ImageView(Image(FileInputStream(previewFile)))
            previewStack.children.add(wholeImage)
        } catch (_: FileNotFoundException) {
        } catch (_: IOException) {
        }

        previewStack.children.add(previewPane)

        fun updatePreviewAction() {
            resultFromInputs().onSuccess {
                val (sc, format) = it
                val x = sc.x
                val y = sc.y
                previewPane.layoutX = x.toDouble()
                previewPane.layoutY = y.toDouble()

                val pixels = storage.toRegularImage(format).toIntArray()

                val w = previewImage.getPixelWriter()
                w.setPixels(0, 0, imageSize, imageSize, pixelFormat, pixels, 0, imageSize)
            }
        }

        updatePreviewAction()

        xCoordTF.textProperty().addListener { _ -> updatePreviewAction() }
        yCoordTF.textProperty().addListener { _ -> updatePreviewAction() }
        formatChooser.selectionModel.selectedItemProperty().addListener { _ -> updatePreviewAction() }

        val inputForm = makeGridPane(
            listOf(
                listOf(Label("X coordinate:"), xCoordTF),
                listOf(Label("Y coordinate:"), yCoordTF),
                listOf(Label("Format:"), formatChooser)
            )
        )

        return getValueFromCustomDialog(
            title = "Save file",
            headerText = "Where in the file should the image be saved, and how?",
            content = listOf(
                inputForm,
                Separator(Orientation.HORIZONTAL),
                previewStack
            ),
            resultConverter = { r ->
                when (r) {
                    ButtonType.OK -> resultFromInputs ().getOrDefault(null)
                    else          -> null
                }
            },
            buttons = listOf(ButtonType.OK, ButtonType.CANCEL)
        )
    }
}
