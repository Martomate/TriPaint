package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.pool.{ImagePool, ImagePoolListener}
import com.martomate.tripaint.model.image.save.ImageSaver
import com.martomate.tripaint.model.image.storage.{ImageStorage, ImageStorageListener}
import scalafx.beans.property.{ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}
import scalafx.scene.paint.Color

class ImageChangeTrackerImpl(init_image: ImageStorage, pool: ImagePool, saver: ImageSaver) extends ImageChangeTracker with ImagePoolListener with ImageStorageListener {
  private var _image: ImageStorage = init_image
  def image: ImageStorage = _image

  pool.addListener(this)
  image.addListener(this)

  private val _changed: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)
  def changed: Boolean = _changed.value
  def changedProperty: ReadOnlyBooleanProperty = _changed.readOnlyProperty

  def tellListenersAboutBigChange(): Unit = notifyListeners(_.onImageChangedALot())

  def onImageSaved(image: ImageStorage, saver: ImageSaver): Unit = {
    if (image == this.image && saver == this.saver) {
      _changed.value = false
    }
  }

  def onImageReplaced(oldImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Unit = {
    if (oldImage == image) {
      image.removeListener(this)
      _image = newImage
      image.addListener(this)
      notifyListeners(_.onImageChangedALot())
    }
  }

  def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit = {
    _changed.value = true
    notifyListeners(_.onPixelChanged(coords, from, to))
  }
}
