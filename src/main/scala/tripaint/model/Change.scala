package tripaint.model

trait Change {
  def undo(): Unit

  def redo(): Unit
}
