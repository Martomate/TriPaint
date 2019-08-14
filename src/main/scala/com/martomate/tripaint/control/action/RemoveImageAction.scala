package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.view.TriPaintView

class RemoveImageAction(image: ImageContent) extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    var abortRemoval = false
    if (image.changeTracker.changed) {
      saveBeforeClosing(view, image) match {
        case Some(shouldSave) =>
          if (shouldSave && !save(model, view, image)) abortRemoval = true
        case None => abortRemoval = true
      }
    }

    if (!abortRemoval) {
      removeImageAt(model, image.coords)
    }
  }

  private def removeImageAt(model: TriPaintModel, coords: TriImageCoords): Unit = model.imageGrid -= coords
}
