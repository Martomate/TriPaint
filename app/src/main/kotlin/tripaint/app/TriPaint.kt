package tripaint.app

import javafx.application.Application
import javafx.stage.Stage

object TriPaint {
    @JvmStatic
    fun main(args: Array<String>) {
        // By creating a custom main method it becomes possible to perform init code before the app starts
        Application.launch(App::class.java, *args)
    }

    class App : Application() {
        override fun start(stage: Stage) {
            MainController(stage, FileSystem.create())

            stage.show()
        }
    }
}
