package com.martomate.tripaint.view.gui

import com.martomate.tripaint.control.TriPaintController
import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.TriPaintViewListener
import com.martomate.tripaint.view.image.{TriImage, TriImageForPreview}
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, ToggleButton}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane

class ImageTabPane(val image: ImageContent, control: TriPaintViewListener, model: TriPaintModel) extends StackPane {
  private val preview = new TriImageForPreview(image, TriImage.previewSize)

  private val closeButton = new Button {
    text = "X"
    visible = false
    alignmentInParent = Pos.TopRight

    onAction = e => {
      control.requestImageRemoval(image)
    }
  }

  private val previewButton = new ToggleButton {
    this.graphic = preview
    this.tooltip = new TriImageTooltip(image, model.imagePool)
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
  def apply(image: ImageContent, control: TriPaintViewListener, model: TriPaintModel): ImageTabPane = {
    new ImageTabPane(image, control, model)
  }
}
