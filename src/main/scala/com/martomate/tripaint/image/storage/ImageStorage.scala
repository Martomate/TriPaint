package com.martomate.tripaint.image.storage

import java.io.File

import com.martomate.tripaint.Listenable
import com.martomate.tripaint.image.CumulativeImageChange
import scalafx.beans.property._
import scalafx.scene.paint.Color

import scala.util.Try

case class SaveLocation(file: File, offset: Option[(Int, Int)])

trait ImageStorageListener {
  def onPixelChanged(coords: Coord): Unit
}

class ImageStorage(initialSource: SquareImageSource) extends Listenable[ImageStorageListener] {
  private var imageSource: SquareImageSource = initialSource
  private val imageSourceListener = new ImageSourceListener {
    override def onPixelChanged(x: Int, y: Int): Unit = {
      notifyListeners(_.onPixelChanged(coordsFromIndex(x + y * imageSize)))
    }

    override def onImageSourceSaved(isSaved: Boolean): Unit = {
      hasChangedWrapper() = !isSaved
    }
  }
  imageSource.addListener(imageSourceListener)

  def imageSize: Int = imageSource.s

  def numPixels: Int = imageSize * imageSize

  private[image] val cumulativeChange = new CumulativeImageChange
  private[image] var registerChanges = true

  def apply(index: Int) = imageSource(index)

  def update(index: Int, newColor: Color): Unit = {
    if (registerChanges) cumulativeChange.addChange(index, apply(index), newColor)
    imageSource(index) = newColor
  }

  def hasChanged: Boolean = imageSource.changed

  private var hasChangedWrapper: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(hasChanged)
  def hasChangedProperty: ReadOnlyBooleanProperty = hasChangedWrapper.readOnlyProperty

  private var _saveLocation: SaveLocation = _

  def saveLocation: SaveLocation = _saveLocation

  def saveLocation_=(location: SaveLocation): Unit = {
    if (location.file == null || location.offset == null) throw new IllegalArgumentException("location mush have non-null file and offset")
    _saveLocation = location
    //hasChanged = true
    imageSource.imageSaver = new FileImageSaver(imageSource, location.file)
    imageSource.save()
    val offset = location.offset.getOrElse((0, 0))
    ImageSourceImpl.fromFile(location.file) foreach { source =>
      SquareImageSource(source, offset._1, offset._2, imageSize) foreach { sqImage =>
        imageSource.removeListener(imageSourceListener)
        imageSource = sqImage
        imageSource.addListener(imageSourceListener)
      }
    }

    _infoText() = makeInfoText
  }

  def neighbours(c: Coord): Seq[Coord] = neighbours(c.x, c.y)

  def neighbours(x: Int, y: Int): Seq[Coord] = {
    Seq(
      Coord.fromXY(x - 1, y, imageSize),
      if (x % 2 == 0) Coord.fromXY(x + 1, y + 1, imageSize) else Coord.fromXY(x - 1, y - 1, imageSize),
      Coord.fromXY(x + 1, y, imageSize)).filter(t => t.x >= 0 && t.y >= 0 && t.x < 2 * t.y + 1 && t.y < imageSize)
  }

  def searchWithIndex(index: Int, predicate: (Coord, Color) => Boolean): Seq[Coord] = search(coordsFromIndex(index), predicate)

  def search(startPos: Coord, predicate: (Coord, Color) => Boolean): Seq[Coord] = {
    val visited = collection.mutable.Set.empty[Coord]
    val result = collection.mutable.ArrayBuffer.empty[Coord]
    val q = collection.mutable.Queue(startPos)
    val startIndex = startPos.index
    if (startIndex != -1) {
      visited += startPos

      while (q.nonEmpty) {
        val p = q.dequeue

        val color = apply(p.index)
        if (predicate(p, color)) {
          result += p

          val newOnes = neighbours(p.x, p.y).filter(!visited(_))
          visited ++= newOnes
          q ++= newOnes
        }
      }
    }
    result
  }

  def coordsFromIndex(index: Int): Coord = Coord.fromIndex(index, imageSize)

  def save: Boolean = imageSource.save()

  private val _infoText = new ReadOnlyStringWrapper

  def infoText: ReadOnlyStringProperty = _infoText.readOnlyProperty

  private def makeInfoText: String = {
    if (saveLocation != null) {
      s"File: ${saveLocation.file.getName}\nSize: $imageSize" + (if (saveLocation.offset.isDefined) s"\nOffset: ${saveLocation.offset}" else "")
    } else s"Not saved\nSize: $imageSize"
  }

  _infoText() = makeInfoText
}

object ImageStorage {
  def loadFromFile(file: File, offset: Option[(Int, Int)] = None, imageSize: Int): Try[ImageStorage] = {
    val (xOff, yOff) = offset.getOrElse((0, 0))
    ImageSourceImpl.fromFile(file) flatMap {
      SquareImageSource(_, xOff, yOff, imageSize) map {
        new ImageStorage(_)
      }
    }
  }

  def fromSource(imageSource: ImageSource, offset: Option[(Int, Int)], imageSize: Int): Try[ImageStorage] = {
    val (xOff, yOff) = offset.getOrElse((0, 0))

    SquareImageSource(imageSource, xOff, yOff, imageSize).map(new ImageStorage(_))
  }

  def unboundImage(imageSize: Int, initialColor: Color): ImageStorage = {
    val imageSource = new UnboundImageSource(imageSize, initialColor)
    SquareImageSource(imageSource, 0, 0, imageSize).map(new ImageStorage(_)).getOrElse(null)
  }
}