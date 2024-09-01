package tripaint

import tripaint.util.InjectiveHashMap
import tripaint.util.InjectiveMap
import tripaint.util.InjectiveMapTest

class InjectiveHashMapTest : InjectiveMapTest() {
    override fun <L, R> createMap(): InjectiveMap<L, R> = InjectiveHashMap<L, R>()
}
