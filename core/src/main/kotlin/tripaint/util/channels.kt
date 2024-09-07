package tripaint.util


class Receiver<E> internal constructor() {
    private val q = ArrayDeque<E>()

    private var listener: ((E) -> Unit)? = null

    internal fun enqueue(event: E) {
        if (this.listener != null) {
            this.listener?.invoke(event)
        } else {
            this.q.addLast(event)
        }
    }

    fun onEvent(f: (E) -> Unit) {
        while (!this.q.isEmpty()) {
            f(this.q.removeFirst())
        }

        this.listener = f
    }

    fun clearBuffer() {
        q.clear()
    }
}

class Sender<E> internal constructor(private val rx: Receiver<E>) {
    fun send(event: E) {
        rx.enqueue(event)
    }
}

class Resource<T> internal constructor(init: T, source: Receiver<T>) {
    private var _value: T = init
    val value: T
        get() = _value

    private val dispatcher = EventDispatcher<Pair<T, T>>()
    fun onChange(f: Tracker<Pair<T, T>>) {
        dispatcher.track(f)
    }

    init {
        source.onEvent { newValue ->
            val oldValue = this._value
            this._value = newValue
            dispatcher.notify(Pair(oldValue, newValue))
        }
    }

    companion object {
        fun <E> createChannel(): Pair<Sender<E>, Receiver<E>> {
            val rx = Receiver<E>()
            val tx = Sender(rx)
            return Pair(tx, rx)
        }

        fun <T> createResource(init: T): Pair<Resource<T>, (value: T) -> Unit> {
            val (tx, rx) = createChannel<T>()
            return Pair(Resource(init, rx)) { v -> tx.send(v) }
        }
    }
}
