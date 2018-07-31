package com.martomate.tripaint.image2.storage

import com.martomate.tripaint.Listenable
import com.martomate.tripaint.image2.coords.TriangleCoords
import com.martomate.tripaint.image2.save.{ImageSaveInfo, ImageSaver}
import scalafx.beans.property.{ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}
import scalafx.scene.paint.Color

trait ImageStorage extends Listenable[ImageStorageListener] {
  val imageSize: Int

  protected def get(coords: TriangleCoords): Color
  protected def set(coords: TriangleCoords, col: Color): Unit

  final def apply(coords: TriangleCoords): Color = get(coords)
  final def update(coords: TriangleCoords, col: Color): Unit = {
    val before = apply(coords)
    if (before != col) {
      set(coords, col)
      notifyListeners(_.onPixelChanged(coords, before, col))

      if (!changed) {
        _changed.value = true
      }
    }
  }

  var saveInfo: ImageSaveInfo = _

  def save(saver: ImageSaver): Boolean = {
    val success = saveInfo != null && saver.save(this, saveInfo)
    if (success) _changed.value = false
    success
  }

  protected val _changed: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)
  final def changedProperty: ReadOnlyBooleanProperty = _changed.readOnlyProperty
  final def changed: Boolean = _changed.value
}

trait ImageStorageListener {
  def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit
}
