package com.martomate.tripaint.image.storage

class SquareImageSource(source: ImageSource, xOff: Int, yOff: Int, s: Int) extends ImageSourceLense(source) {
  override protected def encode(x: Int, y: Int): (Int, Int) = (x + xOff, y + yOff)
  override protected def decode(x: Int, y: Int): (Int, Int) = (x - xOff, y - yOff)
  override protected def inLense(x: Int, y: Int): Boolean = x >= xOff && x < xOff + s && y >= yOff && y < yOff + s
}
