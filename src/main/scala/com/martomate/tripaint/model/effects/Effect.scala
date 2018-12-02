package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.storage.ImageStorage

trait Effect {
  def name: String
  def action(image: ImageStorage): Unit
}
