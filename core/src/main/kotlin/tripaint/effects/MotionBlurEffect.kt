package tripaint.effects

import tripaint.ColorLookup
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords
import kotlin.math.exp
import kotlin.math.pow

class MotionBlurEffect(radius: Int) : LocalEffect() {
    override fun name(): String = "Motion blur"

    private val radiusSq = radius.toDouble() * radius

    override fun predicate(
        image: ColorLookup,
        here: GlobalPixCoords
    ): (coords: GlobalPixCoords, color: Color) -> Boolean {
        return { coords, _ -> here.y == coords.y && (here.x - coords.x).toDouble().pow(2.0) <= radiusSq * 1.5 }
    }

    override fun weightedColor(image: ColorLookup, here: GlobalPixCoords): (
    coords: GlobalPixCoords
    ) -> Pair<Double, Color> {
        return { coords -> Pair(exp(-2 * (here.x - coords.x).toDouble().pow(2.0) / radiusSq), image.lookup(coords)!!) }
    }
}
