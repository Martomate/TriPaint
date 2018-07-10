package com.martomate.tripaint

import com.martomate.tripaint.image.TriImage
import javafx.scene.paint
import javafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer

class ImagePane extends Pane {
  private val images = ArrayBuffer.empty[TriImage]

  private val (_primaryColor, _secondaryColor) = (ObjectProperty(Color.Black), ObjectProperty(Color.White))
  private var _globalZoom = 1d

  def globalZoom: Double = _globalZoom

  onMouseDragged = e => images.reverse.foreach(_.onMouseDragged(e))
  onMousePressed = e => images.reverse.foreach(_.onMousePressed(e))
  onMouseReleased = e => images.reverse.foreach(_.onMouseReleased(e))
  onScroll = e => {
    if (e.isControlDown) _globalZoom *= Math.exp(e.getDeltaY * 0.01)
    images.reverse.foreach(_.onScroll(e))
  }

  def primaryColor: ObjectProperty[paint.Color] = _primaryColor

  def primaryColor_=(col: Color): Unit = primaryColor.value = col

  def secondaryColor: ObjectProperty[paint.Color] = _secondaryColor

  def secondaryColor_=(col: Color): Unit = secondaryColor.value = col

  def getImages: Vector[TriImage] = images.toVector

  def getSelectedImages: Vector[TriImage] = getImages.filter(_.isSelected)

  def addImage(image: TriImage): Unit = images append image

  def removeImage(image: TriImage): Unit = {
    val index = images indexOf image

    if (index != -1) removeImage(index)
  }

  def removeImage(index: Int): Unit = {
    children.remove(index)
    images remove index
    if (getSelectedImages.isEmpty && images.nonEmpty) selectImage(images(images.size - 1), replace = false)
  }

  def selectImage(image: TriImage, replace: Boolean): Unit = {
    if (replace) images.foreach(im => im.selected() = im eq image)
    else image.selected() = !image.selected()
  }

  def undo: Boolean = {
    var result = true
    for (im <- getSelectedImages) {
      if (!im.undo) result = false
    }
    result
  }

  def redo: Boolean = {
    var result = true
    for (im <- getSelectedImages) {
      if (!im.redo) result = false
    }
    result
  }

  this.width onChange updateSize
  this.height onChange updateSize

  def updateSize(): Unit = {
    this.clip() = new Rectangle(0, 0, width(), height())
    images.foreach(x => x.updateLocation())
  }
}