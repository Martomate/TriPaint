package tripaint.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test

abstract class InjectiveMapTest {
    abstract fun <L, R> createMap(): InjectiveMap<L, R>

    @Test
    fun `works if L and R types are the same`() {
        val map = createMap<Int, Int>()
        map.set(6, 1)
        map.set(2, 134)
        map.set(1, 2)
        assertEquals(134, map.getRight(2))
        assertEquals(6, map.getLeft(1))
        map.set(2, 1)
        assertEquals(null, map.getRight(6))
        assertEquals(2, map.getLeft(1))
        assertEquals(null, map.getLeft(134))
    }

    @Nested
    inner class GetRight {
        @Test
        fun `returns null for empty map`() {
            val map = createMap<String, Int>()
            assertEquals(null, map.getRight(""))
        }

        @Test
        fun `returns the value mapped to by 'left'`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(0, map.getRight(""))
        }
    }

    @Nested
    inner class GetLeft {
        @Test
        fun `returns null for an empty map`() {
            val map = createMap<String, Int>()
            assertEquals(null, map.getLeft(0))
        }

        @Test
        fun `returns the value mapped to by 'right'`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals("", map.getLeft(0))
        }
    }

    @Nested
    inner class Setting {
        @Test
        fun `returns true for an empty map`() {
            val map = createMap<String, Int>()
            assertEquals(true, map.set("", 0))
        }

        @Test
        fun `returns false if the mapping already exists`() {
            val map = createMap<String, Int>()
            assertEquals(true, map.set("", 0))
            assertEquals(false, map.set("", 0))
            assertEquals(false, map.set("", 0))
        }

        @Test
        fun `is able to store several mappings`() {
            val map = createMap<String, Int>()
            map.set("", 1)
            map.set("hello", 134)
            map.set("str", 2)
            assertEquals(134, map.getRight("hello"))
            assertEquals("", map.getLeft(1))
            map.set("hello", 1)
            assertEquals(null, map.getRight(""))
            assertEquals("hello", map.getLeft(1))
            assertEquals(null, map.getLeft(134))
        }
    }

    @Nested
    inner class ContainsRight {
        @Test
        fun `returns false for an empty map`() {
            val map = createMap<String, Int>()
            assertEquals(false, map.containsRight(0))
        }

        @Test
        fun `returns true for an existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(true, map.containsRight(0))
        }

        @Test
        fun `returns false for a non-existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(false, map.containsRight(1))
        }
    }

    @Nested
    inner class ContainsLeft {
        @Test
        fun `returns false for an empty map`() {
            val map = createMap<String, Int>()
            assertEquals(false, map.containsLeft(""))
        }

        @Test
        fun `returns true for an existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(true, map.containsLeft(""))
        }

        @Test
        fun `returns false for a non-existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(false, map.containsLeft("a"))
        }
    }

    @Nested
    inner class RemoveRight {
        @Test
        fun `returns false for an empty map`() {
            val map = createMap<String, Int>()
            assertEquals(false, map.removeRight(0))
        }

        @Test
        fun `returns true for an existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(true, map.removeRight(0))
        }

        @Test
        fun `returns false for a non-existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(false, map.removeRight(1))
        }

        @Test
        fun `removes the entire mapping, not only the right value`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            map.removeRight(0)
            assertEquals(false, map.containsLeft(""))
            assertEquals(false, map.containsRight(0))
        }

        @Test
        fun `only returns true once for repeated removes`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(true, map.removeRight(0))
            assertEquals(false, map.removeRight(0))
            assertEquals(false, map.removeRight(0))
        }
    }

    @Nested
    inner class RemoveLeft {
        @Test
        fun `returns false for an empty map`() {
            val map = createMap<String, Int>()
            assertEquals(false, map.removeLeft(""))
        }

        @Test
        fun `returns true for an existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(true, map.removeLeft(""))
        }

        @Test
        fun `returns false for a non-existing mapping`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(false, map.removeLeft("a"))
        }

        @Test
        fun `removes the entire mapping, not only the left value`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            map.removeLeft("")
            assertEquals(false, map.containsLeft(""))
            assertEquals(false, map.containsRight(0))
        }

        @Test
        fun `only returns true once for repeated removes`() {
            val map = createMap<String, Int>()
            map.set("", 0)
            assertEquals(true, map.removeLeft(""))
            assertEquals(false, map.removeLeft(""))
            assertEquals(false, map.removeLeft(""))
        }
    }
}
