package tripaint.coords

data class StorageCoords(val value: Long) {
    val x: Int
        get() = (value shr 32).toInt()
    val y: Int
        get() = value.toInt()

    companion object {
        fun from(x: Int, y: Int): StorageCoords {
            require(x >= 0)
            require(y >= 0)
            return StorageCoords((x.toLong() shl 32) or y.toLong())
        }
    }
}
