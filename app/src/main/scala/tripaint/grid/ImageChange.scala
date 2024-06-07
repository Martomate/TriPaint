package tripaint.grid

import tripaint.Color
import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage
import tripaint.model.Change

import scala.collection.mutable.ArrayBuffer

class ImageChange private (image: ImageStorage, pixelsChanged: Seq[ImageChange.PixelChange])
    extends Change {
  def redo(): Unit =
    for ch <- pixelsChanged do image.setColor(ch.coords, ch.after)

  def undo(): Unit =
    for ch <- pixelsChanged do image.setColor(ch.coords, ch.before)
}

object ImageChange {
  private case class PixelChange(coords: TriangleCoords, before: Color, after: Color)

  class Builder {
    private val pixelsChanged = ArrayBuffer.empty[PixelChange]

    def done(image: ImageStorage): ImageChange =
      val change = new ImageChange(image, pixelsChanged.reverse.toVector)
      pixelsChanged.clear()
      change

    def addChange(index: TriangleCoords, oldColor: Color, newColor: Color): Builder =
      pixelsChanged += PixelChange(index, oldColor, newColor)
      this

    def nonEmpty: Boolean = pixelsChanged.nonEmpty
  }

  def builder(): Builder = new Builder
}
