package com.martomate.tripaint.view.gui

import java.io.{File, FileInputStream, FileNotFoundException, IOException}

import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.model.image.save.ImageSaverToArray
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.view.{FileOpenSettings, FileSaveSettings}
import com.martomate.tripaint.view.image.TriImage
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Orientation
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView, PixelFormat, WritableImage}
import scalafx.scene.layout._
import scalafx.util.StringConverter

import scala.util.Try


object DialogUtils {
  private def isTrue(pred: => Boolean): Boolean = Try(pred).getOrElse(false)

  def customIntRestriction(pred: Int => Boolean): String => Boolean = s => isTrue(pred(s.toInt)) || isTrue(pred((s + "1").toInt))
  def customDoubleRestriction(pred: Double => Boolean): String => Boolean = s => isTrue(pred(s.toDouble)) || isTrue(pred((s + "1").toDouble))

  val doubleRestriction: String => Boolean = customDoubleRestriction(_ => true)
  val intRestriction: String => Boolean = customIntRestriction(_ => true)
  val uintRestriction: String => Boolean = customIntRestriction(_ >= 0)

  def doubleTF: TextField = makeTF(doubleRestriction)
  def intTF: TextField = makeTF(intRestriction)
  def uintTF: TextField = makeTF(uintRestriction)

  def makeTF(restriction: String => Boolean): TextField = {
    val tf = new TextField
    restrictTextField(tf, restriction)
    tf
  }

  def restrictTextField(tf: TextField, contentAllowed: String => Boolean): Unit = {
    tf.text.onChange((ob, oldVal, newVal) => {
      ob.asInstanceOf[StringProperty].value = if (contentAllowed(newVal)) newVal else oldVal
    })
  }

  def makeGridPane(content: Seq[Seq[Node]]): GridPane = {
    val gridPane = new GridPane
    gridPane.vgap = 10
    gridPane.hgap = 10
    for (i <- content.indices)
      for (j <- content(i).indices)
        gridPane.add(content(i)(j), j, i)
    gridPane
  }

  def getValueFromCustomDialog[R](title: String,
                                  headerText: String = null,
                                  contentText: String = null,
                                  graphic: Node = null,
                                  content: Seq[Region] = Seq.empty,
                                  resultConverter: ButtonType => R,
                                  nodeWithFocus: Node = null,
                                  buttons: Seq[ButtonType] = Seq(ButtonType.OK, ButtonType.Cancel)): Option[R] = {
    val dialog = new Dialog[R]
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = graphic
    val contentBox = new VBox(content: _*)
    contentBox.setSpacing(10)
    dialog.dialogPane().setContent(contentBox)
    dialog.resultConverter = resultConverter
    for (b <- buttons) dialog.dialogPane().getButtonTypes add b
    if (nodeWithFocus != null) dialog.setOnShowing(_ => Platform.runLater(nodeWithFocus.requestFocus()))
    val result = dialog.delegate.showAndWait()
    if (result.isPresent) Some(result.get) else None
  }

  def askForXY(title: String, headerText: String): Option[(Int, Int)] = {
    val xCoordTF = DialogUtils.uintTF
    xCoordTF.promptText = "0"
    val yCoordTF = DialogUtils.uintTF
    yCoordTF.promptText = "0"

    val coordsFromTF = () => {
      val xt = xCoordTF.text()
      val yt = yCoordTF.text()

      for {
        xOffset <- Try(if (xt != "") xt.toInt else 0)
        yOffset <- Try(if (yt != "") yt.toInt else 0)
      } yield (xOffset, yOffset)
    }

    getValueFromCustomDialog[(Int, Int)](
      title = title,
      headerText = headerText,

      content = Seq(makeGridPane(Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF)
      ))),

      resultConverter = {
        case ButtonType.OK => coordsFromTF().getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }

  def askForFileOpenSettings(imagePreview: (File, Int, Int),
                             formats: Seq[(StorageFormat, String)],
                             initiallySelectedFormat: Int): Option[FileOpenSettings] = {
    val (previewFile, previewWidth, previewHeight) = imagePreview

    val xCoordTF = DialogUtils.uintTF
    xCoordTF.promptText = "0"

    val yCoordTF = DialogUtils.uintTF
    yCoordTF.promptText = "0"

    val formatMap: Map[StorageFormat, String] = Map.from(formats)

    val formatChooser = new ChoiceBox(ObservableBuffer(formats.map(_._1): _*))
    formatChooser.selectionModel.value.select(initiallySelectedFormat)
    formatChooser.converter = StringConverter.toStringConverter(formatMap(_))

    val resultFromInputs = () => {
      val xt = xCoordTF.text()
      val yt = yCoordTF.text()
      val format = formatChooser.selectionModel.value.getSelectedItem

      for {
        xOffset <- Try(if (xt != "") xt.toInt else 0)
        yOffset <- Try(if (yt != "") yt.toInt else 0)
      } yield FileOpenSettings((xOffset, yOffset), format)
    }

    val previewPane = new Pane
    previewPane.setMinSize(previewWidth, previewHeight)
    previewPane.setMaxSize(previewWidth, previewHeight)

    {
      import javafx.scene.layout._
      import javafx.scene.paint.Color
      val stroke = new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
      previewPane.delegate.setBorder(new Border(stroke))
    }

    val wholeImage = new ImageView(new Image(new FileInputStream(previewFile)))

    val previewStack = new Pane
    previewStack.delegate.getChildren.addAll(wholeImage, previewPane)

    def updatePreviewAction: Unit = {
      resultFromInputs() foreach {
        case FileOpenSettings((x, y), format) =>
          previewPane.setLayoutX(x)
          previewPane.setLayoutY(y)

          // TODO: preview TriImage using the format
      }
    }

    updatePreviewAction

    xCoordTF.text.onChange(updatePreviewAction)
    yCoordTF.text.onChange(updatePreviewAction)

    getValueFromCustomDialog[FileOpenSettings](
      title = "Open image",
      headerText = "Which part of the image should be opened? Please enter the top left corner:",

      content = Seq(makeGridPane(Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF),
        Seq(new Label("Format:"), formatChooser)
      )), Separator(Orientation.Horizontal), previewStack),

