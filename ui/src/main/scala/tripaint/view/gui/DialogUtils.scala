package tripaint.view.gui

import tripaint.ScalaFxExt.toScala
import tripaint.grid.{GridCell, ImageGrid}
import tripaint.image.ImagePool
import tripaint.view.image.TriImageForPreview

import javafx.application.Platform
import javafx.beans.property.StringProperty
import javafx.scene.Node
import javafx.scene.control.{ButtonType, ScrollPane, TextInputDialog}
import javafx.scene.control.{Dialog, TextField}
import javafx.scene.layout.{GridPane, Region, VBox}

import scala.util.Try

type TextFieldRestriction = String => Boolean

object TextFieldRestriction {
  private def isTrue(pred: => Boolean): Boolean = Try(pred).getOrElse(false)

  def customIntRestriction(pred: Int => Boolean): TextFieldRestriction = { s =>
    isTrue(pred(s.toInt)) || isTrue(pred((s + "1").toInt))
  }

  val intRestriction: TextFieldRestriction = customIntRestriction(_ => true)
  val uintRestriction: TextFieldRestriction = customIntRestriction(_ >= 0)
}

object RestrictedTextField {
  def intTF: TextField = restrict(new TextField, TextFieldRestriction.intRestriction)
  def uintTF: TextField = restrict(new TextField, TextFieldRestriction.uintRestriction)

  def restrict(tf: TextField, contentAllowed: TextFieldRestriction): TextField = {
    tf.textProperty.addListener((ob, oldVal, newVal) => {
      val property = ob.asInstanceOf[StringProperty]
      property.setValue(if contentAllowed(newVal) then newVal else oldVal)
    })
    tf
  }
}

object DialogUtils {
  def makeGridPane(content: Seq[Seq[Node]]): GridPane = {
    val gridPane = new GridPane
    gridPane.setVgap(10)
    gridPane.setHgap(10)
    for i <- content.indices do {
      for j <- content(i).indices do {
        gridPane.add(content(i)(j), j, i)
      }
    }
    gridPane
  }

  def getValueFromCustomDialog[R](
      title: String,
      headerText: String = null,
      contentText: String = null,
      graphic: Node = null,
      content: Seq[Region] = Seq.empty,
      resultConverter: ButtonType => R,
      nodeWithFocus: Node = null,
      buttons: Seq[ButtonType] = Seq(ButtonType.OK, ButtonType.CANCEL)
  ): Option[R] = {
    val dialog = new Dialog[R]
    dialog.setTitle(title)
    dialog.setHeaderText(headerText)
    dialog.setContentText(contentText)
    dialog.setGraphic(graphic)

    val contentBox = new VBox(content*)
    contentBox.setSpacing(10)

    dialog.getDialogPane.setContent(contentBox)
    dialog.setResultConverter(b => resultConverter(b))

    for b <- buttons do {
      dialog.getDialogPane.getButtonTypes.add(b)
    }

    if nodeWithFocus != null then {
      dialog.setOnShowing(_ => Platform.runLater(() => nodeWithFocus.requestFocus()))
    }

    dialog.showAndWait().toScala
  }

  def getValueFromDialog[T](
      imagePool: ImagePool,
      images: Seq[GridCell],
      title: String,
      headerText: String,
      contentText: String,
      restriction: String => Boolean,
      stringToValue: String => T,
      refreshPreviewFn: Option[(T, ImageGrid) => Unit] = None
  ): Option[T] = {
    val (previewPane, updatePreview) = makeImagePreviewList(images, imagePool)

    val dialog = new TextInputDialog
    dialog.setTitle(title)
    dialog.setHeaderText(headerText)
    dialog.setContentText(contentText)
    dialog.setGraphic(previewPane)
    RestrictedTextField.restrict(dialog.getEditor, restriction)

    refreshPreviewFn match {
      case Some(refreshFn) =>
        dialog.getEditor.textProperty.addListener((_, _, s) =>
          if restriction(s) then {
            val value = stringToValue(s)
            updatePreview(imageGrid => refreshFn(value, imageGrid))
          } else {
            updatePreview(_ => ())
          }
        )
      case None =>
    }

    dialog.showAndWait().toScala.map(stringToValue)
  }

  def makeImagePreviewList(
      images: Seq[GridCell],
      imagePool: ImagePool
  ): (ScrollPane, (ImageGrid => Unit) => Unit) = {
    ImagePreviewList.fromImageContent(images, TriImageForPreview.previewSize, imagePool.locationOf)
  }
}
