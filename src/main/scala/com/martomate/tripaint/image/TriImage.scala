package com.martomate.tripaint.image

import java.awt.image.BufferedImage
import java.io.File

import com.martomate.tripaint.{EditMode, ImagePane}
import com.martomate.tripaint.undo.UndoManager
import javafx.scene.control.Tooltip
import javafx.scene.input.{MouseButton, MouseEvent, ScrollEvent}
import scalafx.beans.property._
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

object TriImage {
  val previewSize = 64

  def loadFromFile(file: File, imagePane: ImagePane, offset: Option[(Int, Int)], imageSize: Int): TriImage = {
    new TriImage(ImageStorage.loadFromFile(file, offset, imageSize), imagePane)
  }

  def loadFromFile(file: File, imagePane: ImagePane): TriImage = {
    new TriImage(ImageStorage.loadFromFile(file), imagePane)
  }

  def apply(imageSize: Int, imagePane: ImagePane) = new TriImage(new ImageStorage(imageSize, new Color(imagePane.secondaryColor())), imagePane)
}

class TriImage private(private[image] val storage: ImageStorage, val imagePane: ImagePane) extends Pane {
  private var (panX, panY) = (0D, 0D)
  private var zoom = imagePane.globalZoom

  private val (xpoints, ypoints) = (new Array[Double](3), new Array[Double](3))
  private val (xpointsPrev, ypointsPrev) = (new Array[Double](3), new Array[Double](3))
  private val (xpointsInt, ypointsInt) = (new Array[Int](3), new Array[Int](3))
  private var (xDrag, yDrag) = (-1D, -1D)
  private var dragStartedHere = false
  val selected = new BooleanProperty()

  def isSelected = selected()

  private val canvas = new Canvas
  updateCanvasSize(1d)
  private val indexAtPos: BufferedImage = new BufferedImage(Math.ceil(canvas.width() / zoom * 3).toInt, Math.ceil(canvas.height() / zoom * 3).toInt, BufferedImage.TYPE_INT_RGB)
  val previewCanvas = new Canvas(TriImage.previewSize, TriImage.previewSize * Math.sqrt(3) / 2)

  imagePane.children add canvas
  redraw(true)

  private val undoManager = new UndoManager

  def undo: Boolean = undoManager.undo

  def redo: Boolean = undoManager.redo

  private def fill(index: Int, color: Color): Unit = {
    if (index != -1) {
      val referenceColor = storage(index)
      val places = storage.searchWithIndex(index, (_, col) => col == referenceColor)
      places.foreach(p => drawAtCoords(p, color))
    }
  }

  private def mousePressedAt(x: Double, y: Double, e: MouseEvent, dragged: Boolean): Unit = {
    EditMode.currentMode match {
      case EditMode.Draw =>
        e.getButton match {
          case MouseButton.PRIMARY => drawAt(x, y, new Color(imagePane.primaryColor()))
          case MouseButton.SECONDARY => drawAt(x, y, new Color(imagePane.secondaryColor()))
          case _ =>
        }
      case EditMode.Fill =>
        val index = indexAt(x, y)
        e.getButton match {
          case MouseButton.PRIMARY => fill(index, new Color(imagePane.primaryColor()))
          case MouseButton.SECONDARY => fill(index, new Color(imagePane.secondaryColor()))
          case _ =>
        }
      case EditMode.PickColor =>
        val index = indexAt(x, y)
        if (index != -1) {
          e.getButton match {
            case MouseButton.PRIMARY => imagePane.primaryColor = storage(index)
            case MouseButton.SECONDARY => imagePane.secondaryColor = storage(index)
            case _ =>
          }
        }
      case _ =>
    }
  }

