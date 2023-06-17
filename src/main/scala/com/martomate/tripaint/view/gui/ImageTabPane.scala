package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.ImagePool
import com.martomate.tripaint.model.image.content.GridCell
import com.martomate.tripaint.view.TriPaintViewListener
import com.martomate.tripaint.view.image.TriImageForPreview
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, ToggleButton}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane

object ImageTabPane:
  def apply(
             image: GridCell,
             requestImageRemoval: GridCell => Unit,
             imagePool: ImagePool
  ): StackPane =
    val preview = new TriImageForPreview(image, TriImageForPreview.previewSize)

    val closeButton = new Button {
      text = "X"
      visible = false
      alignmentInParent = Pos.TopRight

      onAction = _ => requestImageRemoval(image)
    }

    val previewButton = new ToggleButton {
      this.graphic = preview
      this.tooltip = TriImageTooltip.fromImagePool(image, imagePool.locationOf)
      this.selected <==> image.editableProperty
    }

    val starView: ImageView = makeStarView(image)

    val stackPane = new StackPane

    stackPane.children.addAll(previewButton, closeButton, starView)

    stackPane.onMouseEntered = _ => closeButton.visible = true
    stackPane.onMouseExited = _ => closeButton.visible = false

    stackPane

  private def makeStarView(image: GridCell): ImageView =
    val star: ImageView = new ImageView
    star.image = new Image("/icons/star.png")
    star.alignmentInParent = Pos.TopLeft
    star.mouseTransparent = true
    star.visible <== image.changedProperty
    star
