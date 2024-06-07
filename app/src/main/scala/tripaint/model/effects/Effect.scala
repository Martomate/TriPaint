package tripaint.model.effects

import tripaint.coords.GridCoords
import tripaint.model.ImageGrid

trait Effect {
  def name: String
  def action(images: Seq[GridCoords], grid: ImageGrid): Unit
}
