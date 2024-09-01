package tripaint.util

class InjectiveHashMap<L, R> : InjectiveMap<L, R> {
    private val leftToRight: MutableMap<L, R> = mutableMapOf()
    private val rightToLeft: MutableMap<R, L> = mutableMapOf()

    override fun getRight(left: L): R? = leftToRight[left]

    override fun getLeft(right: R): L? = rightToLeft[right]

    override fun set(left: L, right: R): Boolean {
        val newMapping = getRight(left) != right
        if (newMapping) {
            removeLeft(left)
            removeRight(right)
            leftToRight[left] = right
            rightToLeft[right] = left
        }
        return newMapping
    }

    override fun containsLeft(left: L): Boolean = leftToRight.contains(left)

    override fun containsRight(right: R): Boolean = rightToLeft.contains(right)

    override fun removeRight(right: R): Boolean {
        val left = getLeft(right)
        if (left != null) {
            removeUnchecked(left, right)
            return true
        } else {
            return false
        }
    }

    override fun removeLeft(left: L): Boolean {
        val right = getRight(left)
        if (right != null) {
            removeUnchecked (left, right)
            return true
        } else {
            return false
        }
    }

    private fun removeUnchecked(left: L, right: R) {
        leftToRight.remove(left)
        rightToLeft.remove(right)
    }
}
