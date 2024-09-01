package tripaint.util

/** An `InjectiveMap` is like a bidirectional Map with both unique keys and unique values. This
 * makes the mapping injective.<br /> It can be seen as a collection of pairs `(l, r)` where both l
 * and r can be used to find, update and remove the mappings.<br /> If you call e.g.
 * `removeLeft(l)`, then the pair `(l, r)` will be removed (which means that r is removed too, and
 * since it doesn't exist in any other pair it is no longer present in this map).<br />
 *
 * @tparam L
 *   The Left type
 * @tparam R
 *   The Right type
 */
interface InjectiveMap<L, R> {
    fun getRight(left: L): R?
    fun getLeft(right: R): L?

    fun set(left: L, right: R): Boolean

    fun containsLeft(left: L): Boolean
    fun containsRight(right: R): Boolean

    fun removeRight(right: R): Boolean
    fun removeLeft(left: L): Boolean
}
