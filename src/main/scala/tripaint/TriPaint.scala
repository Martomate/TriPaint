package tripaint

import java.io.File
import javafx.event.{ActionEvent, Event, EventHandler}

import scala.util.Try
import tripaint.image.SaveLocation
import tripaint.image.TriImage

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Orientation, Pos}
import scalafx.scene.{Scene, SnapshotParameters}
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter


object TriPaint extends JFXApp {
  private val imageTabs = new TilePane
  imageTabs.maxWidth = TriImage.previewSize
  
  private val imageDisplay = new ImagePane
  
  private val toolbox = new TilePane
  toolbox.orientation = Orientation.Vertical
  toolbox.children = EditMode.modes.map(_.toolboxButton)
  
  private def makeMenu(text: String, menuItems: MenuItem*): Menu = {
    val menu = new Menu(text)
    menu.items = menuItems
    menu
  }
  
  private def makeMenuItem(text: String, imagePath: String = null, onAction: EventHandler[ActionEvent], accelerator: KeyCombination = null): MenuItem = {
    val item = if (imagePath == null) new MenuItem(text) else new MenuItem(text, new ImageView("icons/" + imagePath + ".png"))
    item.onAction = onAction
    if (accelerator != null) item.accelerator = accelerator
    item
  }
  
  private def makeButton(text: String, imagePath: String = null, onAction: EventHandler[ActionEvent]): Button = {
    val item = if (imagePath == null) new Button(text) else new Button(null, new ImageView("icons/" + imagePath + ".png"))
    item.onAction = onAction
    item.tooltip = new Tooltip(text)
    item
  }
  
