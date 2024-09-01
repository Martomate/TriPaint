package tripaint.view.image

import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import tripaint.color.Color
import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TriImageCanvas(initWidth: Double, private val imageSize: Int) : Canvas(initWidth, initWidth * Math.sqrt(3.0) / 2) {
    private val sc = 2

    private val coordsToRealConverter =
        TriangleCoordsToReal(imageSize) { xx, yy -> Pair(xx * width, yy * height) }

    fun setCanvasSize(width: Double) {
        this.width = width
        this.height = width * sqrt(3.0) / 2
    }

    fun setCanvasLocationUsingCenter(centerX: Double, centerY: Double) {
        // adjustment caused by canvas center not being the wanted rotation center (i.e. the centroid)
        val adjLen = this.height / 6
        val angle = this.rotate / 180 * Math.PI
        val dx = -adjLen * sin(angle)
        val dy = -adjLen * cos(angle)
        this.relocate(centerX - this.width / 2 + dx, centerY - this.height / 2 + dy)
    }

    fun clearCanvas() {
        getGraphicsContext2D().clearRect(-1.0, -1.0, width + 1, height + 1)
    }

    fun drawTriangle(coords: TriangleCoords, color: Color, pixels: ImageStorage) {
        val (lo, hi) = triangleBoundingRect(coords)
        val (xLo, yLo) = lo
        val (xHi, yHi) = hi

        redraw(pixels, xLo.toInt() - 1, yLo.toInt() - 1, xHi.toInt() + 2, yHi.toInt() + 2)
    }

    fun redraw(pixels: ImageStorage) {
        redraw(pixels, 0, 0, width.toInt(), height.toInt())
    }

    @Synchronized
    fun redraw(pixels: ImageStorage, xLo: Int, yLo: Int, xHi: Int, yHi: Int) {
        val gc = getGraphicsContext2D()
        val indexMap = IndexMap(imageSize)

        val x0 = xLo * sc
        val y0 = yLo * sc
        val x1 = xHi * sc
        val y1 = yHi * sc

        val width = this.width * sc
        val height = this.height * sc

        val image = IntArray(16 * sc * 16 * sc)
        val f = PixelFormat.getIntArgbInstance()

        gc.clearRect((xLo + 1).toDouble(), (yLo + 1).toDouble(), (xHi - xLo - 2).toDouble(), (yHi - yLo - 2).toDouble())

        for (yc in y0 until y1 step 16 * sc) {
            val h = (y1 - yc).coerceAtMost(16 * sc)
            for (xc in x0 until x1 step 16 * sc) {
                val w = (x1 - xc).coerceAtMost(16 * sc)

                for (dy in 0 until h) {
                    val y = yc + dy
                    for (dx in 0 until w) {
                        val x = xc + dx

                        val coords = indexMap.coordsAt(x.toDouble() / width, y.toDouble() / height)
                        val col = if (coords != null) pixels.getColor(coords).withAlpha(1.0).toInt() else 0
                        image[dx + dy * 16 * sc] = col
                    }
                }
                val im = WritableImage(16 * sc, 16 * sc)
                im.getPixelWriter().setPixels(0, 0, w, h, f, image, 0, 16 * sc)
                gc.drawImage(im, 0.0, 0.0, w.toDouble(), h.toDouble(), (xc / sc).toDouble(), (yc / sc).toDouble(), (w / sc).toDouble(), (h / sc).toDouble())
            }
        }
    }

    private fun triangleBoundingRect(coords: TriangleCoords): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        val (px, py) = coordsToRealConverter.triangleCornerPoints(coords)

        var (xLo, yLo) = Pair(1.0e9, 1.0e9)
        var (xHi, yHi) = Pair(0.0, 0.0)

        for (i in 0 until 3) {
            val x = px[i]
            val y = py[i]

            if (x < xLo) xLo = x
            if (y < yLo) yLo = y
            if (x > xHi) xHi = x
            if (y > yHi) yHi = y
        }

        return Pair(Pair(xLo, yLo), Pair(xHi, yHi))
    }
}
