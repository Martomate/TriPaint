package com.martomate.tripaint

import com.martomate.tripaint.util.{InjectiveHashMap, InjectiveMap}

class InjectiveHashMapTest extends InjectiveMapTest {
  override def createMap[L, R]: InjectiveMap[L, R] = new InjectiveHashMap[L, R]
}
