package com.martomate.tripaint

import java.io.File

import com.martomate.tripaint.image.effects._
import com.martomate.tripaint.image.storage.SaveLocation
import com.martomate.tripaint.image.{TriImage, TriImageCoords}
import javafx.event.{ActionEvent, Event, EventHandler}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Orientation, Pos}
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.{Scene, SnapshotParameters}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import scala.util.Try


object TriPaint extends JFXApp {
  private val imageTabs = new TilePane
  imageTabs.maxWidth = TriImage.previewSize

  private val imageDisplay: ImagePane = new ImagePane(new ImageCollageImplOld(32))

  private val toolbox = new TilePane
  toolbox.orientation = Orientation.Vertical
  toolbox.children = EditMode.modes.map(_.toolboxButton)

  private def makeMenu(text: String, menuItems: MenuItem*): Menu = {
    val menu = new Menu(text)
    menu.items = menuItems
    menu
  }

  def makeMenuItem(text: String, imagePath: String = null, onAction: EventHandler[ActionEvent], accelerator: KeyCombination = null): MenuItem = {
    val item = if (imagePath == null) new MenuItem(text) else new MenuItem(text, new ImageView("icons/" + imagePath + ".png"))
    item.onAction = onAction
    if (accelerator != null) item.accelerator = accelerator
    item
  }

  def makeButton(text: String, imagePath: String = null, onAction: EventHandler[ActionEvent]): Button = {
    val item = if (imagePath == null) new Button(text) else new Button(null, new ImageView("icons/" + imagePath + ".png"))
    item.onAction = onAction
    item.tooltip = new Tooltip(text)
    item
  }

  private val menu_file = makeMenu("File",
    controls.New.menuItem,
    controls.NewComp.menuItem,
    controls.Open.menuItem,
    controls.OpenPartial.menuItem,
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
      if (!do_exit()) e.consume()
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

  private def addImage(newImage: TriImage): Unit = {
    if (newImage != null) {
      imageDisplay addImage newImage

      val preview = new Canvas(newImage.preview)
      val stackPane = new StackPane
      val closeButton = new Button {
        text = "X"
        visible = false
        alignmentInParent = Pos.TopRight

        onAction = e => {
          if (newImage.hasChanged) {
            new Alert(AlertType.Confirmation) {
              title = "Save before closing?"
              headerText = "Do you want to save this image before closing the tab?"

              graphic = new ImageView(preview.snapshot(new SnapshotParameters {
                fill = Color.Transparent
              }, null))

              buttonTypes = Seq(
                new ButtonType("Save", ButtonData.Yes),
                new ButtonType("Don't save", ButtonData.No),
                new ButtonType("Cancel", ButtonData.CancelClose)
              )
            }.showAndWait() match {
              case Some(t) => t.buttonData match {
                case ButtonData.Yes => if (!newImage.save) if (!saveAs(newImage)) e.consume()
                case ButtonData.No =>
                case _ => e.consume()
              }
              case None => e.consume()
            }
          }

          if (!e.isConsumed) {
            imageDisplay.removeImage(newImage)
            imageTabs.children.remove(stackPane.delegate)
          }
        }
      }

      val previewButton = new ToggleButton {
        this.graphic = preview
        this.tooltip <== newImage.toolTip
        this.selected <==> newImage.selected

        this.onMouseClicked = e => {
          imageDisplay.selectImage(newImage, !e.isControlDown)
        }
      }
      stackPane.children = Seq(
        previewButton,
        closeButton,
        {
          val view = new ImageView
          view.image = new Image("/icons/star.png")
          view.alignmentInParent = Pos.TopLeft
          view.mouseTransparent = true
          view.visible <== newImage.hasChangedProperty
          view
        }
      )
      stackPane.onMouseEntered = _ => {
        closeButton.visible = true
      }
      stackPane.onMouseExited = _ => {
        closeButton.visible = false
      }
      imageTabs.children.add(stackPane.delegate)

      imageDisplay.selectImage(newImage, replace = true)
    }
  }

  private def askForWhereToPutImage(): Option[(Int, Int)] = {
    val xCoordTF = DialogUtils.uintTF
    val yCoordTF = DialogUtils.uintTF

    import DialogUtils._
    showInputDialog[(Int, Int)](
      title = "New image",
      headerText = "Please enter where it should be placed.",

      content = Seq(makeGridPane(Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF)
      ))),

      resultConverter = {
        case ButtonType.OK => Try((xCoordTF.text().toInt, yCoordTF.text().toInt)).getOrElse(null)
        case _ => null
      },

      nodeWithFocus = xCoordTF,

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }

  private def openFile(file: File): Unit = {
    val images = imageDisplay.getSelectedImages
    val xCoordTF = DialogUtils.uintTF
    val yCoordTF = DialogUtils.uintTF
    val imageSizeTF = DialogUtils.uintTF
    import DialogUtils._
    showInputDialog[(Int, Int)](
      title = "Open partial image",
      headerText = "Which part of the image should be opened, and how much?",

      content = Seq(makeGridPane(Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF)
      ))),

