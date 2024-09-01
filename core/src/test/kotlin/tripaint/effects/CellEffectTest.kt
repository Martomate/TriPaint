package tripaint.effects

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.TriangleCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImageStorage
import kotlin.test.Test

class CellEffectTest {
    @Test
    fun `cell effect works for bottom triangles`() {
        val image = ImageStorage.fill(4, Color.Black)
        image.setColor(TriangleCoords.from(0, 0), Color.Cyan)
        image.setColor(TriangleCoords.from(0, 1), Color.Red)
        image.setColor(TriangleCoords.from(1, 1), Color.Green)
        image.setColor(TriangleCoords.from(2, 1), Color.Blue)

        val grid = ImageGrid(4)
        grid.set(GridCell(GridCoords.from(2, 0), image))

        CellEffect().action(listOf(GridCoords.from(2, 0)), grid)

        val c = Color.fromInt(((Color.Cyan + Color.Red + Color.Green + Color.Blue) / 4.0).toInt())
        assertEquals(c, image.getColor(TriangleCoords.from(0, 0)))
        assertEquals(c, image.getColor(TriangleCoords.from(0, 1)))
        assertEquals(c, image.getColor(TriangleCoords.from(1, 1)))
        assertEquals(c, image.getColor(TriangleCoords.from(2, 1)))
    }

    @Test
    fun `cell effect works for top triangles`() {
        val image = ImageStorage.fill(4, Color.Black)
        image.setColor(TriangleCoords.from(0, 0), Color.Cyan)
        image.setColor(TriangleCoords.from(0, 1), Color.Red)
        image.setColor(TriangleCoords.from(1, 1), Color.Green)
        image.setColor(TriangleCoords.from(2, 1), Color.Blue)

        val grid = ImageGrid(4)
        grid.set(GridCell(GridCoords.from(1, 0), image))

        CellEffect().action(listOf(GridCoords.from(1, 0)), grid)

        val c = Color.fromInt(((Color.Cyan + Color.Red + Color.Green + Color.Blue) / 4.0).toInt())
        assertEquals(c, image.getColor(TriangleCoords.from(0, 0)))
        assertEquals(c, image.getColor(TriangleCoords.from(0, 1)))
        assertEquals(c, image.getColor(TriangleCoords.from(1, 1)))
        assertEquals(c, image.getColor(TriangleCoords.from(2, 1)))
    }
}