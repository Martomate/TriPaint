package tripaint.effects

import tripaint.coords.GridCoords
import tripaint.grid.ImageGrid

trait Effect {
  def name: String
  def action(images: Seq[GridCoords], grid: ImageGrid): Unit
}
