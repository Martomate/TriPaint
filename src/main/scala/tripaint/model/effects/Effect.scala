package tripaint.model.effects

import tripaint.model.ImageGrid
import tripaint.model.coords.GridCoords

trait Effect {
  def name: String
  def action(images: Seq[GridCoords], grid: ImageGrid): Unit
}
