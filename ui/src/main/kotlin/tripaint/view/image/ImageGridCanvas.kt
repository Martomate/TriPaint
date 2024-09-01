package tripaint.view.image

import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import tripaint.coords.GlobalPixCoords
import tripaint.coords.GridCoords
import tripaint.coords.PixelCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import kotlin.math.floor
import kotlin.math.sqrt

class ImageGridCanvas(val images: ImageGrid) : Canvas() {
    private var scale: Double = 1.0
    private var displacement: Pair<Double, Double> = Pair(0.0, 0.0)
    private var width: Double = 0.0
    private var height: Double = 0.0

    private val sc = 2 // pixel scale used to ensure shard images on high dpi displays

    init {
        this.widthProperty().addListener { _ -> this.width = getWidth() }
        this.heightProperty().addListener { _ -> this.height = getHeight() }
    }

    fun setScale(scale: Double) {
        this.scale = scale
    }

    fun setDisplacement(dx: Double, dy: Double) {
        this.displacement = Pair(dx, dy)
    }

    fun coordsAt(x: Double, y: Double): GlobalPixCoords {
        val (xScroll, yScroll) = this.displacement

        val yy = -(y - yScroll - this.height / 2) / this.scale / sqrt(3.0)
        val xx = ((x - xScroll - this.width / 2) / this.scale - yy) / 2

        val xInt = floor(xx).toInt()
        val yInt = floor(yy).toInt()

        return if (xx - xInt.toDouble() > 1 - (yy - yInt.toDouble())) {
            GlobalPixCoords.from(xInt * 2 + 1, yInt)
        } else {
            GlobalPixCoords.from(xInt * 2, yInt)
        }
    }

    /** locationOf is the inverse of the coordsAt function */
    fun locationOf(coords: GlobalPixCoords): Pair<Double, Double> {
        val (xScroll, yScroll) = this.displacement

        val (xInt, yInt) = Pair(Math.floorDiv(coords.x, 2), coords.y)
        val (xx, yy) = Pair(xInt + 0.5, yInt + 0.5)

        val y = -yy * sqrt(3.0) * this.scale + this.height / 2 + yScroll
        val x = (xx * 2 + yy) * this.scale + this.width / 2 + xScroll

        return Pair(x, y)
    }

    fun redraw() {
        val w = this.width.toInt()
        val h = this.height.toInt()

        redraw(0, 0, w, h)
    }

    fun redraw(startX: Int, startY: Int, w: Int, h: Int) {
        if (images.imageSize < 0) {
            // This "hack" is needed right at the beginning before the user has specified the size
            return
        }

        val buffer = this.renderRegion(startX, startY, w, h)

        getGraphicsContext2D().clearRect((startX + 1).toDouble(), (startY + 1).toDouble(), (w - 2).toDouble(), (h - 2).toDouble())
        getGraphicsContext2D().drawImage(buffer, 0.0, 0.0, (w * sc).toDouble(), (h * sc).toDouble(), startX.toDouble(), startY.toDouble(), w.toDouble(), h.toDouble())
    }

    private fun calculateCoordsForRegion(startX: Int, startY: Int, w: Int, h: Int): LongArray {
        val imageSize = this.images.imageSize
        val scInv = 1.0 / sc

        val ww = w * sc
        val hh = h * sc
        val buffer = LongArray(ww * hh)

        val y0 = startY * sc
        val x0 = startX * sc
        val y1 = (startY + h) * sc
        val x1 = (startX + w) * sc

        val (xScroll, yScroll) = this.displacement
        val canvasHeight = this.height
        val canvasWidth = this.width
        val canvasInvScale = 1.0 / this.scale
        val invSqrt3 = 1.0 / sqrt(3.0)

        val canvasStartY = yScroll + canvasHeight * 0.5
        val canvasStartX = xScroll + canvasWidth * 0.5

        var yi = y0
        while (yi < y1) {
            var xi = x0
            while (xi < x1) {
                val x = xi * scInv
                val y = yi * scInv

                val yy = -(y - canvasStartY) * canvasInvScale * invSqrt3
                val xx = ((x - canvasStartX) * canvasInvScale - yy) * 0.5

                val xInt = floor(xx).toInt()
                val yInt = floor(yy).toInt()

                var cx = xInt * 2
                val cy = yInt
                if (xx - xInt.toDouble() > 1 - (yy - yInt.toDouble())) {
                    cx += 1
                }

                val c = PixelCoords.fromGlobalCoords(cx, cy, imageSize).value

                buffer[xi - x0 + (yi - y0) * ww] = c

                xi += 1
            }
            yi += 1
        }

        return buffer
    }

    private fun renderRegion(startX: Int, startY: Int, w: Int, h: Int): WritableImage {
        val ww = w * sc
        val hh = h * sc
        val buffer = IntArray(ww * hh) // starts with transparent pixels (value 0)

        var prevCellCoords: GridCoords = GridCoords.from(0x7fff, 0x7fff) // we assume this cell is never used
        var prevCell: GridCell? = null

        val y0 = startY * sc
        val x0 = startX * sc
        val y1 = (startY + h) * sc
        val x1 = (startX + w) * sc

        val stepSize = 64

        val coordsBuffer = this.calculateCoordsForRegion(startX, startY, w, h)

        var ys = y0
        while (ys < y1) {
        val ySteps = (y1 - ys).coerceAtMost(stepSize)

            var xs = x0
            while (xs < x1) {
                val xSteps = (x1 - xs).coerceAtMost(stepSize)

                var dy = 0
                while (dy < ySteps) {
                    val y = ys + dy

                    var dx = 0
                    while (dx < xSteps) {
                        val x = xs + dx

                        val coords = PixelCoords(coordsBuffer[x - x0 + (y - y0) * ww])
                        val cell = if (coords.image == prevCellCoords) {
                            prevCell
                        } else {
                            prevCellCoords = coords.image
                            val cell = images.apply(coords.image)
                            prevCell = cell
                            cell
                        }
                        if (cell != null) {
                            buffer[x - x0 + (y - y0) * ww] = cell.storage.getColorArgb(coords.pix)
                        }

                        dx += 1
                    }
                    dy += 1
                }
                xs += stepSize
            }
            ys += stepSize
        }

        val image = WritableImage(w * sc, h * sc)
        val f = PixelFormat.getIntArgbInstance()
        image.getPixelWriter().setPixels(0, 0, w * sc, h * sc, f, buffer, 0, w * sc)

        return image
    }
}
