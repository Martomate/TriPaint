package tripaint.util

object CachedLoader {

    /** @return `Success((value, foundInCache))` or `Failure(errorDuringLoad)` */
    fun <T> apply(cached: () -> T?, load: () -> Result<T>): Result<Pair<T, Boolean>> {
        val cachedValue = cached()
        return if (cachedValue != null) {
            Result.success(Pair(cachedValue, true))
        } else {
            load().map { Pair(it, false) }
        }
    }
}