      resultConverter = {
        case ButtonType.OK => Try((xCoordTF.text().toInt, yCoordTF.text().toInt)).getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    ) match {
      case Some((ix, iy)) =>
        askForWhereToPutImage() match {
          case Some((x, y)) =>
            addImage(TriImage.loadFromFile(TriImageCoords(x, y), file, imageDisplay, Some(ix, iy), imageDisplay.imageSize))
          case _ =>
        }
      case _ =>
    }
  }

  private def do_exit(): Boolean = {
    imageDisplay.getImages.filter(_.hasChanged) match {
      case Vector() => true
      case images =>
        saveBeforeClosing(images: _*).showAndWait match {
          case Some(t) => t.buttonData match {
            case ButtonData.Yes => save(images: _*)
            case ButtonData.No => true
            case _ => false
          }
          case None => false
        }
    }
  }

  private def makeImagePreviewList(images: Seq[TriImage]): ScrollPane = {
    val params = new SnapshotParameters
    params.fill = Color.Transparent

    val imageViews = images.map(im => {
      val view = new ImageView
      view.image = im.preview.snapshot(params, null)
      Tooltip.install(view.delegate, im.toolTip())
      view
    })
    val sp = new ScrollPane
    sp.maxWidth = TriImage.previewSize * 5
    sp.content = new HBox(children = imageViews: _*)
    sp.minViewportHeight = TriImage.previewSize * Math.sqrt(3) / 2
    sp
  }

  private def saveBeforeClosing(images: TriImage*): Alert = {
    val alert = new Alert(AlertType.Confirmation)
    alert.title = "Save before closing?"
    alert.headerText = "Do you want to save " + (if (images.size == 1) "this image" else "these images") + " before closing the tab?"
    alert.graphic = makeImagePreviewList(images)

    alert.buttonTypes = Seq(
      new ButtonType("Save", ButtonData.Yes),
      new ButtonType("Don't save", ButtonData.No),
      new ButtonType("Cancel", ButtonData.CancelClose)
    )
    alert
  }

  private def save(images: TriImage*): Boolean = images.filter(!_.save).forall(saveAs)

  private def saveAs(image: TriImage): Boolean = {
    val chooser = new FileChooser
    chooser.title = "Save file"
    chooser.extensionFilters.add(new ExtensionFilter("PNG", "*.png"))
    val file = chooser.showSaveDialog(null)
    if (file != null) {
      image.setSaveLocation(SaveLocation(file, None))
      if (!image.save) {
        println("Image could not be saved!!")
        false
      } else true
    } else false
  }

  private def makeTextInputDialog[T](title: String, headerText: String, contentText: String, restriction: String => Boolean, stringToValue: String => T, action: (TriImage, T) => Unit): TextInputDialog = {
    val images = imageDisplay.getSelectedImages
    val dialog = new TextInputDialog
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = makeImagePreviewList(images)
    DialogUtils.restrictTextField(dialog.editor, restriction)
    dialog.showAndWait match {
      case Some(str) =>
        val num = stringToValue(str)
        images.foreach(action(_, num))
      case None =>
    }
    dialog
  }

  private object controls {
    val New: MenuBarAction = MenuBarAction.apply("New", "new", new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)) {
      askForWhereToPutImage() match {
        case Some((x, y)) =>
          addImage(TriImage(TriImageCoords(x, y), imageDisplay.imageSize, imageDisplay))
        case _ =>
      }
    }

    val NewComp: MenuBarAction = MenuBarAction.apply("New composition", accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
      val dialog = new TextInputDialog
      dialog.title = "New image composition"
      dialog.headerText = "Please enter number of images."
      dialog.contentText = "Number of images:"
      DialogUtils.restrictTextField(dialog.editor, DialogUtils.uintRestriction)

      dialog.showAndWait.foreach { result =>
        try {
          val num = result.toInt

        } catch {
          case _: Exception =>
        }
      }
    }

    val Open: MenuBarAction = MenuBarAction.apply("Open", "open", new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)) {
      val chooser = new FileChooser
      chooser.title = "Open file"
      val file = chooser.showOpenDialog(null)
      if (file != null) askForWhereToPutImage() match {
        case Some((x, y)) =>
          addImage(TriImage.loadFromFile(TriImageCoords(x, y), file, imageDisplay))
        case _ =>
      }
    }

    val OpenPartial: MenuBarAction = MenuBarAction.apply("Open partial", accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
      val chooser = new FileChooser
      chooser.title = "Open file"
      val file = chooser.showOpenDialog(null)
      if (file != null) openFile(file)
    }

    val Save: MenuBarAction = MenuBarAction.apply("Save", "save", new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)) {
      save(imageDisplay.getSelectedImages.filter(_.hasChanged): _*)
    }

    val SaveAs: MenuBarAction = MenuBarAction.apply("Save As", accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
      imageDisplay.getSelectedImages.foreach(saveAs)
    }

    val Exit: MenuBarAction = MenuBarAction.apply("Exit") {
      if (do_exit()) stage.close
    }

    val Undo: MenuBarAction = MenuBarAction.apply("Undo", "undo", new KeyCodeCombination(KeyCode.Z, KeyCombination.ControlDown)) {
      imageDisplay.undo
    }

    val Redo: MenuBarAction = MenuBarAction.apply("Redo", "redo", new KeyCodeCombination(KeyCode.Y, KeyCombination.ControlDown)) {
      imageDisplay.redo
    }

    val Cut: MenuBarAction = MenuBarAction.apply("Cut", "cut", new KeyCodeCombination(KeyCode.X, KeyCombination.ControlDown)) {
      ???
    }

    val Copy: MenuBarAction = MenuBarAction.apply("Copy", "copy", new KeyCodeCombination(KeyCode.C, KeyCombination.ControlDown)) {
      ???
    }

    val Paste: MenuBarAction = MenuBarAction.apply("Paste", "paste", new KeyCodeCombination(KeyCode.V, KeyCombination.ControlDown)) {
      ???
    }

    val Move: MenuBarAction = MenuBarAction.apply("Move", "move") {
      val images = imageDisplay.getSelectedImages
      val horizTextField = DialogUtils.doubleTF
      val vertTextField = DialogUtils.doubleTF
      import DialogUtils._
      showInputDialog[(Double, Double)](
        title = "Move images",
        headerText = "How far should the images move?",
        graphic = makeImagePreviewList(images),

        content = Seq(makeGridPane(Seq(
          Seq(new Label("Horizontal movement:"), horizTextField),
          Seq(new Label("Vertical movement:"), vertTextField)
        ))),

        resultConverter = {
          case ButtonType.OK => Try((horizTextField.text().toDouble, vertTextField.text().toDouble)).getOrElse(null)
          case _ => null
        },

        buttons = Seq(ButtonType.OK, ButtonType.Cancel)
      ) match {
        case Some((h, v)) => images.foreach(_.move(h, v))
        case _ =>
      }
    }

    val Scale: MenuBarAction = MenuBarAction.apply("Scale", "scale") {
      makeTextInputDialog[Double](
        "Scale images",
        "How much should the images be scaled?",
        "Scale factor:",
        DialogUtils.doubleRestriction,
        str => Try(str.toDouble).getOrElse(0d),
        (im, sc) => im scale sc
      )
    }

    val Rotate: MenuBarAction = MenuBarAction.apply("Rotate", "rotate") {
      makeTextInputDialog[Double](
        "Rotate images",
        "How much should the images be rotated (degrees)?",
        "Angle:",
        DialogUtils.doubleRestriction,
        str => Try(str.toDouble).getOrElse(0d),
        (im, rt) => im rotate rt
      )
    }

    val Fit: MenuBarAction = MenuBarAction.apply("Fit") {
      ???
    }

    val Blur: MenuBarAction = MenuBarAction.apply("Blur") {
      makeTextInputDialog[Int](
        "Blur images",
        "How much should the images be blurred?",
        "Radius:",
        DialogUtils.uintRestriction,
        str => Try(str.toInt).getOrElse(0),
        (im, amt) => im.applyEffect(new BlurEffect(amt))
      )
    }

    val MotionBlur: MenuBarAction = MenuBarAction.apply("Motion blur") {
      makeTextInputDialog[Int](
        "Motionblur images",
        "How much should the images be motionblurred?",
        "Radius:",
        DialogUtils.uintRestriction,
        str => Try(str.toInt).getOrElse(0),
        (im, amt) => im.applyEffect(new MotionBlurEffect(amt))
      )
    }

    val PerlinNoise: MenuBarAction = MenuBarAction.apply("Perlin noise") {
      imageDisplay.getSelectedImages.foreach(_.applyEffect(PerlinNoiseEffect))
    }

    val RandomNoise: MenuBarAction = MenuBarAction.apply("Random noise") {
      val images = imageDisplay.getSelectedImages
      val loColorPicker = new ColorPicker(Color.Black)
      val hiColorPicker = new ColorPicker(Color.White)
      import DialogUtils._
      showInputDialog[(Color, Color)](
        title = "Fill images randomly",
        headerText = "Which color-range should be used?",
        graphic = makeImagePreviewList(images),

        content = Seq(makeGridPane(Seq(
          Seq(new Label("Minimum color:"), loColorPicker),
          Seq(new Label("Maximum color:"), hiColorPicker)
        ))),

        resultConverter = {
          case ButtonType.OK => Try((new Color(loColorPicker.value()), new Color(hiColorPicker.value()))).getOrElse(null)
          case _ => null
        },

        buttons = Seq(ButtonType.OK, ButtonType.Cancel)
      ) match {
        case Some((lo, hi)) => images.foreach(_.applyEffect(new RandomNoiseEffect(lo, hi)))
        case _ =>
      }
    }

    val Scramble: MenuBarAction = MenuBarAction.apply("Scramble") {
      imageDisplay.getSelectedImages.foreach(_.applyEffect(ScrambleEffect))
    }
  }

}