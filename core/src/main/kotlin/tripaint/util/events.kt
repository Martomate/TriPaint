package tripaint.util

fun interface Tracker<E> {
    fun notify(event: E)

    companion object {
        fun <E> withStorage(): TrackerWithStorage<E> = TrackerWithStorage()
    }
}

typealias RevokeFn = () -> Unit

/** This Tracker stores the incoming events in an array */
class TrackerWithStorage<E> : Tracker<E> {
    private val _events: MutableList<E> = mutableListOf()

    val events: List<E>
        get() = _events.toList()

    override fun notify(event: E) {
        _events += event
    }
}

class EventDispatcher<E> {
    private val trackers: MutableList<Tracker<E>> = mutableListOf()

    fun track(tracker: Tracker<E>): RevokeFn {
        trackers += tracker
        return { trackers -= tracker }
    }

    fun notify(event: E) {
        for (t in trackers) {
            t.notify(event)
        }
    }
}
