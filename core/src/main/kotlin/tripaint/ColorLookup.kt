package tripaint

import tripaint.color.Color
import tripaint.coords.GlobalPixCoords

fun interface ColorLookup {
    fun lookup(coords: GlobalPixCoords): Color?
}
