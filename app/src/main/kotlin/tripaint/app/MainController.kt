package tripaint.app

import javafx.application.Platform
import javafx.stage.Stage
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.effects.*
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.grid.ImageSaveCollisionHandler
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.util.Resource
import tripaint.view.EditMode
import tripaint.view.TriPaintViewListener
import tripaint.view.gui.UIAction

class MainController(
    private val stage: Stage,
    private val fileSystem: FileSystem
) : TriPaintViewListener, ImageSaveCollisionHandler {

    private val imagePool: ImagePool = ImagePool()
    private val imageGrid: ImageGrid = ImageGrid(-1)

    private val currentEditMode = Resource.createResource(EditMode.Draw)
    private val primaryColor = Resource.createResource(Color.Black)
    private val secondaryColor = Resource.createResource(Color.White)

    private val previewStage: Stage = Stage()

    init {
        stage.title = "TriPaint"
        stage.setOnCloseRequest { e ->
            if (!this.doExit()) {
                e.consume()
            } else {
                previewStage.close()
            }
        }
        stage.scene = MainScene.create(
            this, imagePool, imageGrid,
            currentEditMode, primaryColor, secondaryColor, this::requestImageRemoval
        )

        previewStage.title = "TriPaint preview"
        previewStage.scene = PreviewScene.create(imageGrid)

        Platform.runLater {
            imageGrid.setImageSizeIfEmpty(Dialogs.askForImageSize() ?: 32)
        }
    }

    override fun perform(action: UIAction) {
        when (action) {
            UIAction.New -> {
                val (x, y) = Dialogs.askForWhereToPutImage() ?: return

                val backgroundColor = secondaryColor.value
                val coords = GridCoords.from(x, y)
                Actions.createNewImage(imageGrid, backgroundColor, coords)
            }

            UIAction.Open -> {
                val file = Dialogs.askForFileToOpen(stage) ?: return
                val fileOpenSettings =
                    Dialogs.askForFileOpenSettings(fileSystem, file, imageGrid.imageSize, 1, 1) ?: return
                val (x, y) = Dialogs.askForWhereToPutImage() ?: return

                val coords = GridCoords.from(x, y)
                Actions.openImage(fileSystem, imagePool, imageGrid, file, fileOpenSettings, coords)
            }

            UIAction.OpenHexagon -> {
                val file = Dialogs.askForFileToOpen(stage) ?: return
                val fileOpenSettings =
                    Dialogs.askForFileOpenSettings(fileSystem, file, imageGrid.imageSize, 6, 1) ?: return
                val (x, y) = Dialogs.askForWhereToPutImage() ?: return

                val coords = GridCoords.from(x, y)
                Actions.openHexagon(fileSystem, imagePool, imageGrid, file, fileOpenSettings, coords)
            }

            UIAction.Save -> {
                val changedImages = imageGrid.selectedImages().filter { it.changed }
                Actions.save(imageGrid, imagePool, changedImages, fileSystem, {Dialogs.askForSaveFile(stage, it)}, Dialogs::askForFileSaveSettings, this)
            }

            UIAction.SaveAs -> {
                for (im in imageGrid.selectedImages()) {
                    Actions.saveAs(imageGrid, imagePool, im, fileSystem, {Dialogs.askForSaveFile(stage, it)}, Dialogs::askForFileSaveSettings, this)
                }
            }

            UIAction.Exit -> {
                if (doExit()) {
                    stage.close()
                }
            }

            UIAction.Undo -> {
                imageGrid.undo()
            }

            UIAction.Redo -> {
                imageGrid.redo()
            }

            UIAction.ShowPreview -> {
                previewStage.show()
            }

            UIAction.Blur -> {
                val radius = Dialogs.askForBlurRadius(imageGrid, imagePool) ?: return

                Actions.applyEffect(imageGrid, BlurEffect(radius))
            }

            UIAction.MotionBlur -> {
                val radius = Dialogs.askForMotionBlurRadius(imageGrid, imagePool) ?: return

                Actions.applyEffect(imageGrid, MotionBlurEffect(radius))
            }

            UIAction.RandomNoise -> {
                val (lo, hi) = Dialogs.askForRandomNoiseColors(imageGrid, imagePool) ?: return

                Actions.applyEffect(imageGrid, RandomNoiseEffect(lo, hi))
            }

            UIAction.Scramble -> {
                Actions.applyEffect(imageGrid, ScrambleEffect)
            }

            UIAction.Cell -> {
                Actions.applyEffect(imageGrid, CellEffect())
            }

            else -> {}
        }
    }

    private fun requestImageRemoval(image: GridCell) {
        if (shouldRemoveImage(image)) {
            imageGrid.remove(image.coords)
        }
    }

    private fun shouldRemoveImage(image: GridCell): Boolean {
        if (!image.changed) return true

        val shouldSave = Dialogs.askSaveBeforeClosing(imagePool, listOf(image)) ?: return false
        if (shouldSave) {
            val didSave = Actions.save(
                imageGrid,
                imagePool,
                listOf(image),
                fileSystem,
                {Dialogs.askForSaveFile(stage, it)},
                Dialogs::askForFileSaveSettings,
                this,
            )
            if (!didSave) return false
        }
        return true
    }

    private fun doExit(): Boolean {
        val images = imageGrid.changedImages()
        if (images.isEmpty()) return true

        val shouldSave = Dialogs.askSaveBeforeClosing(imagePool, images) ?: return false
        if (!shouldSave) return true

        return Actions.save(
            imageGrid, imagePool, images, fileSystem,
            {Dialogs.askForSaveFile(stage, it)},
            Dialogs::askForFileSaveSettings,
            this
        )
    }

    override fun shouldReplaceImage(
        currentImage: ImageStorage,
        newImage: ImageStorage,
        location: ImagePool.SaveLocation
    ): Boolean? {
        return Dialogs.shouldReplaceImage(imageGrid, imagePool, currentImage, newImage, location)
    }
}