      resultConverter = {
        case ButtonType.OK => resultFromInputs().getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
      )
  }

  def askForFileSaveSettings(storage: ImageStorage,
                             file: File,
                             formats: Seq[(StorageFormat, String)],
                             initiallySelectedFormat: Int): Option[FileSaveSettings] = {
    val imageSize = storage.imageSize
    val (previewFile, previewWidth, previewHeight) = (file, imageSize, imageSize)

    val xCoordTF = DialogUtils.uintTF
    xCoordTF.promptText = "0"

    val yCoordTF = DialogUtils.uintTF
    yCoordTF.promptText = "0"

    val formatMap: Map[StorageFormat, String] = Map.from(formats)

    val formatChooser = new ChoiceBox(ObservableBuffer(formats.map(_._1): _*))
    formatChooser.selectionModel.value.select(initiallySelectedFormat)
    formatChooser.converter = StringConverter.toStringConverter(formatMap(_))

    val resultFromInputs = () => {
      val xt = xCoordTF.text()
      val yt = yCoordTF.text()
      val format = formatChooser.selectionModel.value.getSelectedItem

      for {
        xOffset <- Try(if (xt != "") xt.toInt else 0)
        yOffset <- Try(if (yt != "") yt.toInt else 0)
      } yield FileSaveSettings((xOffset, yOffset), format)
    }

    val previewPane = new StackPane
    previewPane.setMinSize(previewWidth, previewHeight)
    previewPane.setMaxSize(previewWidth, previewHeight)

    val previewImage = new WritableImage(imageSize, imageSize)
    val pixelFormat = PixelFormat.getIntArgbInstance
    previewPane.children.add(new ImageView(previewImage))

    val previewBorder = new Pane
    previewBorder.setMinSize(previewWidth, previewHeight)
    previewBorder.setMaxSize(previewWidth, previewHeight)
    previewPane.children.add(previewBorder)

    {
      import javafx.scene.layout._
      import javafx.scene.paint.Color
      val stroke = new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
      previewBorder.delegate.setBorder(new Border(stroke))
    }

    val previewStack = new Pane
    try {
      val wholeImage = new ImageView(new Image(new FileInputStream(previewFile)))
      previewStack.children.add(wholeImage)
    } catch {
      case _: FileNotFoundException =>
      case _: IOException =>
    }

    previewStack.delegate.getChildren.add(previewPane)

    def updatePreviewAction: Unit = {
      resultFromInputs() foreach {
        case FileSaveSettings((x, y), format) =>
          previewPane.setLayoutX(x)
          previewPane.setLayoutY(y)

          val array = new Array[Int](imageSize * imageSize)
          new ImageSaverToArray(array).save(storage, format, SaveLocation(file, (x, y)))
          previewImage.pixelWriter.setPixels(0, 0, imageSize, imageSize, pixelFormat, array, 0, imageSize)
      }
    }

    updatePreviewAction

    xCoordTF.text.onChange(updatePreviewAction)
    yCoordTF.text.onChange(updatePreviewAction)
    formatChooser.selectionModel().selectedItemProperty().addListener(_ => updatePreviewAction)

    getValueFromCustomDialog[FileSaveSettings](
      title = "Save file",
      headerText = "Where in the file should the image be saved, and how?",

      content = Seq(makeGridPane(Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF),
        Seq(new Label("Format:"), formatChooser)
        )), Separator(Orientation.Horizontal), previewStack),

      resultConverter = {
        case ButtonType.OK => resultFromInputs().getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
      )
  }

  def getValueFromDialog[T](imagePool: ImagePool,
                            images: Seq[ImageContent],
                            title: String,
                            headerText: String,
                            contentText: String,
                            restriction: String => Boolean,
                            stringToValue: String => T): Option[T] = {
    val dialog = new TextInputDialog
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = makeImagePreviewList(images, imagePool)
    DialogUtils.restrictTextField(dialog.editor, restriction)
    dialog.showAndWait match {
      case Some(str) =>
        val num = stringToValue(str)
        Some(num)
      case None =>
        None
    }
  }

  def makeImagePreviewList(images: Seq[ImageContent], imagePool: ImagePool): ScrollPane =
    new ImagePreviewList(images, TriImage.previewSize, imagePool)
}
