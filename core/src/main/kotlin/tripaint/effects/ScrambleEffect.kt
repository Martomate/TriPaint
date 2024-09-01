package tripaint.effects

import tripaint.coords.GridCoords
import tripaint.grid.ImageGrid
import kotlin.random.Random

object ScrambleEffect : Effect {
    override fun name(): String = "Scramble"

    override fun action(images: List<GridCoords>, grid: ImageGrid) {
        for (imageCoords in images) {
            val image = grid.apply(imageCoords)!!.storage
            val allPixels = image.allPixels()

            val transform = allPixels.zip(Random.shuffled(allPixels).map { image.getColor(it) })

            for ((from, col) in transform) {
                image.setColor(from, col)
            }
        }
    }

    private fun <T> Random.shuffled(xs: List<T>): List<T> {
        val buf = xs.toMutableList()

        fun swap(i1: Int, i2: Int) {
            val tmp = buf[i1]
            buf[i1] = buf[i2]
            buf[i2] = tmp
        }

        for (n in buf.size downTo 2) {
            val k = this.nextInt(n)
            swap(n - 1, k)
        }

        return buf.toList()
    }
}
