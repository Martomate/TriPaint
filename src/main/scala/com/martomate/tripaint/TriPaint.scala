package com.martomate.tripaint

import com.martomate.tripaint.image.TriImage
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.ReadOnlyBooleanProperty
import scalafx.geometry.{Orientation, Pos}
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.paint.Color

object TriPaint extends JFXApp with TriPaintView {
  private val controls: TriPaintController = new TriPaintController

  private val imageTabs = new TilePane
  imageTabs.maxWidth = TriImage.previewSize

  val imageDisplay: ImagePane = new ImagePane(new ImageCollageImplOld(32))

  private val toolbox = new TilePane
  toolbox.orientation = Orientation.Vertical
  toolbox.children = EditMode.modes.map(_.toolboxButton)

  private def makeMenu(text: String, menuItems: MenuItem*): Menu = {
    val menu = new Menu(text)
    menu.items = menuItems
    menu
  }

  private val menu_file = makeMenu("File",
    controls.New.menuItem,
    controls.NewComp.menuItem,
    controls.Open.menuItem,
    new SeparatorMenuItem,
    controls.Save.menuItem,
    controls.SaveAs.menuItem,
    new SeparatorMenuItem,
    controls.Exit.menuItem
  )

  private val menu_edit = makeMenu("Edit",
    controls.Undo.menuItem,
    controls.Redo.menuItem,
    new SeparatorMenuItem,
    controls.Cut.menuItem,
    controls.Copy.menuItem,
    controls.Paste.menuItem,
  )

  private val menu_organize = makeMenu("Organize",
    controls.Move.menuItem,
    controls.Scale.menuItem,
    controls.Rotate.menuItem,
    controls.Fit.menuItem
  )

  private val menu_effects = makeMenu("Effects",
    controls.Blur.menuItem,
    controls.MotionBlur.menuItem,
    controls.PerlinNoise.menuItem,
    controls.RandomNoise.menuItem,
    controls.Scramble.menuItem
  )

  private val menuBar = {
    val menuBar = new MenuBar
    menuBar.useSystemMenuBar = true
    menuBar.menus = Seq(menu_file, menu_edit, menu_organize, menu_effects)
    menuBar
  }

  private val toolBar = new ToolBar {
    items = Seq(
      controls.New.button,
      controls.Open.button,
      controls.Save.button,
      new Separator,
      controls.Cut.button,
      controls.Copy.button,
      controls.Paste.button,
      new Separator,
      controls.Undo.button,
      controls.Redo.button
    )
  }

  stage = new PrimaryStage {
    title = "TriPaint"
    onCloseRequest = e => {
      if (!controls.do_exit()) e.consume()
    }
    scene = new Scene(720, 720) {
      delegate.getStylesheets.add(getClass.getResource("/styles/application.css").toExternalForm)
      root = new BorderPane {
        top = new VBox(menuBar, toolBar)
        center = new AnchorPane {
          //overlay and imageDisplay
          val colorPicker1 = new ColorPicker(new Color(imageDisplay.primaryColor()))
          val colorPicker2 = new ColorPicker(new Color(imageDisplay.secondaryColor()))

          imageDisplay.primaryColor <==> colorPicker1.value
          imageDisplay.secondaryColor <==> colorPicker2.value

          val colorBox = new VBox(
            new Label("Primary color:"),
            colorPicker1,
            new Label("Secondary color:"),
            colorPicker2
          )

          AnchorPane.setAnchors(imageDisplay, 0, 0, 0, 0)
          imageDisplay.clip === this.clip

          AnchorPane.setLeftAnchor(toolbox, 0)
          AnchorPane.setTopAnchor(toolbox, 0)

          AnchorPane.setLeftAnchor(colorBox, 10)
          AnchorPane.setBottomAnchor(colorBox, 10)

          AnchorPane.setRightAnchor(imageTabs, 10)
          AnchorPane.setTopAnchor(imageTabs, 10)
          AnchorPane.setBottomAnchor(imageTabs, 10)
          this.children = Seq(imageDisplay, toolbox, colorBox, imageTabs)
        }
      }
    }

    EditMode.modes
      .filter(_.shortCut != null)
      .foreach(m => scene().getAccelerators.put(m.shortCut, () => m.toolboxButton.fire))
  }

  def addImage(newImage: TriImage): Unit = {
    if (newImage != null) {
      imageDisplay addImage newImage

      val stackPane = ImageTabPane(newImage, this, controls, pane => imageTabs.children.remove(pane.delegate))
      imageTabs.children.add(stackPane.delegate)

      imageDisplay.selectImage(newImage, replace = true)
    }
  }

  def close(): Unit = {
    stage.close()
  }

}

object ImageTabPane {
  def apply(image: TriImage, view: TriPaintView, control: TriPaintController, onClose: ImageTabPane => Unit): ImageTabPane = {
    new ImageTabPane(image, view, control, onClose)
  }
}

class ImageTabPane(image: TriImage, view: TriPaintView, control: TriPaintController, onClose: ImageTabPane => Unit) extends StackPane {
  private val preview = new Canvas(image.preview)

  private val closeButton = new Button {
    text = "X"
    visible = false
    alignmentInParent = Pos.TopRight

    onAction = e => {
      if (image.hasChanged) {
        control.saveBeforeClosing(image).showAndWait() match {
          case Some(t) => t.buttonData match {
            case ButtonData.Yes => if (!image.save) if (!control.saveAs(image)) e.consume()
            case ButtonData.No =>
            case _ => e.consume()
          }
          case None => e.consume()
        }
      }

      if (!e.isConsumed) {
        view.imageDisplay.removeImage(image)
        onClose(ImageTabPane.this)
      }
    }
  }

  private val previewButton = new ToggleButton {
    this.graphic = preview
    this.tooltip <== image.toolTip
    this.selected <==> image.selected

    this.onMouseClicked = e => {
      view.imageDisplay.selectImage(image, !e.isControlDown)
    }
  }

  private val starView: StarView = new StarView(image.hasChangedProperty)

  children add previewButton
  children add closeButton
  children add starView

  onMouseEntered = _ => {
    closeButton.visible = true
  }
  onMouseExited = _ => {
    closeButton.visible = false
  }
}

class StarView(visibleProperty: ReadOnlyBooleanProperty) extends ImageView {
  image = new Image("/icons/star.png")
  alignmentInParent = Pos.TopLeft
  mouseTransparent = true
  visible <== visibleProperty
}