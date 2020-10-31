package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.util.Listenable
import scalafx.beans.property.ReadOnlyBooleanProperty

abstract class ImageChangeTracker extends Listenable[ImageChangeListener] {
  def image: ImageStorage

  def changed: Boolean
  def changedProperty: ReadOnlyBooleanProperty

  def tellListenersAboutBigChange(): Unit
}
