package tripaint.view.image

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color as FXColor
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import tripaint.FloodFillSearcher
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.PixelCoords
import tripaint.coords.TriangleCoords
import tripaint.grid.*
import tripaint.util.Resource
import tripaint.view.EditMode
import tripaint.view.JavaFxExt.fromFXColor
import kotlin.math.exp
import kotlin.math.hypot

class ImageGridPane(private val imageGrid: ImageGrid, private val currentEditMode: Resource<EditMode>) : Pane() {
    private var xScroll: Double = 0.0
    private var yScroll: Double = 0.0
    private var zoom: Double = 1.0
    private val drag = Drag()

    private class Drag {
        var x: Double = -1.0
        var y: Double = -1.0
    }

    val colors = Colors()
    class Colors {
        val primaryColor: ObjectProperty<Color> = SimpleObjectProperty(Color.Black)
        val secondaryColor: ObjectProperty<Color> = SimpleObjectProperty(Color.White)

        fun setPrimaryColor(col: Color) {
            primaryColor.setValue(col)
        }

        fun setSecondaryColor(col: Color) {
            secondaryColor.setValue(col)
        }

        fun setPrimaryColor(col: FXColor) {
            setPrimaryColor(fromFXColor(col))
        }

        fun setSecondaryColor(col: FXColor) {
            setSecondaryColor(fromFXColor(col))
        }
    }

    private val gridSearcher: FloodFillSearcher = FloodFillSearcher(ImageGridColorLookup(imageGrid))

    private val canvas = ImageGridCanvas(imageGrid)
    init {
        canvas.setScale(zoom)
        canvas.setDisplacement(xScroll, yScroll)

        this.children.add(canvas)

        imageGrid.trackChanges { this.onImageGridEvent(it) }
    }

    private val cumulativeImageChange: MutableMap<GridCoords, ImageChange.Builder> = mutableMapOf()

    private fun stopDrawing(cell: GridCell): ImageChange {
        return cumulativeImageChange[cell.coords]!!.done(cell.storage)
    }

    private fun drawAt(cell: GridCell, coords: TriangleCoords, color: Color) {
        if (cell.storage.contains(coords)) {
            cumulativeImageChange[cell.coords]!!.addChange(coords, cell.storage.getColor(coords), color)
            cell.storage.setColor(coords, color)
        }
    }

    private fun imageAt(x: Double, y: Double): Pair<GridCell, PixelCoords>? {
        val pt = canvas.sceneToLocal(x, y)
        val coords = PixelCoords.from(canvas.coordsAt(pt.x, pt.y), imageGrid.imageSize)
        val im = imageGrid.images().find { it.coords == coords.image }
        return if (im != null) Pair(im, coords) else null
    }

    init {
        this.setOnMouseDragged { e ->
            if (!e.isConsumed) {
                val xDiff = e.x - drag.x
                val yDiff = e.y - drag.y

                drag.x = e.x
                drag.y = e.y

                when (currentEditMode.value) {
                    EditMode.Organize -> { // TODO: implement scale and rotation if (x, y) is close enough to a corner
                        setScroll(xScroll + xDiff, yScroll + yDiff)
                        canvas.redraw()
                    }
                    else -> {
                        val dist = hypot(xDiff, yDiff) / zoom

                        val steps = 4 * dist.toInt() + 1
                        for (i in 1..steps) {
                            val xx = e.sceneX + xDiff / steps * (i - steps)
                            val yy = e.sceneY + yDiff / steps * (i - steps)

                            val im = imageAt(xx, yy)
                            if (im != null) {
                                val (image, coords) = im
                                if (image.editable) {
                                    mousePressedAt(coords, e.button, dragged = true)
                                }
                            }
                        }
                    }
                }
            }
        }

        this.setOnMousePressed { e ->
            if (!e.isConsumed) {
                drag.x = e.x
                drag.y = e.y

                val im = imageAt(e.sceneX, e.sceneY)
                if (im != null) {
                    val (image, coords) = im
                    if (image.editable) {
                        mousePressedAt(coords, e.button, dragged = false)
                    }
                }
            }
        }

        this.setOnMouseReleased { e ->
            if (!e.isConsumed) {
                val changes: MutableMap<GridCoords, ImageChange> = mutableMapOf()
                for (im in imageGrid.images().reversed()) {
                    val change = stopDrawing(im)
                    change.undo()
                    changes[im.coords] = change
                }

                imageGrid.performChange(ImageGridChange(changes))
            }
        }

        this.setOnScroll { e ->
            val (dx, dy) = Pair(e.deltaX, e.deltaY)

            if (e.isControlDown) {
                val factor = exp(e.deltaY * 0.01).coerceAtLeast(1.0 / 32 / zoom).coerceAtMost(32 / zoom)
                zoom *= factor
                canvas.setScale(zoom)
                setScroll(xScroll * factor, yScroll * factor)
            } else {
                setScroll(xScroll + dx, yScroll + dy)
            }

            canvas.redraw()
        }
    }

