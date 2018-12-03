package com.martomate.tripaint.view.gui

import com.martomate.tripaint.control.TriPaintController
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.view.image.{TriImage, TriImageForPreview}
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, ToggleButton}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane

class ImageTabPane(val image: ImageContent, control: TriPaintController) extends StackPane {
  private val preview = new TriImageForPreview(image, TriImage.previewSize)

  private val closeButton = new Button {
    text = "X"
    visible = false
    alignmentInParent = Pos.TopRight

    onAction = e => {
      if (image.changeTracker.changed) {
        control.saveBeforeClosing(image) match {
          case Some(shouldSave) =>
            if (shouldSave && !control.save(image)) e.consume()
          case None => e.consume()
        }
      }

      if (!e.isConsumed) {
        control.removeImageAt(image.coords)
      }
    }
  }

  private val previewButton = new ToggleButton {
    this.graphic = preview
    this.tooltip = new TriImageTooltip(image, control.model.imagePool)
    this.selected <==> image.editableProperty
  }

  private val starView: ImageView = makeStarView()

  children add previewButton
  children add closeButton
  children add starView

  onMouseEntered = _ => closeButton.visible = true
  onMouseExited  = _ => closeButton.visible = false

  private def makeStarView(): ImageView = {
    val star: ImageView = new ImageView
    star.image = new Image("/icons/star.png")
    star.alignmentInParent = Pos.TopLeft
    star.mouseTransparent = true
    star.visible <== image.changeTracker.changedProperty
    star
  }
}

object ImageTabPane {
  def apply(image: ImageContent, control: TriPaintController): ImageTabPane = {
    new ImageTabPane(image, control)
  }
}