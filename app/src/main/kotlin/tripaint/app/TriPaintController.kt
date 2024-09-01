package tripaint.app

import tripaint.coords.GridCoords
import tripaint.effects.*
import tripaint.grid.GridCell
import tripaint.view.TriPaintView
import tripaint.view.TriPaintViewListener
import tripaint.view.gui.UIAction

class TriPaintController(val model: TriPaintModel, viewFactory: TriPaintViewFactory) : TriPaintViewListener {
    val view: TriPaintView = viewFactory.createView(this, model)

    override fun perform(action: UIAction) {
        when (action) {
            UIAction.New -> {
                view.askForWhereToPutImage()?.let { (x, y) ->
                    val backgroundColor = view.backgroundColor()
                    val coords = GridCoords.from(x, y)
                    Actions.createNewImage(model.imageGrid, backgroundColor, coords)
                }
            }

            UIAction.Open -> {
                view.askForFileToOpen()?.let { file ->
                    view.askForFileOpenSettings(file, model.imageGrid.imageSize, 1, 1)?.let { fileOpenSettings ->
                        view.askForWhereToPutImage()?.let { (x, y) ->
                            val coords = GridCoords.from(x, y)
                            Actions.openImage(model, file, fileOpenSettings, coords)
                        }
                    }
                }
            }

            UIAction.OpenHexagon -> {
                view.askForFileToOpen()?.let { file ->
                    view.askForFileOpenSettings(file, model.imageGrid.imageSize, 6, 1)?.let { fileOpenSettings ->
                        view.askForWhereToPutImage()?.let { (x, y) ->
                            val coords = GridCoords.from(x, y)
                            Actions.openHexagon(model, file, fileOpenSettings, coords)
                        }
                    }
                }
            }

            UIAction.Save -> {
                Actions.save(
                    model.imageGrid,
                    model.imagePool,
                    model.imageGrid.selectedImages().filter { it.changed },
                    model.fileSystem,
                    { image -> view.askForSaveFile(image) },
                    { file, image -> view.askForFileSaveSettings(file, image) },
                    view,
                )
            }

            UIAction.SaveAs -> {
                for (im in model.imageGrid.selectedImages()) {
                    Actions.saveAs(
                        model.imageGrid,
                        model.imagePool,
                        im,
                        model.fileSystem,
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
                model.imageGrid.undo()
            }

            UIAction.Redo -> {
                model.imageGrid.redo()
            }

            UIAction.Blur -> {
                view.askForBlurRadius()?.let { radius ->
                    Actions.applyEffect(model, BlurEffect(radius))
                }
            }

            UIAction.MotionBlur -> {
                view.askForMotionBlurRadius()?.let { radius ->
                    Actions.applyEffect(model, MotionBlurEffect(radius))
                }
            }

            UIAction.RandomNoise -> {
                view.askForRandomNoiseColors()?.let { (lo, hi) ->
                    Actions.applyEffect(model, RandomNoiseEffect(lo, hi))
                }
            }

            UIAction.Scramble -> {
                Actions.applyEffect(model, ScrambleEffect)
            }

            UIAction.Cell -> {
                Actions.applyEffect(model, CellEffect())
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
                        model.imageGrid,
                        model.imagePool,
                        listOf(image),
                        model.fileSystem,
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
            model.imageGrid.remove(image.coords)
        }
    }

    private fun doExit(): Boolean {
        val images = model.imageGrid.changedImages()
        if (images.isEmpty()) return true

        val shouldSave = view.askSaveBeforeClosing(images)
        return if (shouldSave != null) {
            if (shouldSave) {
                Actions.save(
                    model.imageGrid, model.imagePool, images, model.fileSystem,
                    { image -> view.askForSaveFile(image) },
                    { file, image -> view.askForFileSaveSettings(file, image) },
                    view
                )
            } else true
        } else false
    }
}
