package com.martomate.tripaint.image.storage

import java.awt.image.BufferedImage
import java.io.{File, IOException}

import com.martomate.tripaint.image.CumulativeImageChange
import javax.imageio.ImageIO
import scalafx.beans.property._
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer

case class SaveLocation(file: File, offset: Option[(Int, Int)])

trait ImageStorageListener {
  def onPixelChanged(coords: Coord): Unit
}

class ImageStorage(val imageSize: Int, initialColor: Color = null) {
  private val _pixels = Array.fill[Color](imageSize * imageSize)(initialColor)

  private val imageStorageListeners: ArrayBuffer[ImageStorageListener] = ArrayBuffer.empty
  def addImageStorageListener(listener: ImageStorageListener): Unit = imageStorageListeners += listener

  def numPixels: Int = _pixels.length

  private[image] val cumulativeChange = new CumulativeImageChange
  private[image] var registerChanges = true

  def apply(index: Int) = _pixels(index)

  def update(index: Int, newColor: Color): Unit = {
    if (registerChanges) cumulativeChange.addChange(index, apply(index), newColor)
    _pixels(index) = newColor
    hasChanged = true

    imageStorageListeners.foreach(_.onPixelChanged(Coord.fromIndex(index, imageSize)))
  }

  private val _hasChanged = new ReadOnlyBooleanWrapper

  private def hasChanged_=(newVal: Boolean): Unit = _hasChanged() = newVal

  def hasChanged: Boolean = _hasChanged.value

  def hasChangedProperty: ReadOnlyBooleanProperty = _hasChanged.readOnlyProperty

  private var _saveLocation: SaveLocation = _

  def saveLocation: SaveLocation = _saveLocation

  def saveLocation_=(location: SaveLocation): Unit = {
    if (location.file == null || location.offset == null) throw new IllegalArgumentException("location mush have non-null file and offset")
    _saveLocation = location
    hasChanged = true

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

  def save: Boolean = {
    var success = false
    try {
      if (saveLocation != null) {
        if (!saveLocation.file.exists()) {
          saveLocation.file.getParentFile.mkdirs()
          saveLocation.file.createNewFile()
        }
        val image = saveLocation.offset match {
          case Some((xOff, yOff)) =>
            val _image = ImageIO.read(saveLocation.file)
            if (xOff + imageSize > _image.getWidth || yOff + imageSize > _image.getHeight) throw new IOException("Image size too big for destination")
            _image
          case None =>
            new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB)
        }
        val off = saveLocation.offset.getOrElse((0, 0))
        for (y <- 0 until imageSize) {
          for (x <- 0 until imageSize) {
            val idx = x + y * imageSize
            image.setRGB(off._1 + x, off._2 + y,
              (_pixels(idx).opacity * 255).toInt << 24 |
              (_pixels(idx).red     * 255).toInt << 16 |
              (_pixels(idx).green   * 255).toInt <<  8 |
              (_pixels(idx).blue    * 255).toInt)
          }
        }

        if (ImageIO.write(image, saveLocation.file.getName.substring(saveLocation.file.getName.lastIndexOf('.') + 1), saveLocation.file)) {
          hasChanged = false
          success = true
        }
      }
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
    success
  }

  private val _infoText = new ReadOnlyStringWrapper

  def infoText: ReadOnlyStringProperty = _infoText.readOnlyProperty

  private def makeInfoText =
    if (saveLocation != null) {
      s"File: ${saveLocation.file.getName}\nSize: $imageSize" + (if (saveLocation.offset.isDefined) s"\nOffset: ${saveLocation.offset}" else "")
    } else s"Not saved\nSize: $imageSize"

  _infoText() = makeInfoText
}

object ImageStorage {
  def loadFromFile(file: File, offset: Option[(Int, Int)] = None, imageSize: Int = -1): ImageStorage = {
    try {
      val image = ImageIO.read(file)
      val size = if (imageSize != -1) imageSize else {
        val s = image.getWidth()
        if (s != image.getHeight())
          throw new IOException("Image must be square!")
        s
      }
      val (xOff, yOff) = offset.getOrElse((0, 0))
      val pix = image.getRGB(xOff, yOff, size, size, null, 0, size)
      val storage = new ImageStorage(size)

      for (i <- storage._pixels.indices) {
        storage._pixels(i) = Color.rgb(
          pix(i) >> 16 & 0xff,
          pix(i) >> 8 & 0xff,
          pix(i) >> 0 & 0xff,
          (pix(i) >> 24 & 0xff) / 255.0)
      }

      storage.saveLocation = SaveLocation(file, offset)
      storage.hasChanged = false
      storage
    } catch {
      case e: IOException =>
        e.printStackTrace()
        null
    }
  }
}