  private val menu_file = 
    makeMenu("File",
      makeMenuItem("New",
          imagePath = "new",
          onAction = action_new,
          accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)
      ),
      makeMenuItem("Open",
        imagePath = "open",
        onAction = action_open,
        accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)
      ),
      makeMenuItem("Open partial",
        onAction = action_specialOpen,
        accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown, KeyCombination.ShiftDown)
      ),
      new SeparatorMenuItem,
      makeMenuItem("Save",
        imagePath = "save",
        onAction = action_save,
        accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)
      ),
      makeMenuItem("Save As",
  		  onAction = action_saveAs,
        accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown, KeyCombination.ShiftDown)
      ),
      new SeparatorMenuItem,
      makeMenuItem("Exit",
        onAction = e => if (action_exit(e)) stage.close
      )
    )
  private val menu_edit = 
    makeMenu("Edit",
      makeMenuItem("Undo",
        imagePath = "undo",
        onAction = action_undo,
        accelerator = new KeyCodeCombination(KeyCode.Z, KeyCombination.ControlDown)
      ),
      makeMenuItem("Redo",
        imagePath = "redo",
        onAction = action_redo,
        accelerator = new KeyCodeCombination(KeyCode.Y, KeyCombination.ControlDown)
      ),
      new SeparatorMenuItem,
      makeMenuItem("Cut",
        imagePath = "cut",
        onAction = action_cut,
        accelerator = new KeyCodeCombination(KeyCode.X, KeyCombination.ControlDown)
      ),
      makeMenuItem("Copy",
        imagePath = "copy",
        onAction = action_copy,
        accelerator = new KeyCodeCombination(KeyCode.C, KeyCombination.ControlDown)
      ),
      makeMenuItem("Paste",
        imagePath = "paste",
        onAction = action_paste,
        accelerator = new KeyCodeCombination(KeyCode.V, KeyCombination.ControlDown)
      )
    )
  private val menu_organize = 
    makeMenu("Organize",
      makeMenuItem("Move",
        imagePath = "move",
        onAction = action_move
      ),
      makeMenuItem("Scale",
        imagePath = "scale",
        onAction = action_scale
      ),
      makeMenuItem("Rotate",
        imagePath = "rotate",
        onAction = action_rotate
      ),
      makeMenuItem("Fit",
        onAction = action_fit
      )
    )
  private val menu_effects = 
    makeMenu("Effects",
      makeMenuItem("Blur",
        onAction = action_blur
      ),
      makeMenuItem("Motion blur",
      	onAction = action_motionBlur
      ),
      makeMenuItem("Perlin Noise",
        onAction = action_perlinNoise
      ),
      makeMenuItem("Random noise",
        onAction = action_randomNoise
      ),
      makeMenuItem("Scramble",
        onAction = action_scramble
      )
    )
  
  stage = new PrimaryStage {
    title = "TriPaint"
    onCloseRequest = e => {
      if (!action_exit(e)) e.consume()
    }
    scene = new Scene(720, 720) {
      delegate.getStylesheets.add(getClass.getResource("/styles/application.css").toExternalForm)
      root = new BorderPane {
        top = new VBox({
          val menuBar = new MenuBar
            menuBar.useSystemMenuBar = true
            menuBar.menus = Seq(menu_file, menu_edit, menu_organize, menu_effects)
            menuBar
          },
          new ToolBar {
            items = Seq(
              makeButton("New",
                imagePath = "new",
                onAction = action_new
              ),
      		    makeButton("Open",
      		      imagePath = "open",
                onAction = action_open
              ),
      		    makeButton("Save",
      		      imagePath = "save",
                onAction = action_save
              ),
      		    new Separator,
      		    makeButton("Cut",
      		      imagePath = "cut",
                onAction = action_cut
              ),
      		    makeButton("Copy",
      		      imagePath = "copy",
                onAction = action_copy
              ),
      		    makeButton("Paste",
      		      imagePath = "paste",
                onAction = action_paste
              ),
      		    new Separator,
      		    makeButton("Undo",
      		      imagePath = "undo",
                onAction = action_undo
              ),
      		    makeButton("Redo",
      		      imagePath = "redo",
                onAction = action_redo
              )
            )
          }
        )
        center = new AnchorPane {//overlay and imageDisplay
          val colorPicker1 = new ColorPicker(new Color(imageDisplay.primaryColor()))
          val colorPicker2 = new ColorPicker(new Color(imageDisplay.secondaryColor()))
          
          imageDisplay.primaryColor <==> colorPicker1.value
          imageDisplay.secondaryColor <==> colorPicker2.value
          
          val colorBox = new VBox(
            new Label("Color 1:"),
        		colorPicker1,
        		new Label("Color 2:"),
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
      
  	  val preview = new Canvas(newImage.previewCanvas)
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
      imageTabs.children add stackPane.delegate
      
      imageDisplay.selectImage(newImage, replace = true)
    }
  }
  
  private def action_new(e: ActionEvent): Unit = {
    val dialog = new TextInputDialog
		dialog.title = "New image"
		dialog.headerText = "Please enter imagesize."
		dialog.contentText = "Image size:"
		DialogUtils.restrictTextField(dialog.editor, DialogUtils.uintRestriction)
		
		dialog.showAndWait.foreach(result => {
		  try {
			  val size = result.toInt
			  if (size > 0) addImage(TriImage(size, imageDisplay))
		  } catch {
		    case _: Exception =>
		  }
		})
  }
  
  /***/
  private def action_open(e: ActionEvent): Unit = {
    val chooser = new FileChooser
		chooser.title = "Open file"
		val file = chooser.showOpenDialog(null)
		if (file != null) addImage(TriImage.loadFromFile(file, imageDisplay))
  }
  
  private def action_specialOpen(e: ActionEvent): Unit = {
    val chooser = new FileChooser
		chooser.title = "Open file"
		val file = chooser.showOpenDialog(null)
		if (file != null) openFile(file)
  }
  
  private def openFile(file: File): Unit = {
    val images = imageDisplay.getSelectedImages
		val xCoordTF = DialogUtils.uintTF
		val yCoordTF = DialogUtils.uintTF
		val imageSizeTF = DialogUtils.uintTF
		import DialogUtils._
    showInputDialog[(Int, Int, Int)](
      title = "Open partial image",
      headerText = "Which part of the image should be opened, and how much?",
      
      content = Seq(makeGridPane(Seq(
    	  Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF),
        Seq(new Label("Image size:"), imageSizeTF)
      ))),
      
      resultConverter = {
        case ButtonType.OK => Try((xCoordTF.text().toInt, yCoordTF.text().toInt, imageSizeTF.text().toInt)).getOrElse(null)
        case _ => null
      },
      
      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    ) match {
      case Some((x, y, size)) => addImage(TriImage.loadFromFile(file, imageDisplay, Some(x, y), size))
      case _ =>
    }
  }
  
  private def action_save(e: ActionEvent): Unit = save(imageDisplay.getSelectedImages.filter(_.hasChanged): _*)
  
  private def action_saveAs(e: ActionEvent): Unit = imageDisplay.getSelectedImages.foreach(saveAs)

  private def action_exit(e: Event): Boolean = {
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
  
  private def action_undo(e: ActionEvent): Unit = {
    imageDisplay.undo
  }
  
  private def action_redo(e: ActionEvent): Unit = {
    imageDisplay.redo
  }
  
  private def action_cut(e: ActionEvent): Unit = {
    ???
  }
  
  private def action_copy(e: ActionEvent): Unit = {
    ???
  }
  
  private def action_paste(e: ActionEvent): Unit = {
    ???
  }
  
  private def makeImagePreviewList(images: Seq[TriImage]): ScrollPane = {
    val params = new SnapshotParameters
    params.fill = Color.Transparent
    
    val imageViews = images.map(im => {
      val view = new ImageView
      view.image = im.previewCanvas.snapshot(params, null)
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
  
  private def action_move(e: ActionEvent): Unit = {
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
  private def action_scale(e: ActionEvent): Unit = {
    makeTextInputDialog[Double](
      "Scale images",
      "How much should the images be scaled?",
      "Scale factor:",
      DialogUtils.doubleRestriction,
      str => Try(str.toDouble).getOrElse(0d),
      (im, sc) => im scale sc
    )
  }
  private def action_rotate(e: ActionEvent): Unit = {
    makeTextInputDialog[Double](
      "Rotate images",
      "How much should the images be rotated (degrees)?",
      "Angle:",
      DialogUtils.doubleRestriction,
      str => Try(str.toDouble).getOrElse(0d),
      (im, rt) => im rotate rt
    )
  }
  private def action_fit(e: ActionEvent): Unit = ???

  private def action_blur(e: ActionEvent): Unit = {
    makeTextInputDialog[Int](
      "Blur images",
      "How much should the images be blurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0),
      (im, amt) => im blur amt
    )
  }

  private def action_motionBlur(e: ActionEvent): Unit = {
    makeTextInputDialog[Int](
      "Motionblur images",
      "How much should the images be motionblurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0),
      (im, amt) => im motionBlur amt
    )
  }

  private def action_perlinNoise(e: ActionEvent): Unit = {
    imageDisplay.getSelectedImages.foreach(_.perlinNoise)
  }

  private def action_randomNoise(e: ActionEvent): Unit = {
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
      case Some((lo, hi)) => images.foreach(_.randomNoise(lo, hi))
      case _ =>
    }
  }
  
  private def action_scramble(e: ActionEvent): Unit = {
    imageDisplay.getSelectedImages.foreach(_.scramble)
  }
  
}