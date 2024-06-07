package tripaint.view.image

import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage
import tripaint.model.image.GridCell

import scalafx.scene.SnapshotParameters
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane

class TriImageForPreview(content: GridCell, previewWidth: Double) extends Pane {
  private def storage: ImageStorage = content.storage

  private val canvas: TriImageCanvas = new TriImageCanvas(previewWidth, storage.imageSize)

  content.trackChanges(onImageChanged(_))
  children.add(canvas)

  redraw()

  private def drawTriangle(coords: TriangleCoords): Unit = {
    canvas.drawTriangle(coords, storage.getColor(coords), storage)
  }

  private def redraw(): Unit = {
    canvas.clearCanvas()
    for c <- storage.allPixels do {
      drawTriangle(c)
    }
  }

  private def onImageChanged(event: GridCell.Event): Unit = {
    import GridCell.Event.*
    event match {
      case PixelChanged(coords, _, _) =>
        drawTriangle(coords)
      case ImageChangedALot =>
        redraw()
    }
  }

  def toImage(params: SnapshotParameters): Image = {
    canvas.snapshot(params, null)
  }
}

object TriImageForPreview {
  val previewSize = 64
}
