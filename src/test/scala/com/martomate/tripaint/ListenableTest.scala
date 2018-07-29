package com.martomate.tripaint

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

class ListenableTest extends FlatSpec with Matchers {
  class LocalListenable[T] extends Listenable[T] {
    def testNotify(func: T => Unit): Unit = notifyListeners(func)
  }
  "addListener" should "notify it's listeners" in {
    val listenable = new LocalListenable[Int]
    listenable.addListener(7)
    var success = false
    listenable.testNotify(a => success = a == 7)
    success shouldBe true
  }

  it should "not notify if no listeners were added" in {
    val listenable = new LocalListenable[Int]
    var success = false
    listenable.testNotify(_ => success = true)
    success shouldBe false
  }

  it should "notify ALL listeners once" in {
    val listenable = new LocalListenable[Int]
    listenable.addListener(7)
    listenable.addListener(9)
    listenable.addListener(2)

    val nums = ArrayBuffer.empty[Int]
    listenable.testNotify(nums += _)
    nums.sortBy(a => a) shouldBe Seq(2, 7, 9)
  }

  "removeListener" should "remove the listener" in {
    val listenable = new LocalListenable[Int]
    listenable.addListener(7)
    listenable.addListener(9)
    listenable.addListener(2)

    listenable.removeListener(9)

    val nums = ArrayBuffer.empty[Int]
    listenable.testNotify(nums += _)
    nums.sortBy(a => a) shouldBe Seq(2, 7)
  }
}
