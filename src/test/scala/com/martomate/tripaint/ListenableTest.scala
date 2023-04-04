package com.martomate.tripaint

import com.martomate.tripaint.util.Listenable
import munit.FunSuite

import scala.collection.mutable.ArrayBuffer

class ListenableTest extends FunSuite {
  class LocalListenable[T] extends Listenable[T] {
    def testNotify(func: T => Unit): Unit = notifyListeners(func)
  }
  test("addListener should notify it's listeners") {
    val listenable = new LocalListenable[Int]
    listenable.addListener(7)
    var success = false
    listenable.testNotify(a => success = a == 7)
    assert(success)
  }

  test("addListener should not notify if no listeners were added") {
    val listenable = new LocalListenable[Int]
    var success = false
    listenable.testNotify(_ => success = true)
    assert(!success)
  }

  test("addListener should notify ALL listeners once") {
    val listenable = new LocalListenable[Int]
    listenable.addListener(7)
    listenable.addListener(9)
    listenable.addListener(2)

    val nums = ArrayBuffer.empty[Int]
    listenable.testNotify(nums += _)
    assertEquals(nums.sortBy(a => a).toSeq, Seq(2, 7, 9))
  }

  test("removeListener should remove the listener") {
    val listenable = new LocalListenable[Int]
    listenable.addListener(7)
    listenable.addListener(9)
    listenable.addListener(2)

    listenable.removeListener(9)

    val nums = ArrayBuffer.empty[Int]
    listenable.testNotify(nums += _)
    assertEquals(nums.sortBy(a => a).toSeq, Seq(2, 7))
  }
}
