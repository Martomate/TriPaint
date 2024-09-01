package tripaint.grid

import tripaint.Change

class UndoManager {
    private val changes: MutableList<Change> = mutableListOf()
    private var redoIndex = 0

    private fun canUndo(): Boolean = redoIndex > 0

    fun undo(): Boolean {
        return if (canUndo()) {
            redoIndex -= 1
            changes[redoIndex].undo()
            true
        } else {
            false
        }
    }

    private fun canRedo(): Boolean = redoIndex <= changes.size - 1

    fun redo(): Boolean {
        return if (canRedo()) {
            redoIndex += 1
            changes[redoIndex - 1].redo()
            true
        }else {
            false
        }
    }

    fun append(change: Change) {
        if (canRedo()) {
            while (changes.size >= redoIndex) {
                changes.removeLast()
            }
        }
        changes.add(change)
        redoIndex += 1
    }
}
