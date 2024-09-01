package tripaint.effects

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords
import tripaint.coords.GridCoords
import tripaint.coords.PixelCoords
import tripaint.coords.TriangleCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.grid.ImageGridColorLookup
import tripaint.image.ImageStorage
import kotlin.test.Ignore
import kotlin.test.Test

class BlurEffectTest {
    @Test
    fun `name should be 'Blur'`() {
        assertEquals(BlurEffect(3).name(), "Blur")
    }

    @Test
    fun `the effect should be symmetric far from border`() {
        val radius = 2
        val imageSize = 32
        checkSymmetrySimple(radius, imageSize, TriangleCoords.from(imageSize / 2, imageSize / 2))
        checkSymmetrySimple(radius, imageSize, TriangleCoords.from(imageSize / 2 + 2, imageSize / 2))
        checkSymmetrySimple(radius, imageSize, TriangleCoords.from(imageSize / 2, imageSize / 2 - 2))
        checkSymmetrySimple(radius, imageSize, TriangleCoords.from(imageSize / 2 - 2, imageSize / 2 + 2))
    }

    private fun checkSymmetrySimple(
        radius: Int,
        imageSize: Int,
        dotLocation: TriangleCoords
    ) {
        val effect = BlurEffect(radius)
        val thisImage = GridCoords.from(0, 0)

        val storage = ImageStorage.fill(imageSize, Color.Black)
        storage.setColor(dotLocation, Color.White)

        val grid = ImageGrid(imageSize)
        grid.set(GridCell(thisImage, storage))

        effect.action(listOf(thisImage), grid)

        for (dx in 0..radius) {
            val look1 = TriangleCoords.from(dotLocation.x - dx, dotLocation.y)
            val look2 = TriangleCoords.from(dotLocation.x + dx, dotLocation.y)
            val col1 = storage.getColor(look1)
            val col2 = storage.getColor(look2)
            try {
                assertEquals(col1, col2)
            } catch (e: Exception) {
                println("dx = $dx")
                throw e
            }
        }
    }

    @Test
    fun `the effect should be symmetric on the border between images`() {
        val radius = 2
        val imageSize = 16 // has to be high enough to not limit the search
        checkSymmetryBorder(radius, imageSize, TriangleCoords.from(0, imageSize / 2), GridCoords.from(-1, 0))
        checkSymmetryBorder(
            radius,
            imageSize,
            TriangleCoords.from(imageSize, imageSize / 2),
            GridCoords.from(1, 0)
        )
        checkSymmetryBorder(
            radius,
            imageSize,
            TriangleCoords.from(imageSize, imageSize - 1),
            GridCoords.from(1, -1)
        )
    }

    private fun checkSymmetryBorder(
        radius: Int,
        imageSize: Int,
        dotLocation: TriangleCoords,
        borderingImage: GridCoords
    ) {
        val effect = BlurEffect(radius)
        val thisImage = GridCoords.from(0, 0)

        val storage = ImageStorage.fill(imageSize, Color.Black)
        val storage2 = ImageStorage.fill(imageSize, Color.Black)
        storage.setColor(dotLocation, Color.White)

        val grid = ImageGrid(imageSize)
        grid.set(GridCell(thisImage, storage))
        grid.set(GridCell(borderingImage, storage2))

        effect.action(listOf(thisImage, borderingImage), grid)

        val colorLookup = ImageGridColorLookup(grid)
        val dotGlobal = PixelCoords.from(dotLocation, thisImage).toGlobal(imageSize)

        for (dx in 0..radius) {
            val look1 = GlobalPixCoords.from(dotGlobal.x - dx, dotGlobal.y)
            val look2 = GlobalPixCoords.from(dotGlobal.x + dx, dotGlobal.y)
            val col1 = colorLookup.lookup(look1)!!
            val col2 = colorLookup.lookup(look2)!!
            try {
                assertEquals(col1, col2)
            } catch (e: Exception) {
                println("dx = $dx")
                throw e
            }
        }
    }

    @Ignore
    @Test
    fun `the effect should be additive for multiple sources`() {}
}