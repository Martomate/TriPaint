package com.martomate.tripaint.image.storage

import com.martomate.tripaint.Listenable
import scalafx.beans.property.{ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}
import scalafx.scene.paint.Color

abstract class ImageLense(val source: ImageSource, val sideLength: Int) extends Listenable[ImageSourceListener] {
  private val parentListener = new ImageSourceListener {
    override def onPixelChanged(x: Int, y: Int): Unit = if (inLense(x, y)) {
      notifyListeners(_.onPixelChanged(decode(x, y)))
      if (!hasChanged()) {
        hasChanged() = true
        notifyListeners(_.onImageSourceSaved(false))
      }
    }

    override def onImageSourceSaved(isSaved: Boolean): Unit = if (isSaved) {
      hasChanged() = false
      notifyListeners(_.onImageSourceSaved(true))
    }
  }

  source.addListener(parentListener)

  def width: Int = sideLength
  def height: Int = sideLength

  def apply(x: Int, y: Int): Color = {
    source(encode(x, y))
  }
  def update(x: Int, y: Int, col: Color): Unit = {
    source.update(encode(x, y), col)
  }

  protected def encode(x: Int, y: Int): (Int, Int)
  protected def decode(x: Int, y: Int): (Int, Int)
  protected def inLense(x: Int, y: Int): Boolean

  def save(): Boolean = source.save()

  def imageSaver: ImageSaver = source.imageSaver
  def imageSaver_=(saver: ImageSaver): Unit = source.imageSaver_=(saver)

  final def changed: Boolean = hasChanged()
  final def changedProperty: ReadOnlyBooleanProperty = hasChanged.readOnlyProperty
  protected final var hasChanged: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(source.changed)
}
