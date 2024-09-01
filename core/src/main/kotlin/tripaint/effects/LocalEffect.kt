package tripaint.effects

import tripaint.ColorLookup
import tripaint.FloodFillSearcher
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords
import tripaint.coords.GridCoords
import tripaint.coords.PixelCoords
import tripaint.grid.ImageGrid
import tripaint.grid.ImageGridColorLookup

abstract class LocalEffect : Effect {

    protected abstract fun predicate(image: ColorLookup, here: GlobalPixCoords): (coords: GlobalPixCoords, color: Color) -> Boolean

    protected abstract fun weightedColor(image: ColorLookup, here: GlobalPixCoords): (coords: GlobalPixCoords) -> Pair<Double, Color>

    override fun action(images: List<GridCoords>, grid: ImageGrid) {
        val colorLookup = ImageGridColorLookup(grid)

        val searcher = FloodFillSearcher(colorLookup)
        val allChanges = images.map { imageCoords ->
            val image = grid.apply(imageCoords)!!.storage

            val newVals = image.allPixels().map { here ->
                val coords = PixelCoords.from(here, imageCoords)
                val coordsGlobal = coords.toGlobal(grid.imageSize)

                val cols = searcher
                    .search(coordsGlobal, predicate(colorLookup, coordsGlobal))
                    .map(weightedColor(colorLookup, coordsGlobal))

                val numCols = cols.fold(0.0) { acc, b -> acc + b.first }
                Pair(here, (cols.map { (w, c) -> c * w }.reduce { a, b -> a + b } / numCols))
            }
            Pair(imageCoords, newVals)
        }
        for ((im, vals) in allChanges) {
            val image = grid.apply(im)!!.storage
            for ((coords, color) in vals) {
                image.setColor(coords, color)
            }
        }
    }
}
