package com.martomate.tripaint.image.storage

import com.martomate.tripaint.Listenable
import scalafx.beans.property.{ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}
import scalafx.scene.paint.Color

trait ImageSource extends Listenable[ImageSourceListener] {
  def apply(x: Int, y: Int): Color
  def update(x: Int, y: Int, col: Color): Unit

  def width: Int
  def height: Int

  /**
    * Saves this image
    *
    * @return true if the image was saved, false otherwise
    */
  def save(): Boolean = {
    val success = _imageSaver.exists(_.save())
    if (success) {
      hasChanged() = false
      notifyListeners(_.onImageSourceSaved(true))
    }
    success
  }

  protected var _imageSaver: Option[ImageSaver] = None
  def imageSaver: ImageSaver = _imageSaver.orNull
  def imageSaver_=(saver: ImageSaver): Unit = _imageSaver = Option(saver)

  final def changed: Boolean = hasChanged()
  final def changedProperty: ReadOnlyBooleanProperty = hasChanged.readOnlyProperty
  protected final var hasChanged: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)

  final def apply(c: (Int, Int)): Color = apply(c._1, c._2)
  final def update(c: (Int, Int), col: Color): Unit = update(c._1, c._2, col)
}
