package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.{
  FloodFillSearcher,
  ImageGrid,
  ImageGridChange,
  ImageGridColorLookup
}
import com.martomate.tripaint.model.coords.{GridCoords, PixelCoords, TriangleCoords}
import com.martomate.tripaint.model.image.ImageChange
import com.martomate.tripaint.view.EditMode

import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.paint
import javafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

import scala.collection.mutable

class ImageGridPane(imageGrid: ImageGrid) extends Pane {
  private var xScroll: Double = 0
  private var yScroll: Double = 0
  private var zoom: Double = 1d

  private object drag {
    var x: Double = -1
    var y: Double = -1
  }

  object colors {
    val primaryColor: ObjectProperty[paint.Color] = ObjectProperty(Color.Black)

    def primaryColor_=(col: Color): Unit = {
      primaryColor.value = col
    }

    val secondaryColor: ObjectProperty[paint.Color] = ObjectProperty(Color.White)

    def secondaryColor_=(col: Color): Unit = {
      secondaryColor.value = col
    }
  }

  private val gridSearcher: FloodFillSearcher = new FloodFillSearcher(
    new ImageGridColorLookup(imageGrid)
  )

  private val imageMap: mutable.Map[GridCoords, TriImage] = mutable.Map.empty

  private val canvas = new ImageGridCanvas(imageGrid)
  children.add(canvas)

  imageGrid.trackChanges(this.onImageGridEvent _)

  private def triImages: Seq[TriImage] = {
    for im <- imageGrid.images yield imageMap(im.coords)
  }

  private def imageAt(x: Double, y: Double): Option[(TriImage, PixelCoords)] = {
    val coords = PixelCoords(canvas.coordsAt(x, y), imageGrid.imageSize)
    triImages.find(_.content.coords == coords.image).map((_, coords))
  }

  onMouseDragged = e => {
    if !e.isConsumed then {
      val xDiff = e.getX - drag.x
      val yDiff = e.getY - drag.y

      drag.x = e.getX
      drag.y = e.getY

      EditMode.currentMode match {
        case EditMode.Organize => // TODO: implement scale and rotation if (x, y) is close enough to a corner
          setScroll(xScroll + xDiff, yScroll + yDiff)
        case _ =>
          val dist = Math.hypot(xDiff, yDiff) / zoom

          val steps = 4 * dist.toInt + 1
          for i <- 1 to steps do {
            val xx = e.getSceneX + xDiff / steps * (i - steps)
            val yy = e.getSceneY + yDiff / steps * (i - steps)

            for {
              (image, coords) <- imageAt(xx, yy)
              if image.content.editable
            } do mousePressedAt(coords, e, dragged = true)
          }
      }
    }
  }

  onMousePressed = e => {
    if !e.isConsumed then {
      drag.x = e.getX
      drag.y = e.getY

      for {
        (image, coords) <- imageAt(e.getSceneX, e.getSceneY)
        if image.content.editable
      } do {
        mousePressedAt(coords, e, dragged = false)
      }
    }
  }

  onMouseReleased = e => {
    if !e.isConsumed
    then {
      val changes = mutable.Map.empty[GridCoords, ImageChange]
      for im <- triImages.reverse
      do {
        val change = im.stopDrawing()
        change.undo()
        changes(im.content.coords) = change
      }

      imageGrid.performChange(new ImageGridChange(changes.toMap))
    }
  }

  onScroll = e => {
    val (dx, dy) = (e.getDeltaX, e.getDeltaY)

    if e.isControlDown then {
      val factor = Math.min(Math.max(Math.exp(e.getDeltaY * 0.01), 1.0 / 32 / zoom), 32 / zoom)
      zoom *= factor
      canvas.setScale(zoom)
      setScroll(xScroll * factor, yScroll * factor)
    } else {
      setScroll(xScroll + dx, yScroll + dy)
    }

    if e.isControlDown then {
      for im <- triImages.reverse do {
        im.onZoom(zoom)
      }
    }

    canvas.redraw()
  }

  private def setScroll(sx: Double, sy: Double): Unit = {
    xScroll = sx
    yScroll = sy
    relocateChildren()
    canvas.setDisplacement(xScroll, yScroll)
  }

  private def mousePressedAt(coords: PixelCoords, e: MouseEvent, dragged: Boolean): Unit = {
    val colorToUse = e.getButton match {
      case MouseButton.PRIMARY   => Some(colors.primaryColor)
      case MouseButton.SECONDARY => Some(colors.secondaryColor)
      case _                     => None
    }

    for {
      image <- imageGrid(coords.image)
      color <- colorToUse
    } do {
      EditMode.currentMode match {
        case EditMode.Draw =>
          imageMap(image.coords).drawAt(coords.pix, new Color(color()))
        case EditMode.Fill =>
          fill(coords, new Color(color()))
        case EditMode.PickColor =>
          color() = image.storage.getColor(coords.pix).toFXColor
        case _ =>
      }
    }

    val pt = canvas.sceneToLocal(e.getSceneX, e.getSceneY)
    val (sx, sy) = (pt.x, pt.y)

    val (w, h) = (canvas.width().toInt, canvas.height().toInt)

    val startX = Math.min(Math.max((sx - 3 * zoom).toInt, 0), w - 1)
    val startY = Math.min(Math.max((sy - 3 * zoom).toInt, 0), h - 1)
    val endX = Math.min(Math.max((sx + 3 * zoom + 1).toInt, 0), w - 1)
    val endY = Math.min(Math.max((sy + 3 * zoom + 1).toInt, 0), h - 1)

    if endX >= startX && endY >= startY then {
      canvas.redraw(startX, startY, endX - startX, endY - startY)
    }
  }

  private def fill(coords: PixelCoords, color: Color): Unit = {
    for image <- imageGrid(coords.image) do {
      val referenceColor = imageMap(image.coords).content.storage.getColor(coords.pix)
      val places = gridSearcher
        .search(coords.toGlobal(imageGrid.imageSize), (_, col) => col == referenceColor)
        .map(p => PixelCoords(p, imageGrid.imageSize))

      for {
        p <- places
        im <- imageGrid(p.image)
      } do {
        imageMap(im.coords).drawAt(p.pix, color)
      }
    }

    canvas.redraw()
  }

  this.width.onChange(updateSize())
  this.height.onChange(updateSize())

  private def updateSize(): Unit = {
    this.clip() = new Rectangle(0, 0, width(), height())
    relocateChildren()
    canvas.width = width()
    canvas.height = height()
    canvas.redraw()
  }

  private def relocateChildren(): Unit = {
    for im <- imageGrid.images do {
      relocateImage(im.coords)
    }
  }

  private def relocateImage(coords: GridCoords): Unit = {
    imageMap(coords).pane.relocate(width() / 2 + xScroll, height() / 2 + yScroll)
  }

  private def onImageGridEvent(event: ImageGrid.Event): Unit = {
    event match {
      case ImageGrid.Event.ImageAdded(image) =>
        val triImage = new TriImage(image, zoom)
        imageMap(image.coords) = triImage
        relocateImage(image.coords)
      case ImageGrid.Event.ImageRemoved(image) =>
    }

    canvas.setScale(zoom)
    canvas.setDisplacement(xScroll, yScroll)
    canvas.redraw()
  }
}
