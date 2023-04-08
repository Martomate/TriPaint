package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.storage.ImageStorage
import scalafx.scene.SnapshotParameters
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane

class TriImageForPreview(content: ImageContent, previewWidth: Double) extends Pane:
  private def storage: ImageStorage = content.storage

  private val canvas: TriImageCanvas = new TriImageCanvas(previewWidth, storage.imageSize)

  content.trackChanges(onImageChanged _)
  children.add(canvas)

  redraw()

  private def drawTriangle(coords: TriangleCoords): Unit =
    canvas.drawTriangle(coords, storage(coords), storage)

  private def redraw(): Unit =
    canvas.clearCanvas()
    for c <- storage.allPixels do drawTriangle(c)

  private def onImageChanged(event: ImageContent.Event): Unit =
    import ImageContent.Event.*
    event match
      case PixelChanged(coords, _, _) =>
        drawTriangle(coords)
      case ImageChangedALot =>
        redraw()

  def toImage(params: SnapshotParameters): Image = canvas.snapshot(params, null)

object TriImageForPreview:
  val previewSize = 64
