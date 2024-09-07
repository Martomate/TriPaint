package tripaint.app

import javafx.application.Platform
import javafx.stage.Stage
import tripaint.coords.GridCoords
import tripaint.effects.*
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.view.TriPaintView
import tripaint.view.TriPaintViewListener
import tripaint.view.gui.UIAction

class TriPaintController(stage: Stage, private val fileSystem: FileSystem) : TriPaintViewListener {
    private val imagePool: ImagePool = ImagePool()
    private val imageGrid: ImageGrid = ImageGrid(-1)

    private val view: TriPaintView = MainStage(this, fileSystem, imagePool, imageGrid, stage)

    init {
        Platform.runLater {
            imageGrid.setImageSizeIfEmpty(view.askForImageSize() ?: 32)
        }
    }

    override fun perform(action: UIAction) {
        when (action) {
            UIAction.New -> {
                view.askForWhereToPutImage()?.let { (x, y) ->
                    val backgroundColor = view.backgroundColor()
                    val coords = GridCoords.from(x, y)
                    Actions.createNewImage(imageGrid, backgroundColor, coords)
                }
            }

            UIAction.Open -> {
                view.askForFileToOpen()?.let { file ->
                    view.askForFileOpenSettings(file, imageGrid.imageSize, 1, 1)?.let { fileOpenSettings ->
                        view.askForWhereToPutImage()?.let { (x, y) ->
                            val coords = GridCoords.from(x, y)
                            Actions.openImage(fileSystem, imagePool, imageGrid, file, fileOpenSettings, coords)
                        }
                    }
                }
            }

            UIAction.OpenHexagon -> {
                view.askForFileToOpen()?.let { file ->
                    view.askForFileOpenSettings(file, imageGrid.imageSize, 6, 1)?.let { fileOpenSettings ->
                        view.askForWhereToPutImage()?.let { (x, y) ->
                            val coords = GridCoords.from(x, y)
                            Actions.openHexagon(fileSystem, imagePool, imageGrid, file, fileOpenSettings, coords)
                        }
                    }
                }
            }

            UIAction.Save -> {
                Actions.save(
                    imageGrid,
                    imagePool,
                    imageGrid.selectedImages().filter { it.changed },
                    fileSystem,
                    { image -> view.askForSaveFile(image) },
                    { file, image -> view.askForFileSaveSettings(file, image) },
                    view,
                )
            }

            UIAction.SaveAs -> {
                for (im in imageGrid.selectedImages()) {
                    Actions.saveAs(
                        imageGrid,
                        imagePool,
                        im,
                        fileSystem,
                        { image -> view.askForSaveFile(image) },
                        { file, image -> view.askForFileSaveSettings(file, image) },
                        view
                    )
                }
            }

            UIAction.Exit -> {
                if (doExit()) {
                    view.close()
                }
            }

            UIAction.Undo -> {
                imageGrid.undo()
            }

            UIAction.Redo -> {
                imageGrid.redo()
            }

            UIAction.Blur -> {
                view.askForBlurRadius()?.let { radius ->
                    Actions.applyEffect(imageGrid, BlurEffect(radius))
                }
            }

            UIAction.MotionBlur -> {
                view.askForMotionBlurRadius()?.let { radius ->
                    Actions.applyEffect(imageGrid, MotionBlurEffect(radius))
                }
            }

            UIAction.RandomNoise -> {
                view.askForRandomNoiseColors()?.let { (lo, hi) ->
                    Actions.applyEffect(imageGrid, RandomNoiseEffect(lo, hi))
                }
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

    override fun requestExit(): Boolean = doExit()

    override fun requestImageRemoval(image: GridCell) {
        var abortRemoval = false
        if (image.changed) {
            val shouldSave = view.askSaveBeforeClosing(listOf(image))
            if (shouldSave != null) {
                if (shouldSave && !Actions.save(
                        imageGrid,
                        imagePool,
                        listOf(image),
                        fileSystem,
                        { view.askForSaveFile(it) },
                        { file, im -> view.askForFileSaveSettings(file, im) },
                        view,
                    )
                ) {
                    abortRemoval = true
                }
            } else {
                abortRemoval = true
            }
        }

        if (!abortRemoval) {
            imageGrid.remove(image.coords)
        }
    }

    private fun doExit(): Boolean {
        val images = imageGrid.changedImages()
        if (images.isEmpty()) return true

        val shouldSave = view.askSaveBeforeClosing(images)
        return if (shouldSave != null) {
            if (shouldSave) {
                Actions.save(
                    imageGrid, imagePool, images, fileSystem,
                    { image -> view.askForSaveFile(image) },
                    { file, image -> view.askForFileSaveSettings(file, image) },
                    view
                )
            } else true
        } else false
    }
}
