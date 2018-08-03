package com.martomate.tripaint.gui

import com.martomate.tripaint.TriPaintController
import com.martomate.tripaint.image.{TriImage, TriImagePreview}
import scalafx.geometry.Pos
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control.{Button, ToggleButton}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane

class ImageTabPane(val image: TriImage, control: TriPaintController) extends StackPane {
  private val preview = new TriImagePreview(TriImage.previewSize, image)

  private val closeButton = new Button {
    text = "X"
    visible = false
    alignmentInParent = Pos.TopRight

    onAction = e => {
      if (image.changed) {
        control.saveBeforeClosing(image) match {
          case Some(shouldSave) =>
            if (shouldSave && !control.save(image)) e.consume()
          case None => e.consume()
        }
      }

      if (!e.isConsumed) {
        control.imageGrid -= image.coords
      }
    }
  }

  private val previewButton = new ToggleButton {
    this.graphic = preview
    this.tooltip = new TriImageTooltip(image.content, control.imagePool)
    this.selected <==> image.editableProperty

    this.onMouseClicked = e => {
      control.imageGrid.selectImage(image, !e.isControlDown)
    }
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
    star.visible <== image.changedProperty
    star
  }
}

object ImageTabPane {
  def apply(image: TriImage, control: TriPaintController): ImageTabPane = {
    new ImageTabPane(image, control)
  }
}