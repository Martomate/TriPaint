package tripaint.util

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Tracker[E] {
  def notify(event: E): Unit
}

object Tracker {
  type RevokeFn = () => Unit

  def withStorage[E]: TrackerWithStorage[E] = new TrackerWithStorage[E]
}

/** This Tracker stores the incoming events in an array */
class TrackerWithStorage[E] extends Tracker[E] {
  private val _events: ArrayBuffer[E] = ArrayBuffer.empty

  def events: Seq[E] = _events.toSeq

  def notify(event: E): Unit = _events += event
}

class EventDispatcher[E] {
  private val trackers: ArrayBuffer[Tracker[E]] = ArrayBuffer.empty

  def track(tracker: Tracker[E]): Tracker.RevokeFn = {
    trackers += tracker
    () => trackers -= tracker
  }

  def notify(event: E): Unit = for t <- trackers do t.notify(event)
}

class Receiver[E] private[util] {
  private val q = mutable.Queue.empty[E]

  private var listener: Option[E => Unit] = None

  private[util] def enqueue(event: E): Unit = {
    if this.listener.isDefined then {
      this.listener.get.apply(event)
    } else {
      this.q.enqueue(event)
    }
  }

  def onEvent(f: E => Unit): Unit = {
    while this.q.nonEmpty do {
      f(this.q.dequeue())
    }

    this.listener = Some(f)
  }

  def clearBuffer(): Unit = {
    q.clear()
  }
}

class Sender[E] private[util] {
  private[util] var rx: Receiver[E] = null.asInstanceOf[Receiver[E]]

  def send(event: E): Unit = {
    rx.enqueue(event)
  }
}

class Resource[T] private[util] (init: T, source: Receiver[T]) {
  private var _value: T = init
  def value: T = _value

  private val dispatcher = EventDispatcher[(T, T)]()
  def onChange(f: Tracker[(T, T)]): Unit = {
    dispatcher.track(f)
  }

  source.onEvent { newValue =>
    val oldValue = this._value
    this._value = newValue
    dispatcher.notify((oldValue, newValue))
  }
}

def createChannel[E](): (Sender[E], Receiver[E]) = {
  val rx = new Receiver[E]
  val tx = new Sender[E]
  tx.rx = rx
  (tx, rx)
}

def createResource[T](init: T): (Resource[T], T => Unit) = {
  val (tx, rx) = createChannel[T]()
  (Resource(init, rx), v => tx.send(v))
}