    private fun setScroll(sx: Double, sy: Double) {
        xScroll = sx
        yScroll = sy
        canvas.setDisplacement(xScroll, yScroll)
    }

    private fun mousePressedAt(
        coords: PixelCoords,
        button: MouseButton,
        dragged: Boolean
    ) {
        val colorToUse = when (button) {
            MouseButton.PRIMARY   -> colors.primaryColor
            MouseButton.SECONDARY -> colors.secondaryColor
            else                  -> null
        }

        val image = imageGrid.apply(coords.image)
        if (image != null) {
            if (colorToUse != null) {
                when (currentEditMode.value) {
                    EditMode.Draw -> drawAt(image, coords.pix, colorToUse.get())
                    EditMode.Fill -> fill(coords, colorToUse.get())
                    EditMode.PickColor -> colorToUse.setValue(image.storage.getColor(coords.pix))
                    else -> {}
                }
            }
        }
    }

    private fun updateCanvasAt(x: Double, y: Double) {
        val (w, h) = Pair(canvas.width.toInt(), canvas.height.toInt())

        val startX = (x - 3 * zoom).toInt().coerceAtLeast(0).coerceAtMost(w - 1)
        val startY = (y - 3 * zoom).toInt().coerceAtLeast(0).coerceAtMost(h - 1)
        val endX = (x + 3 * zoom + 1).toInt().coerceAtLeast(0).coerceAtMost(w - 1)
        val endY = (y + 3 * zoom + 1).toInt().coerceAtLeast(0).coerceAtMost(h - 1)

        if (endX >= startX && endY >= startY) {
            canvas.redraw(startX, startY, endX - startX + 1, endY - startY + 1)
        }
    }

    private fun fill(coords: PixelCoords, color: Color) {
        val image = imageGrid.apply(coords.image)
        if (image != null) {
            val referenceColor = image.storage.getColor(coords.pix)
            val places = gridSearcher
                .search(coords.toGlobal(imageGrid.imageSize)) { _, col -> col == referenceColor }
                .map { p -> PixelCoords.from(p, imageGrid.imageSize) }

            for (p in places) {
                val im = imageGrid.apply(p.image)
                if (im != null) {
                    drawAt(im, p.pix, color)
                }
            }
        }

        canvas.redraw()
    }

    init {
        this.widthProperty().addListener { _ -> updateSize() }
        this.heightProperty().addListener { _ -> updateSize() }
    }

    private fun updateSize() {
        this.clip = Rectangle(0.0, 0.0, width, height)
        canvas.width = width
        canvas.height = height
        canvas.redraw()
    }

    private fun onImageGridEvent(event: ImageGrid.Event) {
        when (event) {
        is ImageGrid.Event.ImageAdded -> {
            cumulativeImageChange[event.image.coords] = ImageChange.Builder()
            canvas.redraw()
        }
        is ImageGrid.Event.ImageRemoved -> {
            cumulativeImageChange -= event.image.coords
            canvas.redraw()
        }
        is ImageGrid.Event.PixelChanged -> {
            val (x, y) = canvas.locationOf(event.coords.toGlobal(imageGrid.imageSize))
            this.updateCanvasAt(x, y)
        }
        is ImageGrid.Event.ImageChangedALot -> {
            // TODO: calculate bounds of the image and only redraw that part of the canvas
            canvas.redraw()
        }
        }
    }
}
