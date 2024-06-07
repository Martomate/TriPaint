package tripaint

trait Change {
  def undo(): Unit

  def redo(): Unit
}
