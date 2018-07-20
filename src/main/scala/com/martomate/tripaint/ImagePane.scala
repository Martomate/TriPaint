package com.martomate.tripaint

import com.martomate.tripaint.image.{Coord, TriImage, TriImageCoords}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.paint
import javafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer

trait ImageCollage {
  def imageSize: Int
  def images: Seq[TriImage]

  private val listeners: ArrayBuffer[ImageCollageListener] = ArrayBuffer.empty

  def apply(coords: TriImageCoords): TriImage
  def update(coords: TriImageCoords, image: TriImage): Unit
  def -=(coords: TriImageCoords): TriImage

  final def addListener(listener: ImageCollageListener): Unit = listeners += listener
  final def removeListener(listener: ImageCollageListener): Unit = listeners -= listener

  protected final def onAddImage(image: TriImage): Unit = listeners.foreach(_.onAddImage(image))
  protected final def onRemoveImage(image: TriImage): Unit = listeners.foreach(_.onRemoveImage(image))
}

class ImageCollageImplOld(val imageSize: Int) extends ImageCollage {
  val images: ArrayBuffer[TriImage] = ArrayBuffer.empty[TriImage]

  override def apply(coords: TriImageCoords): TriImage = images.find(_.coords == coords).orNull
  override def update(coords: TriImageCoords, image: TriImage): Unit = {
    val idx = images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val prev = images(idx)
      if (prev != image) onRemoveImage(prev)
      images(idx) = image
    }
    else images += image
    onAddImage(image)
  }
  override def -=(coords: TriImageCoords): TriImage = {
    val idx = images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val ret = images.remove(idx)
      onRemoveImage(ret)
      ret
    }
    else null
  }
}

trait ImageCollageListener {
  def onAddImage(image: TriImage): Unit
  def onRemoveImage(image: TriImage): Unit
}

case class PixelCoords(pix: Coord, image: TriImageCoords) {
  def neighbours(imageSize: Int): Seq[PixelCoords] = {// TODO: reach into neighboring images
    val (x, y) = (pix.x, pix.y)
    val localCoords = Seq(
      Coord.fromXY(x - 1, y, imageSize),
      if (x % 2 == 0) Coord.fromXY(x + 1, y + 1, imageSize) else Coord.fromXY(x - 1, y - 1, imageSize),
      Coord.fromXY(x + 1, y, imageSize)
    ).filter(t => t.x >= 0 && t.y >= 0 && t.x < 2 * t.y + 1 && t.y < imageSize)
    localCoords.map(c => PixelCoords(c, image))
  }
}

class ImagePane(collage: ImageCollage) extends Pane with ImageCollageListener {
  private val (_primaryColor, _secondaryColor) = (ObjectProperty(Color.Black), ObjectProperty(Color.White))
  private var _globalZoom = 1d
  private var _xScroll: Double = 0
  private var _yScroll: Double = 0

  collage.addListener(this)

  private def images: Seq[TriImage] = collage.images
  def imageSize: Int = collage.imageSize

  def globalZoom: Double = _globalZoom
  def xScroll: Double = _xScroll
  def yScroll: Double = _yScroll
  def sideLength: Double = (collage.imageSize * 2 + 1) * globalZoom

  private object drag {
    var x: Double = -1
    var y: Double = -1
  }

  private def imageAt(x: Double, y: Double): Option[TriImage] = {
//    val xx = (x - width() / 2 - xScroll) / sideLength
//    val yy = (y - height() / 2 - yScroll) / sideLength

    images find { im =>
      val pt = im.canvas.sceneToLocal(x, y)
      im.indexAt(pt.getX, pt.getY) != -1
    }
  }

  private def imageAt(coords: TriImageCoords): Option[TriImage] = images.find(_.coords == coords)

  onMouseDragged = e => {
    if (!e.isConsumed) {
      val xPos = e.getX
      val yPos = e.getY

      EditMode.currentMode match {
        case EditMode.Organize => // TODO: implement scale and rotation if (x, y) is close enough to a corner
          _xScroll += xPos - drag.x
          _yScroll += yPos - drag.y

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
                  val point = image.canvas.sceneToLocal(xx, yy)
                  val internalCoords = Coord.fromIndex(image.indexAt(point.getX, point.getY), imageSize)
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
        val point = image.canvas.sceneToLocal(e.getSceneX, e.getSceneY)
        val internalCoords = Coord.fromIndex(image.indexAt(point.getX, point.getY), imageSize)
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
      _xScroll *= factor
      _yScroll *= factor
    } else {
      _xScroll += dx
      _yScroll += dy
    }
    images.reverse.foreach(_.onScroll.getValue.handle(e))
  }

  private def mousePressedAt(coords: PixelCoords, e: MouseEvent, dragged: Boolean): Unit = {
    imageAt(coords.image) foreach { image =>
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
    imageAt(coords.image) foreach { image =>
      val referenceColor = image.storage(coords.pix.index)
      val places = search(coords, (_, col) => col == referenceColor)
      places.foreach(p => imageAt(p.image).foreach(_.drawAtCoords(p.pix, color)))
    }
  }

  def search(startPos: PixelCoords, predicate: (PixelCoords, Color) => Boolean): Seq[PixelCoords] = {
    val visited = collection.mutable.Set.empty[PixelCoords]
    val result = collection.mutable.ArrayBuffer.empty[PixelCoords]
    val q = collection.mutable.Queue(startPos)
    visited += startPos

    while (q.nonEmpty) {
      val p = q.dequeue
      imageAt(p.image) foreach { image =>
        val color = image.storage(p.pix.index)
        if (predicate(p, color)) {
          result += p

          val newOnes = p.neighbours(imageSize).filter(!visited(_))
          visited ++= newOnes
          q ++= newOnes
        }
      }
    }
    result
  }

  def primaryColor: ObjectProperty[paint.Color] = _primaryColor
  def primaryColor_=(col: Color): Unit = primaryColor.value = col

  def secondaryColor: ObjectProperty[paint.Color] = _secondaryColor
  def secondaryColor_=(col: Color): Unit = secondaryColor.value = col

  def getImages: Vector[TriImage] = images.toVector
  def getSelectedImages: Vector[TriImage] = getImages.filter(_.isSelected)

  def addImage(image: TriImage): Unit = collage(image.coords) = image
  def removeImage(image: TriImage): Unit = collage -= image.coords

  def selectImage(image: TriImage, replace: Boolean): Unit = {
    if (replace) images.foreach(im => im.selected() = im eq image)
    else image.selected() = !image.selected()
  }

  def undo: Boolean = getSelectedImages.forall(_.undo)
  def redo: Boolean = getSelectedImages.forall(_.redo)

  this.width  onChange updateSize
  this.height onChange updateSize

  def updateSize(): Unit = {
    this.clip() = new Rectangle(0, 0, width(), height())
    images.foreach(_.updateLocation())
  }

  override def onAddImage(image: TriImage): Unit = children add image

  override def onRemoveImage(image: TriImage): Unit = {
    val index = children indexOf image

    if (index != -1) {
      children.remove(index)
      if (getSelectedImages.isEmpty && images.nonEmpty)
        selectImage(images.last, replace = false)
    }
  }
}
