package tripaint.view.gui

import tripaint.grid.GridCell
import tripaint.image.ImagePool
import tripaint.view.image.TriImageForPreview

import scalafx.geometry.Pos
import scalafx.scene.control.{Button, ToggleButton}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane

object ImageTabPane {
  def apply(
      image: GridCell,
      requestImageRemoval: GridCell => Unit,
      imagePool: ImagePool
  ): StackPane = {
    val preview = new TriImageForPreview(image, TriImageForPreview.previewSize)

    val closeButton = {
      val b = new Button
      b.text = "X"
      b.visible = false
      b.alignmentInParent = Pos.TopRight
      b.onAction = _ => requestImageRemoval(image)
      b
    }

    val previewButton = {
      val b = new ToggleButton
      b.graphic = preview
      b.tooltip = TriImageTooltip.fromImagePool(image, imagePool.locationOf)
      b.selected <==> image.editableProperty
      b
    }

    val starView: ImageView = makeStarView(image)

    val stackPane = {
      val p = new StackPane
      p.children.addAll(previewButton, closeButton, starView)
      p.onMouseEntered = _ => {
        closeButton.visible = true
      }
      p.onMouseExited = _ => {
        closeButton.visible = false
      }
      p
    }

    stackPane
  }

  private def makeStarView(image: GridCell): ImageView = {
    val star: ImageView = new ImageView
    star.image = new Image("/icons/star.png")
    star.alignmentInParent = Pos.TopLeft
    star.mouseTransparent = true
    star.visible <== image.changedProperty
    star
  }
}
