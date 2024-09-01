package tripaint.effects

import tripaint.coords.GridCoords
import tripaint.grid.ImageGrid

interface Effect {
    fun name(): String
    fun action(images: List<GridCoords>, grid: ImageGrid)
}
