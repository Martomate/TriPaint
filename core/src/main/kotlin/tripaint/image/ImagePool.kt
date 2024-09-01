package tripaint.image

import tripaint.coords.StorageCoords
import tripaint.image.format.StorageFormat
import tripaint.util.InjectiveHashMap
import tripaint.util.InjectiveMap
import java.io.File

/** This class should keep track of Map[SaveLocation, ImageStorage] So if SaveAs then this class
 * should be called so that collisions can be detected and dealt with, like e.g. when you save A
 * into the same place as B the user should be asked which image to keep, and after that they will
 * share the same ImageStorage.
 */
class ImagePool {
    private val mapping: InjectiveMap<SaveLocation, ImageStorage> = InjectiveHashMap()
    private val saveInfo: MutableMap<ImageStorage, SaveInfo> = mutableMapOf()

    fun locationOf(image: ImageStorage): SaveLocation? = mapping.getLeft(image)

    fun getSaveLocationAndInfo(image: ImageStorage): Pair<SaveLocation?, SaveInfo?> {
        return Pair(mapping.getLeft(image), saveInfo.get(image))
    }

    fun imageAt(location: SaveLocation): ImageStorage? = mapping.getRight(location)

    fun set(image: ImageStorage, location: SaveLocation, info: SaveInfo) {
        val right = mapping.getRight(location)
        if (right != null) {
            this.remove(right)
        }
        mapping.set(location, image)
        saveInfo[image] = info
    }

    fun remove(image: ImageStorage) {
        mapping.removeRight(image)
        saveInfo.remove(image)
    }

    data class SaveInfo(val format: StorageFormat)
    data class SaveLocation(val file: File, val offset: StorageCoords = StorageCoords.from(0, 0))
}