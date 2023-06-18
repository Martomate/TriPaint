package com.martomate.tripaint.control

import com.martomate.tripaint.model.coords.GridCoords
import com.martomate.tripaint.model.effects.*
import com.martomate.tripaint.model.image.{GridCell, ImageStorage}
import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.view.gui.UIAction
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewFactory, TriPaintViewListener}

class TriPaintController(val model: TriPaintModel, viewFactory: TriPaintViewFactory)
    extends TriPaintViewListener {
  val view: TriPaintView = viewFactory.createView(this, model)

  override def perform(action: UIAction): Unit = action match
    case UIAction.New =>
      for
        (x, y) <- view.askForWhereToPutImage()
        backgroundColor = Color.fromFXColor(view.backgroundColor)
        coords = GridCoords(x, y)
      do Actions.createNewImage(model.imageGrid, backgroundColor, coords)

    case UIAction.Open =>
      for
        file <- view.askForFileToOpen()
        fileOpenSettings <- view.askForFileOpenSettings(file, model.imageGrid.imageSize, 1, 1)
        (x, y) <- view.askForWhereToPutImage()
        coords = GridCoords(x, y)
      do Actions.openImage(model, file, fileOpenSettings, coords)

    case UIAction.OpenHexagon =>
      for
        file <- view.askForFileToOpen()
        fileOpenSettings <- view.askForFileOpenSettings(file, model.imageGrid.imageSize, 6, 1)
        (x, y) <- view.askForWhereToPutImage()
        coords = GridCoords(x, y)
      do Actions.openHexagon(model, file, fileOpenSettings, coords)

    case UIAction.Save =>
      Actions.save(
        model.imagePool,
        model.imageGrid.selectedImages.filter(_.changed),
        model.fileSystem
      )(view.askForSaveFile, view.askForFileSaveSettings, view)

    case UIAction.SaveAs =>
      model.imageGrid.selectedImages.foreach(im =>
        Actions.saveAs(model.imagePool, im, model.fileSystem)(
          view.askForSaveFile,
          view.askForFileSaveSettings,
          view
        )
      )

    case UIAction.Exit => if do_exit() then view.close()

    case UIAction.Undo => model.imageGrid.undo()

    case UIAction.Redo => model.imageGrid.redo()

    case UIAction.Blur =>
      for radius <- view.askForBlurRadius()
      do Actions.applyEffect(model, new BlurEffect(radius))

    case UIAction.MotionBlur =>
      for radius <- view.askForMotionBlurRadius()
      do Actions.applyEffect(model, new MotionBlurEffect(radius))

    case UIAction.RandomNoise =>
      for (lo, hi) <- view.askForRandomNoiseColors()
      do Actions.applyEffect(model, new RandomNoiseEffect(lo, hi))

    case UIAction.Scramble => Actions.applyEffect(model, ScrambleEffect)

    case _ =>

  override def requestExit(): Boolean = do_exit()

  override def requestImageRemoval(image: GridCell): Unit =
    var abortRemoval = false
    if image.changed then
      view.askSaveBeforeClosing(Seq(image)) match
        case Some(shouldSave) =>
          if (
            shouldSave && !Actions.save(model.imagePool, Seq(image), model.fileSystem)(
              view.askForSaveFile,
              view.askForFileSaveSettings,
              view
            )
          ) abortRemoval = true
        case None => abortRemoval = true

    if (!abortRemoval) model.imageGrid -= image.coords

  private def do_exit(): Boolean =
    model.imageGrid.changedImages match
      case Seq() => true
      case images =>
        view.askSaveBeforeClosing(images) match
          case Some(shouldSave) =>
            if shouldSave then
              Actions.save(model.imagePool, images, model.fileSystem)(
                view.askForSaveFile,
                view.askForFileSaveSettings,
                view
              )
            else true
          case None => false
}
