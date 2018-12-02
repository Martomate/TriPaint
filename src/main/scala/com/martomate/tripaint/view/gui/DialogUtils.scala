package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.pool.ImagePool
import com.martomate.tripaint.view.image.TriImage
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, Region, VBox}

import scala.util.Try


object DialogUtils {
  private def isTrue(pred: => Boolean): Boolean = Try(pred).getOrElse(false)

  def customIntRestriction(pred: Int => Boolean): String => Boolean = s => isTrue(pred(s.toInt)) || isTrue(pred((s + "1").toInt))
  def customDoubleRestriction(pred: Double => Boolean): String => Boolean = s => isTrue(pred(s.toDouble)) || isTrue(pred((s + "1").toDouble))

  val doubleRestriction: String => Boolean = customDoubleRestriction(_ => true)
  val intRestriction: String => Boolean = customIntRestriction(_ => true)
  val uintRestriction: String => Boolean = customIntRestriction(_ >= 0)

  def doubleTF: TextField = makeTF(doubleRestriction)
  def intTF: TextField = makeTF(doubleRestriction)
  def uintTF: TextField = makeTF(doubleRestriction)

  def makeTF(restriction: String => Boolean): TextField = {
    val tf = new TextField
    DialogUtils.restrictTextField(tf, restriction)
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
    dialog.dialogPane().setContent(new VBox(content: _*))
    dialog.resultConverter = resultConverter
    for (b <- buttons) dialog.dialogPane().getButtonTypes add b
    if (nodeWithFocus != null) dialog.setOnShowing(_ => Platform.runLater(nodeWithFocus.requestFocus()))
    val result = dialog.delegate.showAndWait()
    if (result.isPresent) Some(result.get) else None
  }

  def askForXY(title: String, headerText: String): Option[(Int, Int)] = {
    val xCoordTF = DialogUtils.uintTF
    val yCoordTF = DialogUtils.uintTF

    getValueFromCustomDialog[(Int, Int)](
      title = title,
      headerText = headerText,

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

  def askForWhereToPutImage(): Option[(Int, Int)] = askForXY(
    title = "New image",
    headerText = "Please enter where it should be placed."
  )

  def askForOffset(): Option[(Int, Int)] = askForXY(
    title = "Open partial image",
    headerText = "Which part of the image should be opened? Please enter the top left corner:"
  )

  def getValueFromDialog[T](imagePool: ImagePool,
                            images: Seq[TriImage],
                            title: String,
                            headerText: String,
                            contentText: String,
                            restriction: String => Boolean,
                            stringToValue: String => T): Option[T] = {
    val dialog = new TextInputDialog
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = makeImagePreviewList(images.map(_.content), imagePool)
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
