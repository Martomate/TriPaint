package tripaint.view.gui

import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.util.StringConverter
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.StorageCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImageStorage
import tripaint.image.RegularImage
import tripaint.image.format.StorageFormat
import tripaint.view.FileOpenSettings
import tripaint.view.JavaFxExt.toFXColor
import tripaint.view.gui.DialogUtils.getValueFromCustomDialog
import tripaint.view.gui.DialogUtils.makeGridPane
import tripaint.view.image.TriImageForPreview
import java.io.File

data class ImagePreviewParams(val previewFile: File, val imageSize: Int, val xCount: Int, val yCount: Int)

object AskForFileOpenSettingsDialog {
    fun askForFileOpenSettings(
        imagePreview: ImagePreviewParams,
        formats: List<Pair<StorageFormat, String>>,
        initiallySelectedFormat: Int,
        readImage: (File) -> RegularImage?
    ): FileOpenSettings? {
        val (previewFile, imageSize, xCount, yCount) = imagePreview
        val (previewWidth, previewHeight) = Pair(xCount * imageSize, yCount * imageSize)

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

        fun resultFromInputs(): Result<FileOpenSettings> {
            val xt = xCoordTF.text
            val yt = yCoordTF.text
            val format = formatChooser.selectionModel.selectedItem

            return Result.runCatching {
                val xOffset = if (xt != "") xt.toInt() else 0
                val yOffset = if (yt != "") yt.toInt() else 0
                FileOpenSettings(StorageCoords.from(xOffset, yOffset), format)
            }
        }

        val previewPaneBorder = run {
            val stroke = BorderStroke(
                    Color.Red.toFXColor(),
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderWidths.DEFAULT
            )
            Border(stroke)
        }

        val previewPane = run {
            val p = Pane()
            p.setMinSize(previewWidth.toDouble(), previewHeight.toDouble())
            p.setMaxSize(previewWidth.toDouble(), previewHeight.toDouble())
            p.border = previewPaneBorder
            p
        }

        val underlyingImage = readImage(previewFile)!!

        val wholeImage = ImageView(SwingFXUtils.toFXImage(underlyingImage.toBufferedImage(), null))

        val previewStack = Pane()
        previewStack.children.addAll(wholeImage, previewPane)

        val blankImage = ImageStorage.fill(imageSize, Color.White)
        val imageGridCells = (0..xCount).map { x -> GridCell(GridCoords.from(x, 0), blankImage) }.toList()
        val imageGrid = ImageGrid.fromCells(imageSize, imageGridCells)

        val (triPreviewPane, updateTriPreview) = ImagePreviewList.fromImageContent(
            imageGrid.images(),
            TriImageForPreview.previewSize
        ) { _ -> null }

        fun updatePreviewAction() {
            resultFromInputs().onSuccess {
                val (sc, format) = it
                val sx = sc.x
                val sy = sc.y
                previewPane.setLayoutX(sx.toDouble())
                previewPane.setLayoutY(sy.toDouble())

                for (x in 0 until xCount) {
                    val offset = StorageCoords.from(sx + x * imageSize, sy)
                    val newPreviewImage = ImageStorage
                        .fromRegularImage(underlyingImage, offset, format, imageSize)
                        .getOrDefault(blankImage)

                    imageGrid.replaceImage(GridCoords.from(x, 0), newPreviewImage)

                    updateTriPreview { _ -> }
                }
            }
        }

        updatePreviewAction()

        xCoordTF.textProperty().addListener { _ -> updatePreviewAction() }
        yCoordTF.textProperty().addListener { _ -> updatePreviewAction() }
        formatChooser.setOnAction { _ -> updatePreviewAction() }

        val inputForm = makeGridPane(
            listOf(
                listOf(Label("X coordinate:"), xCoordTF),
                listOf(Label("Y coordinate:"), yCoordTF),
                listOf(Label("Format:"), formatChooser)
            )
        )

        return getValueFromCustomDialog(
            title = "Open image",
            headerText = "Which part of the image should be opened? Please enter the top left corner:",
            content = listOf(
                inputForm,
                Separator(Orientation.HORIZONTAL),
                previewStack
            ),
            graphic = triPreviewPane,
            resultConverter = { r ->
                when (r) {
                    ButtonType.OK -> resultFromInputs().getOrDefault(null)
                    else -> null
                }
            },
            buttons = listOf(ButtonType.OK, ButtonType.CANCEL)
        )
    }
}
