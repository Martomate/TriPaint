package com.martomate.tripaint.gui

import com.martomate.tripaint.{EditMode, ImagePane, TriPaintController, TriPaintView}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{ColorPicker, Label}
import scalafx.scene.layout.{AnchorPane, BorderPane, VBox}
import scalafx.scene.paint.Color

class MainStage extends PrimaryStage with TriPaintView {
  private val controls: TriPaintController = new TriPaintController(this)
  val imageDisplay: ImagePane = new ImagePane(controls.imageGrid)

  private val menuBar: TheMenuBar  = new TheMenuBar(controls)
  private val toolBar: TheToolBar  = new TheToolBar(controls)
  private val toolBox: ToolBox     = new ToolBox
  private val imageTabs: ImageTabs = new ImageTabs(controls)
  private val colorBox: VBox       = makeColorBox()

  title = "TriPaint"
  onCloseRequest = e => {
    if (!controls.do_exit()) e.consume()
  }
  scene = new Scene(720, 720) {
    delegate.getStylesheets.add(getClass.getResource("/styles/application.css").toExternalForm)
    root = new BorderPane {
      top = new VBox(menuBar, toolBar)
      center = new AnchorPane {
        AnchorPane.setAnchors(imageDisplay, 0, 0, 0, 0)
        imageDisplay.clip === this.clip

        AnchorPane.setLeftAnchor(toolBox, 0)
        AnchorPane.setTopAnchor(toolBox, 0)

        AnchorPane.setLeftAnchor(colorBox, 10)
        AnchorPane.setBottomAnchor(colorBox, 10)

        AnchorPane.setRightAnchor(imageTabs, 10)
        AnchorPane.setTopAnchor(imageTabs, 10)
        AnchorPane.setBottomAnchor(imageTabs, 10)
        this.children = Seq(imageDisplay, toolBox, colorBox, imageTabs)
      }
    }
  }

  EditMode.modes
    .filter(_.shortCut != null)
    .foreach(m => scene().getAccelerators.put(m.shortCut, () => m.toolboxButton.fire))

  private def makeColorBox() = {
    //overlay and imageDisplay
    val colorPicker1 = new ColorPicker(new Color(imageDisplay.primaryColor()))
    val colorPicker2 = new ColorPicker(new Color(imageDisplay.secondaryColor()))

    imageDisplay.primaryColor <==> colorPicker1.value
    imageDisplay.secondaryColor <==> colorPicker2.value

    new VBox(
      new Label("Primary color:"),
      colorPicker1,
      new Label("Secondary color:"),
      colorPicker2
    )
  }
}
