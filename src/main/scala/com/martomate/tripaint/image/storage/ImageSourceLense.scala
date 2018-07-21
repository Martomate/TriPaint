package com.martomate.tripaint.image.storage

import scalafx.scene.paint.Color

abstract class ImageSourceLense(source: ImageSource) extends ImageSource {
  source.addListener(new ImageSourceListener {
    override def onPixelChanged(x: Int, y: Int): Unit = if (inLense(x, y)) {
      notifyListeners(_.onPixelChanged(decode(x, y)))
      if (!hasChanged) {
        hasChanged = true
        notifyListeners(_.onImageSourceSaved(false))
      }
    }

    override def onImageSourceSaved(isSaved: Boolean): Unit = if (isSaved) hasChanged = false
  })

  override def apply(x: Int, y: Int): Color = {
    require(inLense(x, y))
    source(encode(x, y))
  }
  override def update(x: Int, y: Int, col: Color): Unit = {
    require(inLense(x, y))
    source.update(encode(x, y), col)
  }

  protected def encode(x: Int, y: Int): (Int, Int)
  protected def decode(x: Int, y: Int): (Int, Int)
  protected def inLense(x: Int, y: Int): Boolean

  override def save(): Boolean = false
}
