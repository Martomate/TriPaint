package com.martomate.tripaint.image.storage

import scalafx.scene.paint.Color

class ImageSourceImpl(width: Int, height: Int) extends ImageSource {
  private val pixels: Array[Color] = new Array(width * height)

  override def apply(x: Int, y: Int): Color = pixels(x + y * width)

  override def update(x: Int, y: Int, col: Color): Unit = {
    pixels(x + y * width) = col
    notifyListeners(_.onPixelChanged(x, y))
  }

  override def save(): Boolean = false
}
