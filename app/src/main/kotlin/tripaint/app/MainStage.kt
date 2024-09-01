package tripaint.app

import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import tripaint.color.Color
import tripaint.effects.BlurEffect
import tripaint.effects.MotionBlurEffect
import tripaint.effects.RandomNoiseEffect
import tripaint.grid.GridCell
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.format.RecursiveStorageFormat
import tripaint.image.format.SimpleStorageFormat
import tripaint.view.*
import tripaint.util.Resource
import tripaint.view.JavaFxExt.fromFXColor
import tripaint.view.JavaFxExt.toFXColor
import tripaint.view.gui.*
import tripaint.view.gui.DialogUtils.getValueFromCustomDialog
import tripaint.view.gui.DialogUtils.makeGridPane
import tripaint.view.image.ImageGridPane
import java.io.File

class MainStage(
    private val controls: TriPaintViewListener,
    private val model: TriPaintModel,
    private val stage: Stage,
) : TriPaintView {
    private val currentEditMode: Resource<EditMode>
    private val setCurrentEditMode: (EditMode) -> Unit

    init {
        val res = Resource.createResource(EditMode.Draw)
        this.currentEditMode = res.first
        this.setCurrentEditMode = res.second
    }

    private val imageDisplay: ImageGridPane = ImageGridPane(model.imageGrid, currentEditMode)

    private val menuBar: MenuBar = TheMenuBar.create(controls)
    private val toolBar: ToolBar = TheToolBar.create(controls)
    private val toolBox: TilePane = ToolBox.create(EditMode.all(), currentEditMode, setCurrentEditMode)
    private val imageTabs: TilePane =
        ImageTabs.fromImagePool(model.imageGrid, model.imagePool, controls::requestImageRemoval)
    private val colorBox: VBox = makeColorBox()

    private var currentFolder: File? = null

    init {
        stage.setTitle("TriPaint")
        stage.setOnCloseRequest { e ->
            if (!controls.requestExit()) e.consume()
        }

        val centerPane = AnchorPane(imageDisplay, toolBox, colorBox, imageTabs)
        AnchorPane.setTopAnchor(imageDisplay, 0.0)
        AnchorPane.setRightAnchor(imageDisplay, 0.0)
        AnchorPane.setBottomAnchor(imageDisplay, 0.0)
        AnchorPane.setLeftAnchor(imageDisplay, 0.0)
        imageDisplay.clipProperty().isEqualTo(centerPane.clipProperty())

        AnchorPane.setLeftAnchor(toolBox, 0.0)
        AnchorPane.setTopAnchor(toolBox, 0.0)

        AnchorPane.setLeftAnchor(colorBox, 10.0)
        AnchorPane.setBottomAnchor(colorBox, 10.0)

        AnchorPane.setRightAnchor(imageTabs, 10.0)
        AnchorPane.setTopAnchor(imageTabs, 10.0)
        AnchorPane.setBottomAnchor(imageTabs, 10.0)

        val topPane = VBox(menuBar, toolBar)

        val sceneContents = BorderPane(centerPane, topPane, null, null, null)

        val scene = Scene(sceneContents, 720.0, 720.0)
        scene.stylesheets.add(MainStage::class.java.getResource("/styles/application.css").toExternalForm())

        for (m in EditMode.all()) {
            if (m.shortCut != null) {
                scene.accelerators[m.shortCut] = Runnable { setCurrentEditMode(m) }
            }
        }

        stage.setScene(scene)
    }

    private fun makeColorBox(): VBox {
        // overlay and imageDisplay
        val colorPicker1 = ColorPicker(imageDisplay.colors.primaryColor.get().toFXColor())
        val colorPicker2 = ColorPicker(imageDisplay.colors.secondaryColor.get().toFXColor())

        colorPicker1.valueProperty().addListener { _, from, to ->
            if (from != to) imageDisplay.colors.setPrimaryColor(fromFXColor(to))
        }

        colorPicker2.valueProperty().addListener { _, from, to ->
            if (from != to) imageDisplay.colors.setSecondaryColor(fromFXColor(to))
        }

        imageDisplay.colors.primaryColor.addListener { _, from, to ->
            if (from != to) colorPicker1.setValue(to.toFXColor())
        }

        imageDisplay.colors.secondaryColor.addListener { _, from, to ->
            if (from != to) colorPicker2.setValue(to.toFXColor())
        }

        return VBox(
            Label("Primary color:"),
            colorPicker1,
            Label("Secondary color:"),
            colorPicker2
        )
    }

    override fun backgroundColor(): Color = imageDisplay.colors.secondaryColor.get()

    override fun askForSaveFile(image: GridCell): File? {
        val chooser = FileChooser()
        currentFolder?.let { chooser.initialDirectory = it }
        chooser.title = "Save file"
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("PNG", "*.png"))
        val result = chooser.showSaveDialog(stage)
        result?.let { currentFolder = it.getParentFile() }
        return result
    }

    override fun askForFileSaveSettings(file: File, image: GridCell): FileSaveSettings? {
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

    override fun askForFileToOpen(): File? {
        val chooser = FileChooser()
        chooser.title = "Open file"
        val result = chooser.showOpenDialog(stage)
        result?.let { currentFolder = it.getParentFile() }
        return result
    }

    override fun askForWhereToPutImage(): Pair<Int, Int>? {
        return AskForXYDialog.askForXY(
            title = "New image",
            headerText = "Please enter where it should be placed."
        )
    }

    override fun askForBlurRadius(): Int? {
        val selectedImagesCoords = model.imageGrid.selectedImages().map { it.coords }
        return DialogUtils.getValueFromDialog<Int>(
            model.imagePool,
            model.imageGrid.selectedImages(),
            "Blur images",
            "How much should the images be blurred?",
            "Radius:",
            { TextFieldRestriction.uintRestriction(it) },
            { str -> runCatching { str.toInt() }.getOrDefault(0) },
            { radius, grid -> if (radius > 0) BlurEffect(radius).action(selectedImagesCoords, grid) },
        )
    }

    override fun askForMotionBlurRadius(): Int? {
        val selectedImagesCoords = model.imageGrid.selectedImages().map { it.coords }
        return DialogUtils.getValueFromDialog(
            model.imagePool,
            model.imageGrid.selectedImages(),
            "Motion blur images",
            "How much should the images be motion blurred?",
            "Radius:",
            { TextFieldRestriction.uintRestriction(it) },
            { str -> runCatching { str.toInt() }.getOrDefault(0) },
            { radius, grid -> if (radius > 0) MotionBlurEffect(radius).action(selectedImagesCoords, grid) },
        )
    }

    override fun askForRandomNoiseColors(): Pair<Color, Color>? {
        val images = model.imageGrid.selectedImages()
        val selectedImagesCoords = model.imageGrid.selectedImages().map { it.coords }
        val loColorPicker = ColorPicker(Color.Black.toFXColor())
        val hiColorPicker = ColorPicker(Color.White.toFXColor())

        val (previewPane, updatePreview) = DialogUtils.makeImagePreviewList(images, model.imagePool)

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

    override fun askSaveBeforeClosing(images: List<GridCell>): Boolean? {
        return saveBeforeClosingAlert(images).showAndWait().orElse(null)?.let { res ->
            return when (res.buttonData) {
                ButtonBar.ButtonData.YES -> true
                ButtonBar.ButtonData.NO -> false
                else -> null
            }
        }
    }

    private fun saveBeforeClosingAlert(images: List<GridCell>): Alert {
        val (previewPane, _) = DialogUtils.makeImagePreviewList(images, model.imagePool)

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

    override fun askForFileOpenSettings(
        file: File,
        imageSize: Int,
        xCount: Int,
        yCount: Int
    ): FileOpenSettings? {
        return AskForFileOpenSettingsDialog.askForFileOpenSettings(
            imagePreview = ImagePreviewParams(file, imageSize, xCount, yCount),
            listOf(
                Pair(SimpleStorageFormat, "Simple format"),
                Pair(RecursiveStorageFormat, "Recursive format")
            ),
            0
        ) { model.fileSystem.readImage(it) }
    }

    override fun shouldReplaceImage(
        currentImage: ImageStorage,
        newImage: ImageStorage,
        location: ImagePool.SaveLocation
    ): Boolean? {
        val tri1 = model.imageGrid.findByStorage(newImage)!!
        val tri2 = model.imageGrid.findByStorage(currentImage)!!
        val (previewPane, _) = DialogUtils.makeImagePreviewList(listOf(tri1, tri2), model.imagePool)

        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Collision"
        alert.headerText = "The image already exists on the screen. Which one should be used?"
        alert.graphic = previewPane

        alert.buttonTypes.setAll(
            ButtonType("Left", ButtonBar.ButtonData.YES),
            ButtonType("Right", ButtonBar.ButtonData.NO),
            ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        )
        return alert.showAndWait().orElse(null)?.buttonData == ButtonBar.ButtonData.YES
    }

    override fun askForImageSize(): Int? {
        val dialog = TextInputDialog("32")
        dialog.title = "Image Size"
        dialog.headerText = "What should be the image size? (#rows)"
        val sizeStr = dialog.showAndWait().orElse(null)
        return sizeStr?.let { str ->
            runCatching { str.toInt() }.getOrDefault(null)?.let { if (it > 0) it else null } ?: 32
        }
    }

    override fun close() = stage.close()
}
