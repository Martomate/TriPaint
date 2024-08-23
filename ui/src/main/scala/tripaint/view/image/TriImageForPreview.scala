package tripaint.view.image

import tripaint.coords.TriangleCoords
import tripaint.grid.GridCell
import tripaint.image.ImageStorage

import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.layout.Pane

class TriImageForPreview(content: GridCell, previewWidth: Double) extends Pane {
  private def storage: ImageStorage = content.storage

  private val canvas: TriImageCanvas = new TriImageCanvas(previewWidth, storage.imageSize)

  content.trackChanges(onImageChanged(_))
  this.getChildren.add(canvas)

  redraw()

  private def drawTriangle(coords: TriangleCoords): Unit = {
    canvas.drawTriangle(coords, storage.getColor(coords), storage)
  }

  private def redraw(): Unit = {
    canvas.clearCanvas()
    canvas.redraw(storage)
  }

  private def onImageChanged(event: GridCell.Event): Unit = {
    import GridCell.Event.*
    event match {
      case PixelChanged(coords, _, _) =>
        drawTriangle(coords)
      case ImageChangedALot =>
        redraw()
      case _ =>
    }
  }

  def toImage(params: SnapshotParameters): Image = {
    canvas.snapshot(params, null)
  }
}

object TriImageForPreview {
  val previewSize = 64
}
