package com.martomate.tripaint.image.effects

import com.martomate.tripaint.image.storage.ImageStorage
import scalafx.scene.paint.Color

trait Effect {
  def name: String
  def action(image: ImageStorage): Unit
}
