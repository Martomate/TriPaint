package tripaint.undo

trait Change {
	def description: String
  def undo: Boolean
  def redo: Boolean
}