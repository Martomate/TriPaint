package com.martomate.tripaint

import com.martomate.tripaint.image._
import com.martomate.tripaint.image.storage.Coord
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.paint
import javafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

class ImagePane(imageGrid: ImageGrid) extends Pane with ImageGridListener {
  private val (_primaryColor, _secondaryColor) = (ObjectProperty(Color.Black), ObjectProperty(Color.White))
  private var _globalZoom = 1d
  private var _xScroll: Double = 0
  private var _yScroll: Double = 0

  private val gridSearcher: ImageGridSearcher = new ImageGridSearcher(imageGrid)

  imageGrid.addListener(this)

  private def images: Seq[TriImage] = imageGrid.images
  def imageSize: Int = imageGrid.imageSize

  def globalZoom: Double = _globalZoom
  def xScroll: Double = _xScroll
  def yScroll: Double = _yScroll
  def sideLength: Double = (imageGrid.imageSize * 2 + 1) * globalZoom

  private object drag {
    var x: Double = -1
    var y: Double = -1
  }

  private def imageAt(x: Double, y: Double): Option[TriImage] = {
    //    val xx = (x - width() / 2 - xScroll) / sideLength
    //    val yy = (y - height() / 2 - yScroll) / sideLength

    imageGrid.images find { im =>
      im.indexAt(x, y) != -1
    }
  }

  onMouseDragged = e => {
    if (!e.isConsumed) {
      val xPos = e.getX
      val yPos = e.getY

      EditMode.currentMode match {
        case EditMode.Organize => // TODO: implement scale and rotation if (x, y) is close enough to a corner
          setScroll(xScroll + xPos - drag.x, yScroll + yPos - drag.y)

          images.foreach(_.updateLocation())
        case _ =>
          if (true) {
            val xDiff = xPos - drag.x
            val yDiff = yPos - drag.y
            val dist = Math.hypot(xDiff, yDiff) / globalZoom

            val steps = 4 * dist.toInt + 1
            //		println(s"$steps\t$dist")
            for (i <- 1 to steps) {
              val xx = e.getSceneX + xDiff / steps * (i - steps)
              val yy = e.getSceneY + yDiff / steps * (i - steps)
              imageAt(xx, yy).filter(_.isSelected) match {
                case Some(image) =>
                  val internalCoords = Coord.fromIndex(image.indexAt(xx, yy), imageSize)
                  mousePressedAt(PixelCoords(internalCoords, image.coords), e, dragged = true)
                case _ =>
              }
            }
          }
      }
      drag.x = xPos
      drag.y = yPos
    }
  }
  onMousePressed = e => {
    if (!e.isConsumed) {
      drag.x = e.getX
      drag.y = e.getY
      imageAt(e.getSceneX, e.getSceneY).filter(_.isSelected) foreach { image =>
        val internalCoords = Coord.fromIndex(image.indexAt(e.getSceneX, e.getSceneY), imageSize)
        mousePressedAt(PixelCoords(internalCoords, image.coords), e, dragged = false)
      }
    }
  }
  onMouseReleased = e => images.reverse.foreach(_.onMouseReleased.getValue.handle(e))
  onScroll = e => {
    val (dx, dy) = (e.getDeltaX, e.getDeltaY)

    if (e.isControlDown) {
      val factor = Math.exp(e.getDeltaY * 0.01)
      _globalZoom *= factor
      setScroll(xScroll * factor, yScroll * factor)
    } else {
      setScroll(xScroll + dx, yScroll + dy)
    }
    images.reverse.foreach(_.onScroll.getValue.handle(e))
  }

  private def setScroll(sx: Double, sy: Double): Unit = {
    _xScroll = sx
    _yScroll = sy
    relocateChildren()
  }

  private def mousePressedAt(coords: PixelCoords, e: MouseEvent, dragged: Boolean): Unit = {
    imageGrid(coords.image) foreach { image =>
      primaryOrSecondaryColor foreach { color =>
        EditMode.currentMode match {
          case EditMode.Draw =>
              image.drawAt(coords.pix.index, new Color(color()))
          case EditMode.Fill =>
              fill(coords, new Color(color()))
          case EditMode.PickColor =>
              color() = image.storage(coords.pix.index)
          case _ =>
        }
      }
    }

    def primaryOrSecondaryColor: Option[ObjectProperty[paint.Color]] = e.getButton match {
      case MouseButton.PRIMARY => Some(primaryColor)
      case MouseButton.SECONDARY => Some(secondaryColor)
      case _ => None
    }
  }

  def fill(coords: PixelCoords, color: Color): Unit = {
    imageGrid(coords.image) foreach { image =>
      val referenceColor = image.storage(coords.pix.index)
      val places = gridSearcher.search(coords, (_, col) => col == referenceColor)
      places.foreach(p => imageGrid(p.image).foreach(_.drawAtCoords(p.pix, color)))
    }
  }

  def primaryColor: ObjectProperty[paint.Color] = _primaryColor
  def primaryColor_=(col: Color): Unit = primaryColor.value = col

  def secondaryColor: ObjectProperty[paint.Color] = _secondaryColor
  def secondaryColor_=(col: Color): Unit = secondaryColor.value = col

  def undo: Boolean = imageGrid.selectedImages.forall(_.undo)
  def redo: Boolean = imageGrid.selectedImages.forall(_.redo)

  this.width  onChange updateSize
  this.height onChange updateSize

  def updateSize(): Unit = {
    this.clip() = new Rectangle(0, 0, width(), height())
    relocateChildren()
  }

  def relocateChildren(): Unit = {
    images.foreach(relocateImage)
  }

  def relocateImage(image: TriImage): Unit = {
    image.relocate(width() / 2 + xScroll, height() / 2 + yScroll)
  }

  override def onAddImage(image: TriImage): Unit = {
    children add image
    relocateImage(image)
  }

  override def onRemoveImage(image: TriImage): Unit = {
    val index = children indexOf image

    if (index != -1) {
      children.remove(index)
      if (imageGrid.selectedImages.isEmpty && images.nonEmpty)
        imageGrid.selectImage(images.last, false)
    }
  }
}
