package com.martomate.tripaint

import com.martomate.tripaint.image.{TriImage, TriImageCoords}
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

  onMouseDragged = e => images.reverse.foreach(_.onMouseDragged.getValue.handle(e))
  onMousePressed = e => images.reverse.foreach(_.onMousePressed.getValue.handle(e))
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