  def onMouseDragged(e: MouseEvent): Unit = {
    if (!e.isConsumed) {
      val xPos = e.getX
      val yPos = e.getY

      EditMode.currentMode match {
        case EditMode.Organize => // TODO: implement scale and rotation if (x, y) is close enough to a corner
          if (dragStartedHere) {
            panX += xPos - xDrag
            panY += yPos - yDrag

            updateLocation()
            e.consume()
          }
        case _ =>
          if (isSelected) {
            val xDiff = xPos - xDrag
            val yDiff = yPos - yDrag
            val dist = Math.hypot(xDiff, yDiff) / zoom

            val steps = 4 * dist.toInt + 1
            //		println(s"$steps\t$dist")
            for (i <- 1 to steps) {
              val point = canvas.sceneToLocal(e.getSceneX + xDiff / steps * (i - steps), e.getSceneY + yDiff / steps * (i - steps))
              mousePressedAt(point.getX, point.getY, e, dragged = true)
            }
          }
      }
      xDrag = xPos
      yDrag = yPos
    }
  }

  def onMousePressed(e: MouseEvent): Unit = {
    if (!e.isConsumed) {
      xDrag = e.getX
      yDrag = e.getY
      val point = canvas.sceneToLocal(e.getSceneX, e.getSceneY)
      dragStartedHere = indexAt(point.getX, point.getY) != -1

      if (isSelected) mousePressedAt(point.getX, point.getY, e, dragged = false)
    }
  }

  def onMouseReleased(e: MouseEvent): Unit = {
    if (!e.isConsumed) {
      if (isSelected) updateAfterDraw()
    }
  }

  def onScroll(e: ScrollEvent): Unit = {
    val (dx, dy) = (e.getDeltaX, e.getDeltaY)

    if (e.isControlDown) {
      val gc = canvas.graphicsContext2D
      gc.clearRect(0, 0, canvas.width(), canvas.height())

      updateCanvasSize(Math.exp(dy * 0.01))
      // TODO: rotate with dx?

      redraw(true)
    } else {
      panX += dx
      panY += dy

      updateLocation()
    }
  }

  private def drawAt(x: Double, y: Double, color: Color): Unit = {
    drawAt(indexAt(x, y), color)
  }

  private def drawAt(index: Int, color: Color): Unit = {
    if (index >= 0 && index < storage.numPixels) {
      storage(index) = color

      val coords = storage.coordsFromIndex(index)
      drawTriangle(coords, doIndexMapping = false)
    }
  }

  private def drawAtCoords(coords: Coord, color: Color): Unit = {
    val index = coords.index
    if (index >= 0 && index < storage.numPixels) {
      storage(index) = color

      drawTriangle(coords, doIndexMapping = false)
    }
  }

  private def indexAt(x: Double, y: Double): Int = {
    val (xx, yy) = ((x / canvas.width() * indexAtPos.getWidth).toInt, (y / canvas.height() * indexAtPos.getHeight).toInt)
    if (xx >= 0 && xx < indexAtPos.getWidth() && yy >= 0 && yy < indexAtPos.getHeight()) {
      (indexAtPos.getRGB(xx, yy) & 0xffffff) - 1
    } else -1
  }

  private[image] def drawTriangle(coords: Coord, doIndexMapping: Boolean, strokeInstead: Boolean = false): Unit = {
    val Coord(x, y, index) = coords
    val yp = y
    val xp = x * 0.5 - (yp - storage.imageSize + 1) * 0.5

    if (x % 2 == 1) {
      storeCoords(0, xp, yp)
      storeCoords(1, xp + 1.0, yp)
      storeCoords(2, xp + 0.5, yp + 1.0)
    } else {
      storeCoords(0, xp, yp + 1.0)
      storeCoords(1, xp + 1.0, yp + 1.0)
      storeCoords(2, xp + 0.5, yp)
    }

    val gc = canvas.graphicsContext2D

    if (strokeInstead) {
      gc.setStroke(storage(index))
      gc.strokePolygon(xpoints, ypoints, 3)
    } else {
      gc.setFill(storage(index))
      gc.fillPolygon(xpoints, ypoints, 3)
    }

    val gcPrev = previewCanvas.graphicsContext2D
    if (strokeInstead) {
      gcPrev.setStroke(storage(index))
      gcPrev.strokePolygon(xpointsPrev, ypointsPrev, 3)
    } else {
      gcPrev.setFill(storage(index))
      gcPrev.fillPolygon(xpointsPrev, ypointsPrev, 3)
    }

    if (doIndexMapping) {
      val indexGraphics = indexAtPos.getGraphics
      val indexColor = new java.awt.Color(index + 1)
      indexGraphics.setColor(indexColor)
      indexGraphics.drawPolygon(xpointsInt, ypointsInt, 3)
      indexGraphics.fillPolygon(xpointsInt, ypointsInt, 3)
    }
  }

