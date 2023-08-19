package com.martomate.tripaint.util

class InjectiveHashMapTest extends InjectiveMapTest {
  override def createMap[L, R]: InjectiveMap[L, R] = new InjectiveHashMap[L, R]
}
