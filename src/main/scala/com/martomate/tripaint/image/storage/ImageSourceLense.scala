package com.martomate.tripaint.image.storage

import scalafx.scene.paint.Color

abstract class ImageSourceLense(source: ImageSource) extends ImageSource {
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

  override def apply(x: Int, y: Int): Color = {
    source(encode(x, y))
  }
  override def update(x: Int, y: Int, col: Color): Unit = {
    source.update(encode(x, y), col)
  }

  protected def encode(x: Int, y: Int): (Int, Int)
  protected def decode(x: Int, y: Int): (Int, Int)
  protected def inLense(x: Int, y: Int): Boolean

  override def save(): Boolean = source.save()

  override def imageSaver_=(saver: ImageSaver): Unit = source.imageSaver_=(saver)
}
