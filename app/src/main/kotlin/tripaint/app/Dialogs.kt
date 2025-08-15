package tripaint.app

import javafx.scene.control.*
import javafx.stage.FileChooser
import javafx.stage.Stage
import tripaint.color.Color
import tripaint.coords.StorageCoords
import tripaint.effects.BlurEffect
import tripaint.effects.MotionBlurEffect
import tripaint.effects.RandomNoiseEffect
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.format.RecursiveStorageFormat
import tripaint.image.format.SimpleStorageFormat
import tripaint.image.format.StorageFormat
import tripaint.view.JavaFxExt.fromFXColor
import tripaint.view.JavaFxExt.toFXColor
import tripaint.view.gui.*
import tripaint.view.gui.DialogUtils.getValueFromCustomDialog
import tripaint.view.gui.DialogUtils.makeGridPane
import java.io.File

object Dialogs {
    private var currentFolder: File? = null

    fun askForSaveFile(stage: Stage, image: GridCell): File? {
        val chooser = FileChooser()
        currentFolder?.let { chooser.initialDirectory = it }
        chooser.title = "Save file"
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("PNG", "*.png"))
        val result = chooser.showSaveDialog(stage)
        result?.let { currentFolder = it.getParentFile() }
        return result
    }

     fun askForFileSaveSettings(file: File, image: GridCell): Pair<StorageCoords, StorageFormat>? {
        return AskForFileSaveSettingsDialog.askForFileSaveSettings(
            image.storage,
            file,
            listOf(
                Pair(SimpleStorageFormat, "Simple format"),
                Pair(RecursiveStorageFormat, "Recursive format")
            ),
            0
        )
    }

     fun askForFileToOpen(stage: Stage): File? {
        val chooser = FileChooser()
        chooser.title = "Open file"
        val result = chooser.showOpenDialog(stage)
        result?.let { currentFolder = it.getParentFile() }
        return result
    }

     fun askForWhereToPutImage(): Pair<Int, Int>? {
        return AskForXYDialog.askForXY(
            title = "New image",
            headerText = "Please enter where it should be placed."
        )
    }

     fun askForBlurRadius(imageGrid: ImageGrid, imagePool: ImagePool): Int? {
        val selectedImagesCoords = imageGrid.selectedImages().map { it.coords }
        return DialogUtils.getValueFromDialog(
            imagePool,
            imageGrid.selectedImages(),
            "Blur images",
            "How much should the images be blurred?",
            "Radius:",
            { TextFieldRestriction.uintRestriction(it) },
            { str -> runCatching { str.toInt() }.getOrDefault(0) },
            { radius, grid -> if (radius > 0) BlurEffect(radius).action(selectedImagesCoords, grid) },
        )
    }

     fun askForMotionBlurRadius(imageGrid: ImageGrid, imagePool: ImagePool): Int? {
        val selectedImagesCoords = imageGrid.selectedImages().map { it.coords }
        return DialogUtils.getValueFromDialog(
            imagePool,
            imageGrid.selectedImages(),
            "Motion blur images",
            "How much should the images be motion blurred?",
            "Radius:",
            { TextFieldRestriction.uintRestriction(it) },
            { str -> runCatching { str.toInt() }.getOrDefault(0) },
            { radius, grid -> if (radius > 0) MotionBlurEffect(radius).action(selectedImagesCoords, grid) },
        )
    }

     fun askForRandomNoiseColors(imageGrid: ImageGrid, imagePool: ImagePool): Pair<Color, Color>? {
        val images = imageGrid.selectedImages()
        val selectedImagesCoords = imageGrid.selectedImages().map { it.coords }
        val loColorPicker = ColorPicker(Color.Black.toFXColor())
        val hiColorPicker = ColorPicker(Color.White.toFXColor())

        val (previewPane, updatePreview) = DialogUtils.makeImagePreviewList(images, imagePool)

        fun updatePreviewFromInputs() {
            val lo = fromFXColor(loColorPicker.value)
            val hi = fromFXColor(hiColorPicker.value)
            updatePreview { grid -> RandomNoiseEffect(lo, hi).action(selectedImagesCoords, grid) }
        }

        loColorPicker.valueProperty().addListener { _, _, _ -> updatePreviewFromInputs() }
        hiColorPicker.valueProperty().addListener { _, _, _ -> updatePreviewFromInputs() }

        updatePreviewFromInputs()

        return getValueFromCustomDialog(
            title = "Fill images randomly",
            headerText = "Which color-range should be used?",
            graphic = previewPane,
            content = listOf(
                makeGridPane(
                    listOf(
                        listOf(Label("Minimum color:"), loColorPicker),
                        listOf(Label("Maximum color:"), hiColorPicker)
                    )
                )
            ),
            resultConverter = { r ->
                when (r) {
                    ButtonType.OK -> {
                        val lo = runCatching { loColorPicker.value }.getOrDefault(null)
                        val hi = runCatching { hiColorPicker.value }.getOrDefault(null)
                        if (lo != null && hi != null) Pair(fromFXColor(lo), fromFXColor(hi)) else null
                    }

                    else -> null
                }
            },
            buttons = listOf(ButtonType.OK, ButtonType.CANCEL)
        )
    }

     fun askSaveBeforeClosing(imagePool: ImagePool, images: List<GridCell>): Boolean? {
        return saveBeforeClosingAlert(imagePool, images).showAndWait().orElse(null)?.let { res ->
            return when (res.buttonData) {
                ButtonBar.ButtonData.YES -> true
                ButtonBar.ButtonData.NO -> false
                else -> null
            }
        }
    }

    private fun saveBeforeClosingAlert(imagePool: ImagePool, images: List<GridCell>): Alert {
        val (previewPane, _) = DialogUtils.makeImagePreviewList(images, imagePool)

        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Save before closing?"
        alert.headerText =
            "Do you want to save " + (if (images.size == 1) "this image" else "these images") + " before closing the tab?"
        alert.graphic = previewPane

        alert.buttonTypes.setAll(
            ButtonType("Save", ButtonBar.ButtonData.YES),
            ButtonType("Don't save", ButtonBar.ButtonData.NO),
            ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        )
        return alert
    }

     fun askForFileOpenSettings(
         fileSystem: FileSystem,
        file: File,
        imageSize: Int,
        xCount: Int,
        yCount: Int
    ): Pair<StorageCoords, StorageFormat>? {
        return AskForFileOpenSettingsDialog.askForFileOpenSettings(
            imagePreview = ImagePreviewParams(file, imageSize, xCount, yCount),
            listOf(
                Pair(SimpleStorageFormat, "Simple format"),
                Pair(RecursiveStorageFormat, "Recursive format")
            ).filter { it.first.supportsImageSize(imageSize) },
            0
        ) { fileSystem.readImage(it) }
    }

     fun shouldReplaceImage(
         imageGrid: ImageGrid,
         imagePool: ImagePool,
        currentImage: ImageStorage,
        newImage: ImageStorage,
        location: ImagePool.SaveLocation
    ): Boolean? {
        val tri1 = imageGrid.findByStorage(newImage)!!
        val tri2 = imageGrid.findByStorage(currentImage)!!
        val (previewPane, _) = DialogUtils.makeImagePreviewList(listOf(tri1, tri2), imagePool)

        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Collision"
        alert.headerText = "The image already exists on the screen. Which one should be used?"
        alert.graphic = previewPane

        alert.buttonTypes.setAll(
            ButtonType("Left", ButtonBar.ButtonData.YES),
            ButtonType("Right", ButtonBar.ButtonData.NO),
            ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        )
        return alert.showAndWait().map { it.buttonData == ButtonBar.ButtonData.YES }.orElse(null)
    }

    fun askForImageSize(): Int? {
        val dialog = TextInputDialog("32")
        dialog.title = "Image Size"
        dialog.headerText = "What should be the image size? (#rows)"
        val sizeStr = dialog.showAndWait().orElse(null)
        return sizeStr?.let { str ->
            runCatching { str.toInt() }.getOrDefault(null)?.let { if (it > 0) it else null } ?: 32
        }
    }
}