package com.martomate.tripaint.util

import munit.FunSuite

abstract class InjectiveMapTest extends FunSuite {
  def createMap[L, R]: InjectiveMap[L, R]

  test("InjectiveMap should work with L = R") {
    val map = createMap[Int, Int]
    map.set(6, 1)
    map.set(2, 134)
    map.set(1, 2)
    assertEquals(map.getRight(2), Some(134))
    assertEquals(map.getLeft(1), Some(6))
    map.set(2, 1)
    assertEquals(map.getRight(6), None)
    assertEquals(map.getLeft(1), Some(2))
    assertEquals(map.getLeft(134), None)
  }

  test("getRight should return None for an empty map") {
    val map = createMap[String, Int]
    assertEquals(map.getRight(""), None)
  }

  test("getRight should return the value mapped to by 'left'") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.getRight(""), Some(0))
  }

  test("getLeft should return None for an empty map") {
    val map = createMap[String, Int]
    assertEquals(map.getLeft(0), None)
  }

  test("getLeft should return the value mapped to by 'right'") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.getLeft(0), Some(""))
  }

  test("set should return true for an empty map") {
    val map = createMap[String, Int]
    assertEquals(map.set("", 0), true)
  }

  test("set should return false if the mapping already exists") {
    val map = createMap[String, Int]
    assertEquals(map.set("", 0), true)
    assertEquals(map.set("", 0), false)
    assertEquals(map.set("", 0), false)
  }

  test("set should be able to store several mappings") {
    val map = createMap[String, Int]
    map.set("", 1)
    map.set("hello", 134)
    map.set("str", 2)
    assertEquals(map.getRight("hello"), Some(134))
    assertEquals(map.getLeft(1), Some(""))
    map.set("hello", 1)
    assertEquals(map.getRight(""), None)
    assertEquals(map.getLeft(1), Some("hello"))
    assertEquals(map.getLeft(134), None)
  }

  test("containsRight should return false for an empty map") {
    val map = createMap[String, Int]
    assertEquals(map.containsRight(0), false)
  }

  test("containsRight should return true for an existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.containsRight(0), true)
  }

  test("containsRight should return false for a non-existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.containsRight(1), false)
  }

  test("containsLeft should return false for an empty map") {
    val map = createMap[String, Int]
    assertEquals(map.containsLeft(""), false)
  }

  test("containsLeft should return true for an existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.containsLeft(""), true)
  }

  test("containsLeft should return false for a non-existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.containsLeft("a"), false)
  }

  test("removeRight should return false for an empty map") {
    val map = createMap[String, Int]
    assertEquals(map.removeRight(0), false)
  }

  test("removeRight should return true for an existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.removeRight(0), true)
  }

  test("removeRight should return false for a non-existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.removeRight(1), false)
  }

  test("removeRight should remove the entire mapping, not only the right value") {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeRight(0)
    assertEquals(map.containsLeft(""), false)
    assertEquals(map.containsRight(0), false)
  }

  test("removeRight should only return true once for repeated removes") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.removeRight(0), true)
    assertEquals(map.removeRight(0), false)
    assertEquals(map.removeRight(0), false)
  }

  test("removeLeft should return false for an empty map") {
    val map = createMap[String, Int]
    assertEquals(map.removeLeft(""), false)
  }

  test("removeLeft should return true for an existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.removeLeft(""), true)
  }

  test("removeLeft should return false for a non-existing mapping") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.removeLeft("a"), false)
  }

  test("removeLeft should remove the entire mapping, not only the left value") {
    val map = createMap[String, Int]
    map.set("", 0)
    map.removeLeft("")
    assertEquals(map.containsLeft(""), false)
    assertEquals(map.containsRight(0), false)
  }

  test("removeLeft should only return true once for repeated removes") {
    val map = createMap[String, Int]
    map.set("", 0)
    assertEquals(map.removeLeft(""), true)
    assertEquals(map.removeLeft(""), false)
    assertEquals(map.removeLeft(""), false)
  }
}
