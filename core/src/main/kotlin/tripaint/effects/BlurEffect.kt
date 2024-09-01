package tripaint.effects

import tripaint.ColorLookup
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords
import kotlin.math.exp

class BlurEffect(radius: Int) : LocalEffect() {
    override fun name(): String = "Blur"

    private val radiusSq = radius.toDouble() * radius

    override fun predicate(
        image: ColorLookup,
        here: GlobalPixCoords
    ): (GlobalPixCoords, Color) -> Boolean {
        return { coords, _ -> coords.distanceSq(here) <= radiusSq * 1.5 }
    }

    override fun weightedColor(image: ColorLookup, here: GlobalPixCoords): ((GlobalPixCoords) -> Pair<Double, Color>) {
        return {coords -> Pair(exp(-2 * coords.distanceSq(here) / radiusSq), image.lookup(coords)!!) }
    }
}
