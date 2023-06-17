package com.martomate.tripaint.model.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.util.{EventDispatcher, Tracker}

import scala.util.Try

class ImageStorage private (val imageSize: Int, image: RegularImage) {
  private val dispatcher = new EventDispatcher[ImageStorage.Event]
  def trackChanges(tracker: Tracker[ImageStorage.Event]): Unit = dispatcher.track(tracker)

  final def contains(coords: TriangleCoords): Boolean = coords.y < imageSize

  final def getColor(coords: TriangleCoords): Color =
    val sc = SimpleStorageFormat.transform(coords)
    image.getColor(sc.x, sc.y)

  final def setColor(coords: TriangleCoords, col: Color): Unit =
    val before = this.getColor(coords)
    if before != col then
      val sc = SimpleStorageFormat.transform(coords)
      image.setColor(sc.x, sc.y, col)
      dispatcher.notify(ImageStorage.Event.PixelChanged(coords, before, col))

  def allPixels: IndexedSeq[TriangleCoords] =
    for
      y <- 0 until imageSize
      x <- 0 until 2 * y + 1
    yield TriangleCoords(x, y)

  def toRegularImage(format: StorageFormat): RegularImage =
    val image = RegularImage.ofSize(imageSize, imageSize)
    for y <- 0 until imageSize do
      for x <- 0 until 2 * y + 1 do
        val tCoords = TriangleCoords(x, y)
        val sCoords = format.transform(tCoords)
        image.setColor(sCoords.x, sCoords.y, this.getColor(tCoords))
    image
}

object ImageStorage {
  def fill(imageSize: Int, color: Color) =
    new ImageStorage(imageSize, RegularImage.fill(imageSize, imageSize, color))

  def fromRegularImage(
      image: RegularImage,
      offset: StorageCoords,
      format: StorageFormat,
      imageSize: Int
  ): Try[ImageStorage] =
    val regularImage = RegularImage.tabulate(imageSize, imageSize)((x, y) =>
      val stCoords = format.transform(SimpleStorageFormat.reverse(StorageCoords(x, y)))
      image.getColor(offset.x + stCoords.x, offset.y + stCoords.y)
    )
    Try(new ImageStorage(imageSize, regularImage))

  enum Event:
    case PixelChanged(coords: TriangleCoords, from: Color, to: Color)
}
