package com.martomate.tripaint

import scala.collection.mutable

class InjectiveHashMap[L, R] extends InjectiveMap[L, R] {
  private val leftToRight: mutable.Map[L, R] = mutable.Map.empty
  private val rightToLeft: mutable.Map[R, L] = mutable.Map.empty

  override def getRight(left: L): Option[R] = leftToRight.get(left)

  override def getLeft(right: R): Option[L] = rightToLeft.get(right)

  override def set(left: L, right: R): Boolean = {
    val newMapping = !getRight(left).contains(right)
    if (newMapping) {
      removeLeft(left)
      removeRight(right)
      leftToRight.put(left, right)
      rightToLeft.put(right, left)
    }
    newMapping
  }

  override def containsLeft(left: L): Boolean = leftToRight.contains(left)

  override def containsRight(right: R): Boolean = rightToLeft.contains(right)

  override def removeRight(right: R): Boolean = {
    val l = getLeft(right)
    l.foreach(left => removeUnchecked(left, right))
    l.isDefined
  }

  override def removeLeft(left: L): Boolean = {
    val r = getRight(left)
    r.foreach(right => removeUnchecked(left, right))
    r.isDefined
  }

  private def removeUnchecked(left: L, right: R): Unit = {
    leftToRight.remove(left)
    rightToLeft.remove(right)
  }
}
