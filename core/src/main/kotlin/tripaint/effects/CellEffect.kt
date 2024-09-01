package tripaint.effects

import tripaint.ColorLookup
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords

class CellEffect : LocalEffect() {
    override fun name(): String = "Cell"

    override fun predicate(
        image: ColorLookup,
        here: GlobalPixCoords
    ): (coords: GlobalPixCoords, color: Color) -> Boolean {
        return { coords, _ -> coords.cell() == here.cell() }
    }

    override fun weightedColor(image: ColorLookup, here: GlobalPixCoords): (coords: GlobalPixCoords) -> Pair<Double, Color> {
        return { coords -> Pair(1.0, image.lookup(coords)!!) }
    }
}
