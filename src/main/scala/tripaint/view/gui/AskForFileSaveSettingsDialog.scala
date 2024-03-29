package tripaint.view.gui

import tripaint.model.coords.StorageCoords
import tripaint.model.image.ImageStorage
import tripaint.model.image.format.StorageFormat
import tripaint.view.FileSaveSettings
import tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}

import scalafx.collections.ObservableBuffer
import scalafx.geometry.Orientation
import scalafx.scene.control.{ButtonType, ChoiceBox, Label, Separator}
import scalafx.scene.image.{Image, ImageView, PixelFormat, WritableImage}
import scalafx.scene.layout.{Pane, StackPane}
import scalafx.util.StringConverter

import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import scala.util.{Success, Try}

object AskForFileSaveSettingsDialog {
  def askForFileSaveSettings(
      storage: ImageStorage,
      file: File,
      formats: Seq[(StorageFormat, String)],
      initiallySelectedFormat: Int
  ): Option[FileSaveSettings] = {
    val imageSize = storage.imageSize
    val (previewFile, previewWidth, previewHeight) = (file, imageSize, imageSize)

    val xCoordTF = RestrictedTextField.uintTF
    xCoordTF.promptText = "0"

    val yCoordTF = RestrictedTextField.uintTF
    yCoordTF.promptText = "0"

    val formatMap: Map[StorageFormat, String] = Map.from(formats)

    val formatChooser = {
      val b = new ChoiceBox[StorageFormat](ObservableBuffer(formats.map(_._1): _*))
      b.selectionModel.value.select(initiallySelectedFormat)
      b.converter = StringConverter.toStringConverter(formatMap(_))
      b
    }

    val resultFromInputs = () => {
      val xt = xCoordTF.text()
      val yt = yCoordTF.text()
      val format = formatChooser.selectionModel.value.getSelectedItem

      for {
        xOffset <- Try(if xt != "" then xt.toInt else 0)
        yOffset <- Try(if yt != "" then yt.toInt else 0)
      } yield FileSaveSettings(StorageCoords(xOffset, yOffset), format)
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
      import javafx.scene.layout.*
      import javafx.scene.paint.Color
      val stroke = new BorderStroke(
        Color.RED,
        BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY,
        BorderWidths.DEFAULT
      )
      previewBorder.delegate.setBorder(new Border(stroke))
    }

    val previewStack = new Pane
    try {
      val wholeImage = new ImageView(new Image(new FileInputStream(previewFile)))
      previewStack.children.add(wholeImage)
    } catch {
      case _: FileNotFoundException =>
      case _: IOException           =>
    }

    previewStack.delegate.getChildren.add(previewPane)

    def updatePreviewAction(): Unit = {
      resultFromInputs() match {
        case Success(FileSaveSettings(StorageCoords(x, y), format)) =>
          previewPane.setLayoutX(x)
          previewPane.setLayoutY(y)

          val pixels = storage.toRegularImage(format).toIntArray

          val w = previewImage.pixelWriter
          w.setPixels(0, 0, imageSize, imageSize, pixelFormat, pixels, 0, imageSize)
        case _ =>
      }
    }

    updatePreviewAction()

    xCoordTF.text.onChange(updatePreviewAction())
    yCoordTF.text.onChange(updatePreviewAction())
    formatChooser.selectionModel().selectedItemProperty().addListener(_ => updatePreviewAction())

    val inputForm = makeGridPane(
      Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF),
        Seq(new Label("Format:"), formatChooser)
      )
    )

    getValueFromCustomDialog[FileSaveSettings](
      title = "Save file",
      headerText = "Where in the file should the image be saved, and how?",
      content = Seq(
        inputForm,
        Separator(Orientation.Horizontal),
        previewStack
      ),
      resultConverter = {
        case ButtonType.OK => resultFromInputs().getOrElse(null)
        case _             => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }
}
