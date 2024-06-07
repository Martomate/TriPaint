package tripaint.view.image

import tripaint.Color
import tripaint.ScalaFxExt.fromFXColor
import tripaint.coords.{GridCoords, PixelCoords, TriangleCoords}
import tripaint.model.{FloodFillSearcher, ImageGrid, ImageGridChange, ImageGridColorLookup}
import tripaint.model.image.{GridCell, ImageChange}
import tripaint.view.EditMode

import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color as FXColor

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
    val primaryColor: ObjectProperty[Color] = ObjectProperty(Color.Black)
    val secondaryColor: ObjectProperty[Color] = ObjectProperty(Color.White)

    def setPrimaryColor(col: Color): Unit = {
      primaryColor.value = col
    }

    def setSecondaryColor(col: Color): Unit = {
      secondaryColor.value = col
    }

    def setPrimaryColor(col: FXColor): Unit = {
      setPrimaryColor(fromFXColor(col))
    }

    def setSecondaryColor(col: FXColor): Unit = {
      setSecondaryColor(fromFXColor(col))
    }
  }

  private val gridSearcher: FloodFillSearcher = new FloodFillSearcher(
    new ImageGridColorLookup(imageGrid)
  )

  private val canvas = new ImageGridCanvas(imageGrid)
  canvas.setScale(zoom)
  canvas.setDisplacement(xScroll, yScroll)

  children.add(canvas)

  imageGrid.trackChanges(this.onImageGridEvent(_))

  private val cumulativeImageChange = mutable.Map.empty[GridCoords, ImageChange.Builder]

  private def stopDrawing(cell: GridCell): ImageChange = {
    cumulativeImageChange(cell.coords).done(cell.storage)
  }

  private def drawAt(cell: GridCell, coords: TriangleCoords, color: Color): Unit = {
    if cell.storage.contains(coords) then {
      cumulativeImageChange(cell.coords).addChange(coords, cell.storage.getColor(coords), color)
      cell.storage.setColor(coords, color)
    }
  }

  private def imageAt(x: Double, y: Double): Option[(GridCell, PixelCoords)] = {
    val pt = canvas.sceneToLocal(x, y)
    val coords = PixelCoords(canvas.coordsAt(pt.x, pt.y), imageGrid.imageSize)
    imageGrid.images.find(_.coords == coords.image).map((_, coords))
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
              if image.editable
            } do mousePressedAt(coords, e.getButton, dragged = true)
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
        if image.editable
      } do {
        mousePressedAt(coords, e.getButton, dragged = false)
      }
    }
  }

  onMouseReleased = e => {
    if !e.isConsumed
    then {
      val changes = mutable.Map.empty[GridCoords, ImageChange]
      for im <- imageGrid.images.reverse
      do {
        val change = stopDrawing(im)
        change.undo()
        changes(im.coords) = change
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

    canvas.redraw()
  }

  private def setScroll(sx: Double, sy: Double): Unit = {
    xScroll = sx
    yScroll = sy
    canvas.setDisplacement(xScroll, yScroll)
  }

  private def mousePressedAt(
      coords: PixelCoords,
      button: MouseButton,
      dragged: Boolean
  ): Unit = {
    val colorToUse = button match {
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
          drawAt(image, coords.pix, color())
        case EditMode.Fill =>
          fill(coords, color())
        case EditMode.PickColor =>
          color() = image.storage.getColor(coords.pix)
        case _ =>
      }
    }
  }

  private inline def clamp(a: Int, lo: Int, hi: Int): Int = Math.min(Math.max(a, lo), hi)

  private def updateCanvasAt(x: Double, y: Double): Unit = {
    val (w, h) = (canvas.width().toInt, canvas.height().toInt)

    val startX = clamp((x - 3 * zoom).toInt, 0, w - 1)
    val startY = clamp((y - 3 * zoom).toInt, 0, h - 1)
    val endX = clamp((x + 3 * zoom + 1).toInt, 0, w - 1)
    val endY = clamp((y + 3 * zoom + 1).toInt, 0, h - 1)

    if endX >= startX && endY >= startY then {
      canvas.redraw(startX, startY, endX - startX + 1, endY - startY + 1)
    }
  }

  private def fill(coords: PixelCoords, color: Color): Unit = {
    for image <- imageGrid(coords.image) do {
      val referenceColor = image.storage.getColor(coords.pix)
      val places = gridSearcher
        .search(coords.toGlobal(imageGrid.imageSize), (_, col) => col == referenceColor)
        .map(p => PixelCoords(p, imageGrid.imageSize))

      for {
        p <- places
        im <- imageGrid(p.image)
      } do {
        drawAt(im, p.pix, color)
      }
    }

    canvas.redraw()
  }

  this.width.onChange(updateSize())
  this.height.onChange(updateSize())

  private def updateSize(): Unit = {
    this.setClip(new Rectangle(0, 0, width(), height()))
    canvas.width = width()
    canvas.height = height()
    canvas.redraw()
  }

  private def onImageGridEvent(event: ImageGrid.Event): Unit = {
    event match {
      case ImageGrid.Event.ImageAdded(image) =>
        cumulativeImageChange(image.coords) = new ImageChange.Builder
        canvas.redraw()
      case ImageGrid.Event.ImageRemoved(image) =>
        cumulativeImageChange -= image.coords
        canvas.redraw()
      case ImageGrid.Event.PixelChanged(coords, from, to) =>
        val (x, y) = canvas.locationOf(coords.toGlobal(imageGrid.imageSize))
        this.updateCanvasAt(x, y)
      case ImageGrid.Event.ImageChangedALot(coords) =>
        // TODO: calculate bounds of the image and only redraw that part of the canvas
        canvas.redraw()
    }
  }
}
