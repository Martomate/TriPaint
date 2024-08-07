package tripaint.view.gui

import tripaint.grid.GridCell
import tripaint.image.ImagePool
import tripaint.view.image.TriImageForPreview

import javafx.geometry.Pos
import javafx.scene.control.{Button, ToggleButton}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.StackPane

object ImageTabPane {
  def apply(
      image: GridCell,
      requestImageRemoval: GridCell => Unit,
      imagePool: ImagePool
  ): StackPane = {
    val preview = new TriImageForPreview(image, TriImageForPreview.previewSize)

    val closeButton = {
      val b = new Button
      b.setText("X")
      b.setVisible(false)
      b.setOnAction(_ => requestImageRemoval(image))
      b
    }

    val previewButton = {
      val b = new ToggleButton
      b.setGraphic(preview)
      b.setTooltip(TriImageTooltip.fromImagePool(image, imagePool.locationOf))

      image.trackChanges {
        case GridCell.Event.StateUpdated(editable, _) =>
          b.setSelected(editable)
        case _ =>
      }
      b.selectedProperty.addListener { (_, _, selected) =>
        image.setEditable(selected)
      }
      b.setSelected(image.editable)

      b
    }

    val starView: ImageView = makeStarView(image)

    val stackPane = {
      val p = new StackPane(previewButton, closeButton, starView)
      StackPane.setAlignment(closeButton, Pos.TOP_RIGHT)
      StackPane.setAlignment(starView, Pos.TOP_LEFT)
      p.setOnMouseEntered(_ => {
        closeButton.setVisible(true)
      })
      p.setOnMouseExited(_ => {
        closeButton.setVisible(false)
      })
      p
    }

    stackPane
  }

  private def makeStarView(image: GridCell): ImageView = {
    val star: ImageView = new ImageView
    star.setImage(new Image("/icons/star.png"))
    star.setMouseTransparent(true)

    image.trackChanges {
      case GridCell.Event.StateUpdated(_, changed) =>
        star.setVisible(changed)
      case _ =>
    }
    star.setVisible(image.changed)

    star
  }
}
