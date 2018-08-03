package com.martomate.tripaint.image.effects

import com.martomate.tripaint.image.storage.ImageStorage

trait Effect {
  def name: String
  def action(image: ImageStorage): Unit
}
