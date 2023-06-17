package com.martomate.tripaint

import munit.Assertions.assertEquals

object ExtraAsserts {
  extension [T](s: Seq[T]) private def occurrences = s.groupMapReduce(identity)(_ => 1)(_ + _)

  def assertSameElementsIgnoringOrder[T](left: Seq[T], right: Seq[T])(using munit.Location): Unit =
    assertEquals(left.occurrences, right.occurrences)
}