  def redraw(doIndexMapping: Boolean): Unit = {
    canvas.graphicsContext2D.clearRect(0, 0, canvas.width(), canvas.height())
    previewCanvas.graphicsContext2D.clearRect(0, 0, TriImage.previewSize, TriImage.previewSize)

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangle(Coord(x, y)(storage), doIndexMapping, strokeInstead = true)
      }
    }

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangle(Coord(x, y)(storage), doIndexMapping)
      }
    }
  }

  private def storeCoords(index: Int, xPos: Double, yPos: Double): Unit = {
    xpoints(index) = xPos / storage.imageSize * canvas.width()
    ypoints(index) = yPos / storage.imageSize * canvas.height()

    xpointsPrev(index) = xPos / storage.imageSize * previewCanvas.width()
    ypointsPrev(index) = yPos / storage.imageSize * previewCanvas.height()

    xpointsInt(index) = Math.round(xPos / storage.imageSize * indexAtPos.getWidth).toInt
    ypointsInt(index) = Math.round(yPos / storage.imageSize * indexAtPos.getHeight).toInt
  }

  private def updateCanvasSize(zoomFactor: Double): Unit = {
    zoom *= zoomFactor
    panX *= zoomFactor
    panY *= zoomFactor
    canvas.width = (storage.imageSize * 2 + 1) * zoom
    canvas.height = canvas.width() * Math.sqrt(3) / 2

    updateLocation()
  }

  def move(dx: Double, dy: Double): Unit = {
    panX += dx
    panY += dy

    updateLocation()
  }

  def scale(amt: Double): Unit = {
    updateCanvasSize(amt)

    redraw(true)
  }

  def rotate(angle: Double): Unit = {
    canvas.rotate() += angle
  }

  private def updateAfterDraw(): Unit = {
    undoManager.append(storage.cumulativeChange.done(EditMode.currentMode.tooltipText, this))
    redraw(false)

  }

  def blur(radius: Int): Unit = {
    storage.blur(radius)

    updateAfterDraw()
  }

  def motionBlur(radius: Int): Unit = {
    storage.motionBlur(radius)

    updateAfterDraw()
  }

  def perlinNoise(): Unit = {
    storage.perlinNoise()

    updateAfterDraw()
  }

  def randomNoise(min: Color = Color.Black, max: Color = Color.White): Unit = {
    storage.randomNoise(min, max)

    updateAfterDraw()
  }

  def scramble(): Unit = {
    storage.scramble()

    updateAfterDraw()
  }

  def updateLocation(): Unit = {
    canvas.relocate((imagePane.width() - canvas.width()) / 2 + panX, (imagePane.height() - canvas.height()) / 2 + panY)
  }

  def save: Boolean = storage.save

  def setSaveLocation(location: SaveLocation): Unit = {
    storage.saveLocation = location
  }

  def hasChanged: Boolean = storage.hasChanged

  def hasChangedProperty: ReadOnlyBooleanProperty = storage.hasChangedProperty

  private val _toolTip = new ReadOnlyObjectWrapper[Tooltip](null, null, new Tooltip("Not saved"))

  def toolTip: ReadOnlyObjectProperty[Tooltip] = _toolTip.readOnlyProperty

  _toolTip().textProperty().bind(storage.infoText)
}
