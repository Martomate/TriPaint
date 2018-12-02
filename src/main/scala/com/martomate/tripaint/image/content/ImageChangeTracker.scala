package com.martomate.tripaint.image.content

import com.martomate.tripaint.Listenable
import com.martomate.tripaint.image.storage.ImageStorage
import scalafx.beans.property.ReadOnlyBooleanProperty

abstract class ImageChangeTracker extends Listenable[ImageChangeListener] {
  def image: ImageStorage

  def changed: Boolean
  def changedProperty: ReadOnlyBooleanProperty

  def tellListenersAboutBigChange(): Unit
}
