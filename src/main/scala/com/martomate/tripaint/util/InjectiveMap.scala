package com.martomate.tripaint.util

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
trait InjectiveMap[L, R] {
  def getRight(left: L): Option[R]
  def getLeft(right: R): Option[L]

  def set(left: L, right: R): Boolean

  def containsLeft(left: L): Boolean
  def containsRight(right: R): Boolean

  def removeRight(right: R): Boolean
  def removeLeft(left: L): Boolean
}
