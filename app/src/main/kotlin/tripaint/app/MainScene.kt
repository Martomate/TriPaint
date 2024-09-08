package tripaint.app

import javafx.scene.Scene
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.control.ToolBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import tripaint.color.Color
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.util.MutableResource
import tripaint.view.EditMode
import tripaint.view.JavaFxExt.fromFXColor
import tripaint.view.JavaFxExt.toFXColor
import tripaint.view.TriPaintViewListener
import tripaint.view.gui.ImageTabs
import tripaint.view.gui.TheMenuBar
import tripaint.view.gui.TheToolBar
import tripaint.view.gui.ToolBox
import tripaint.view.image.ImageGridPane

class MainScene(private val scene: Scene, private val shouldExit: () -> Boolean) {
    fun mount(stage: Stage) {
        stage.title = "TriPaint"
        stage.setOnCloseRequest { e ->
            if (!shouldExit()) e.consume()
        }
        stage.setScene(scene)
    }

    companion object {
        fun create(
            controls: TriPaintViewListener,
            imagePool: ImagePool,
            imageGrid: ImageGrid,
            currentEditMode: MutableResource<EditMode>,
            primaryColor: MutableResource<Color>,
            secondaryColor: MutableResource<Color>,
        ): MainScene {
            val imageDisplay = ImageGridPane(imageGrid, currentEditMode.component1(), primaryColor, secondaryColor)

            val menuBar: MenuBar = TheMenuBar.create(controls)
            val toolBar: ToolBar = TheToolBar.create(controls)
            val toolBox: TilePane = ToolBox.create(EditMode.all(), currentEditMode)
            val imageTabs: TilePane = ImageTabs.fromImagePool(imageGrid, imagePool, controls::requestImageRemoval)
            val colorBox: VBox = makeColorBox(primaryColor, secondaryColor)

            val centerPane = AnchorPane(imageDisplay, toolBox, colorBox, imageTabs)
            AnchorPane.setTopAnchor(imageDisplay, 0.0)
            AnchorPane.setRightAnchor(imageDisplay, 0.0)
            AnchorPane.setBottomAnchor(imageDisplay, 0.0)
            AnchorPane.setLeftAnchor(imageDisplay, 0.0)
            imageDisplay.clipProperty().isEqualTo(centerPane.clipProperty())

            AnchorPane.setLeftAnchor(toolBox, 0.0)
            AnchorPane.setTopAnchor(toolBox, 0.0)

            AnchorPane.setLeftAnchor(colorBox, 10.0)
            AnchorPane.setBottomAnchor(colorBox, 10.0)

            AnchorPane.setRightAnchor(imageTabs, 10.0)
            AnchorPane.setTopAnchor(imageTabs, 10.0)
            AnchorPane.setBottomAnchor(imageTabs, 10.0)

            val topPane = VBox(menuBar, toolBar)

            val sceneContents = BorderPane(centerPane, topPane, null, null, null)

            val scene = Scene(sceneContents, 720.0, 720.0)
            scene.stylesheets.add(MainScene::class.java.getResource("/styles/application.css")!!.toExternalForm())

            for (m in EditMode.all()) {
                if (m.shortCut != null) {
                    scene.accelerators[m.shortCut] = Runnable { currentEditMode.value = m }
                }
            }

            return MainScene(scene) { controls.requestExit() }
        }

        private fun makeColorBox(primaryColor: MutableResource<Color>, secondaryColor: MutableResource<Color>): VBox {
            // overlay and imageDisplay
            val colorPicker1 = ColorPicker(primaryColor.value.toFXColor())
            val colorPicker2 = ColorPicker(secondaryColor.value.toFXColor())

            colorPicker1.valueProperty().addListener { _, from, to ->
                if (from != to) primaryColor.value = fromFXColor(to)
            }

            colorPicker2.valueProperty().addListener { _, from, to ->
                if (from != to) secondaryColor.value = fromFXColor(to)
            }

            primaryColor.onChange { (from, to) ->
                if (from != to) colorPicker1.value = to.toFXColor()
            }

            secondaryColor.onChange { (from, to) ->
                if (from != to) colorPicker2.value = to.toFXColor()
            }

            return VBox(
                Label("Primary color:"),
                colorPicker1,
                Label("Secondary color:"),
                colorPicker2
            )
        }
    }
}
