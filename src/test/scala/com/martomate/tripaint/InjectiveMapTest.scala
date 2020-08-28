package com.martomate.tripaint

import com.martomate.tripaint.util.InjectiveMap
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class InjectiveMapTest extends AnyFlatSpec with Matchers {
  def createMap[L, R]: InjectiveMap[L, R]

  "InjectiveMap" should "work with L = R" in {
    val map = createMap[Int, Int]
    map.set(6, 1)
    map.set(2, 134)
    map.set(1, 2)
    map.getRight(2) shouldBe Some(134)
    map.getLeft(1) shouldBe Some(6)
    map.set(2, 1)
    map.getRight(6) shouldBe None
    map.getLeft(1) shouldBe Some(2)
    map.getLeft(134) shouldBe None
  }

  "getRight" should "return None for an empty map" in {
    val map = createMap[String, Int]
    map.getRight("") shouldBe None
  }

  it should "return the value mapped to by 'left'" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.getRight("") shouldBe Some(0)
  }

  "getLeft" should "return None for an empty map" in {
    val map = createMap[String, Int]
    map.getLeft(0) shouldBe None
  }

  it should "return the value mapped to by 'right'" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.getLeft(0) shouldBe Some("")
  }

  "set" should "return true for an empty map" in {
    val map = createMap[String, Int]
    map.set("", 0) shouldBe true
  }

  it should "return false if the mapping already exists" in {
    val map = createMap[String, Int]
    map.set("", 0) shouldBe true
    map.set("", 0) shouldBe false
    map.set("", 0) shouldBe false
  }

  it should "be able to store several mappings" in {
    val map = createMap[String, Int]
    map.set("", 1)
    map.set("hello", 134)
    map.set("str", 2)
    map.getRight("hello") shouldBe Some(134)
    map.getLeft(1) shouldBe Some("")
    map.set("hello", 1)
    map.getRight("") shouldBe None
    map.getLeft(1) shouldBe Some("hello")
    map.getLeft(134) shouldBe None
  }

  "containsRight" should "return false for an empty map" in {
    val map = createMap[String, Int]
    map.containsRight(0) shouldBe false
  }

  it should "return true for an existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.containsRight(0) shouldBe true
  }

  it should "return false for a non-existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.containsRight(1) shouldBe false
  }

  "containsLeft" should "return false for an empty map" in {
    val map = createMap[String, Int]
    map.containsLeft("") shouldBe false
  }

  it should "return true for an existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.containsLeft("") shouldBe true
  }

  it should "return false for a non-existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.containsLeft("a") shouldBe false
  }

  "removeRight" should "return false for an empty map" in {
    val map = createMap[String, Int]
    map.removeRight(0) shouldBe false
  }

  it should "return true for an existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeRight(0) shouldBe true
  }

  it should "return false for a non-existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeRight(1) shouldBe false
  }

  it should "remove the entire mapping, not only the right value" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeRight(0)
    map.containsLeft("") shouldBe false
    map.containsRight(0) shouldBe false
  }

  it should "only return true once for repeated removes" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeRight(0) shouldBe true
    map.removeRight(0) shouldBe false
    map.removeRight(0) shouldBe false
  }

  "removeLeft" should "return false for an empty map" in {
    val map = createMap[String, Int]
    map.removeLeft("") shouldBe false
  }

  it should "return true for an existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeLeft("") shouldBe true
  }

  it should "return false for a non-existing mapping" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeLeft("a") shouldBe false
  }

  it should "remove the entire mapping, not only the left value" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeLeft("")
    map.containsLeft("") shouldBe false
    map.containsRight(0) shouldBe false
  }

  it should "only return true once for repeated removes" in {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeLeft("") shouldBe true
    map.removeLeft("") shouldBe false
    map.removeLeft("") shouldBe false
  }
}
