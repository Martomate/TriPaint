package tripaint.util

import scala.collection.mutable.ArrayBuffer

trait Tracker[E]:
  def notify(event: E): Unit

object Tracker:
  type RevokeFn = () => Unit

  def withStorage[E]: TrackerWithStorage[E] = new TrackerWithStorage[E]

/** This Tracker stores the incoming events in an array */
class TrackerWithStorage[E] extends Tracker[E]:
  private val _events: ArrayBuffer[E] = ArrayBuffer.empty
  def events: Seq[E] = _events.toSeq

  def notify(event: E): Unit = _events += event

class EventDispatcher[E]:
  private val trackers: ArrayBuffer[Tracker[E]] = ArrayBuffer.empty
  def track(tracker: Tracker[E]): Tracker.RevokeFn = {
    trackers += tracker
    () => trackers -= tracker
  }

  def notify(event: E): Unit = for t <- trackers do t.notify(event)
