package tripaint

interface Change {
    fun undo()

    fun redo()
}
