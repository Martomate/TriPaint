package com.martomate.tripaint.control

import com.martomate.tripaint.control.action.*
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.effects.{
  BlurEffect,
  MotionBlurEffect,
  RandomNoiseEffect,
  ScrambleEffect
}
import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.gui.UIAction
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewFactory, TriPaintViewListener}

class TriPaintController(val model: TriPaintModel, viewFactory: TriPaintViewFactory)
    extends TriPaintViewListener {
  val view: TriPaintView = viewFactory.createView(this, model)

  override def perform(action: UIAction): Unit = action match
    case UIAction.New =>
      for where <- view.askForWhereToPutImage()
      do
        new NewAction(
          model.imageGrid,
          Color.fromFXColor(view.backgroundColor),
          TriImageCoords(where._1, where._2)
        ).perform()
    case UIAction.Open =>
      for
        file <- view.askForFileToOpen()
        fileOpenSettings <- view.askForFileOpenSettings(file, model.imageGrid.imageSize, 1, 1)
        where <- view.askForWhereToPutImage()
        coords = TriImageCoords(where._1, where._2)
      do new OpenAction(model, file, fileOpenSettings, coords).perform()
    case UIAction.OpenHexagon =>
      for
        file <- view.askForFileToOpen()
        fileOpenSettings <- view.askForFileOpenSettings(file, model.imageGrid.imageSize, 6, 1)
        where <- view.askForWhereToPutImage()
        coords = TriImageCoords(where._1, where._2)
      do new OpenHexagonAction(model, file, fileOpenSettings, coords).perform()
    case UIAction.Save =>
      new SaveAction(model, view.askForSaveFile, view.askForFileSaveSettings, view).perform()
    case UIAction.SaveAs =>
      new SaveAsAction(model, view.askForSaveFile, view.askForFileSaveSettings, view).perform()
    case UIAction.Exit =>
      new ExitAction(
        model,
        view.askForSaveFile,
        view.askForFileSaveSettings,
        view,
        view.askSaveBeforeClosing,
        view.close
      ).perform()
    case UIAction.Undo => model.imageGrid.images.foreach(_.undo())
    case UIAction.Redo => model.imageGrid.images.foreach(_.redo())
    case UIAction.Blur =>
      for radius <- view.askForBlurRadius()
      do new EffectAction(model, new BlurEffect(radius)).perform()
    case UIAction.MotionBlur =>
      for radius <- view.askForMotionBlurRadius()
      do new EffectAction(model, new MotionBlurEffect(radius)).perform()
    case UIAction.RandomNoise =>
      for (lo, hi) <- view.askForRandomNoiseColors()
      do new EffectAction(model, new RandomNoiseEffect(lo, hi)).perform()
    case UIAction.Scramble => new EffectAction(model, ScrambleEffect).perform()
    case _                 =>

  override def requestExit(): Boolean = new ExitAction(
    model,
    view.askForSaveFile,
    view.askForFileSaveSettings,
    view,
    view.askSaveBeforeClosing,
    view.close
  ).do_exit()
  override def requestImageRemoval(image: ImageContent): Unit =
    new RemoveImageAction(
      image,
      model,
      view.askForSaveFile,
      view.askForFileSaveSettings,
      view,
      view.askSaveBeforeClosing
    ).perform()
}
// dispatcher, store, view, action, ...
// model, view, intent
