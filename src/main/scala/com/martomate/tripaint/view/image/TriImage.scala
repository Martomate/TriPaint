package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.content.{
  CumulativeImageChange,
  ImageChangeListener,
  ImageContent
}
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.storage.ImageStorage
import javafx.scene.input.{MouseEvent, ScrollEvent}
import scalafx.beans.property.ReadOnlyBooleanProperty
import scalafx.scene.layout.Pane

class TriImage(val content: ImageContent, init_zoom: Double) extends ImageChangeListener:
  val pane: Pane = new Pane()

  private val indexMap = new IndexMap(storage.imageSize)

  private val canvas: TriImageCanvas =
    new TriImageCanvas((storage.imageSize * 2 + 1) * init_zoom, storage.imageSize)

  private val cumulativeImageChange = new CumulativeImageChange

  content.addListener(onImageChanged)
  pane.children.add(canvas)

  if content.coords.x % 2 != 0 then canvas.rotate() += 180
  updateCanvasSize(init_zoom)

  redraw()

  private def storage: ImageStorage = content.storage

  private def panX(zoom: Double) = content.coords.centerX * (storage.imageSize * 2 + 1) * zoom
  private def panY(zoom: Double) = content.coords.centerY * (storage.imageSize * 2 + 1) * zoom

  def onStoppedDrawing(): Unit =
    content.undoManager.append(cumulativeImageChange.done("draw", content))

  def onZoom(zoom: Double): Unit =
    updateCanvasSize(zoom)
    redraw()

  def drawAt(coords: TriangleCoords, color: Color): Unit =
    if storage.contains(coords) then
      cumulativeImageChange.addChange(coords, storage(coords), color)
      storage(coords) = color

  /** @param x
    *   the x coordinate in scene space
    * @param y
    *   the y coordinate in scene space
    * @return
    *   the `TriangleCoords` of the point if it lies inside the triangle, null otherwise
    */
  def coordsAt(x: Double, y: Double): TriangleCoords =
    val pt = canvas.sceneToLocal(x, y)
    indexMap.coordsAt(pt.getX / canvas.width(), pt.getY / canvas.height())

  private def updateCanvasSize(zoom: Double): Unit =
    canvas.setCanvasSize((storage.imageSize * 2 + 1) * zoom)
    canvas.setCanvasLocationUsingCenter(panX(zoom), panY(zoom))

  private def drawTriangle(coords: TriangleCoords): Unit =
    canvas.drawTriangle(coords, storage(coords), storage)

  private def redraw(): Unit =
    canvas.clearCanvas()
    canvas.redraw(storage)

  override def onImageChanged(event: ImageContent.Event): Unit =
    import ImageContent.Event.*
    event match
      case PixelChanged(coords, _, _) =>
        drawTriangle(coords)
      case ImageChangedALot =>
        redraw()
