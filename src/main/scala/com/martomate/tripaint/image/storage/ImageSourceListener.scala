package com.martomate.tripaint.image.storage

trait ImageSourceListener {
  def onPixelChanged(x: Int, y: Int): Unit
  final def onPixelChanged(c: (Int, Int)): Unit = onPixelChanged(c._1, c._2)

  def onImageSourceSaved(isSaved: Boolean): Unit
}
