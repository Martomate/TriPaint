package tripaint.control

import tripaint.infrastructure.FileSystem
import tripaint.model.{Color, ImageGrid, ImageGridChange, TriPaintModel}
import tripaint.model.coords.{GridCoords, StorageCoords}
import tripaint.model.effects.Effect
import tripaint.model.image.{
  GridCell,
  ImageChange,
  ImagePool,
  ImageSaveCollisionHandler,
  ImageStorage
}
import tripaint.model.image.ImagePool.{SaveInfo, SaveLocation}
import tripaint.model.image.format.StorageFormat
import tripaint.util.CachedLoader
import tripaint.view.{FileOpenSettings, FileSaveSettings}

import java.io.File
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object Actions {
  def save(
      imageGrid: ImageGrid,
      imagePool: ImagePool,
      images: Seq[GridCell],
      fileSystem: FileSystem
  )(
      askForSaveFile: GridCell => Option[File],
      askForFileSaveSettings: (File, GridCell) => Option[FileSaveSettings],
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean = {
    images
      .filter(im => !trySaveImage(imageGrid, imagePool, im.storage, fileSystem))
      .forall(im =>
        trySaveImage(imageGrid, imagePool, im.storage, fileSystem) ||
          saveAs(imageGrid, imagePool, im, fileSystem)(
            askForSaveFile,
            askForFileSaveSettings,
            imageSaveCollisionHandler
          )
      )
  }

  private def trySaveImage(
      imageGrid: ImageGrid,
      imagePool: ImagePool,
      image: ImageStorage,
      fileSystem: FileSystem
  ): Boolean =
    imagePool.getSaveLocationAndInfo(image) match
      case (Some(loc), Some(info)) => imageGrid.save(image, fileSystem, loc, info)
      case _                       => false

  def saveAs(imageGrid: ImageGrid, imagePool: ImagePool, image: GridCell, fileSystem: FileSystem)(
      askForSaveFile: GridCell => Option[File],
      askForFileSaveSettings: (File, GridCell) => Option[FileSaveSettings],
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean =
    val didMoveOpt = for
      file <- askForSaveFile(image)
      settings <- askForFileSaveSettings(file, image)
    yield
      val location = ImagePool.SaveLocation(file, settings.offset)
      val info = ImagePool.SaveInfo(settings.format)
      imageGrid.setImageSource(image.storage, location, info)(
        imagePool,
        imageSaveCollisionHandler
      )

    val didMove = didMoveOpt.getOrElse(false)

    if didMove then
      val saved = trySaveImage(imageGrid, imagePool, image.storage, fileSystem)
      if (!saved) println("Image could not be saved!!")
      saved
    else false

  def createNewImage(imageGrid: ImageGrid, backgroundColor: Color, coords: GridCoords): Unit =
    val storage = ImageStorage.fill(imageGrid.imageSize, backgroundColor)
    imageGrid.set(new GridCell(coords, storage))

  def openImage(
      model: TriPaintModel,
      file: File,
      fileOpenSettings: FileOpenSettings,
      whereToPutImage: GridCoords
  ): Unit =
    val FileOpenSettings(offset, format) = fileOpenSettings
    val location = ImagePool.SaveLocation(file, offset)
    val imageSize = model.imageGrid.imageSize

    CachedLoader(
      cached = model.imagePool.imageAt(location),
      load = loadImageFromFile(location, format, imageSize, model.fileSystem)
    ) match
      case Success((image, found)) =>
        if !found then model.imagePool.set(image, location, SaveInfo(format))
        model.imageGrid.set(new GridCell(whereToPutImage, image))
      case Failure(exc) => exc.printStackTrace()

  private def loadImageFromFile(
      location: SaveLocation,
      format: StorageFormat,
      imageSize: Int,
      fileSystem: FileSystem
  ): Try[ImageStorage] =
    for
      regularImage <- fileSystem.readImage(location.file) match
        case Some(im) => Success(im)
        case None     => Failure(new RuntimeException("no such image"))
      image <- ImageStorage.fromRegularImage(regularImage, location.offset, format, imageSize)
    yield image

  def openHexagon(
      model: TriPaintModel,
      file: File,
      fileOpenSettings: FileOpenSettings,
      coords: GridCoords
  ): Unit =
    val imageSize = model.imageGrid.imageSize
    val FileOpenSettings(offset, format) = fileOpenSettings

    def coordOffset(idx: Int): (Int, Int) =
      idx match
        case 0 => (0, 0)
        case 1 => (-1, 0)
        case 2 => (-2, 0)
        case 3 => (-1, -1)
        case 4 => (0, -1)
        case 5 => (1, -1)

    for idx <- 0 until 6 do
      val imageOffset = StorageCoords(offset.x + idx * imageSize, offset.y)

      val off = coordOffset(idx)
      val whereToPutImage = GridCoords(coords.x + off._1, coords.y + off._2)

      openImage(model, file, FileOpenSettings(imageOffset, format), whereToPutImage)

  def applyEffect(model: TriPaintModel, effect: Effect): Unit =
    val grid = model.imageGrid
    val images = grid.selectedImages

    val before = for im <- images yield im.storage.allPixels.map(im.storage.getColor)

    effect.action(images.map(_.coords), grid)

    val after = for im <- images yield im.storage.allPixels.map(im.storage.getColor)

    val changes = mutable.Map.empty[GridCoords, ImageChange]
    for here <- images.indices do
      val image = images(here)
      val allPixels = image.storage.allPixels

      val changeBuilder = new ImageChange.Builder
      for
        neigh <- allPixels.indices
        if before(here)(neigh) != after(here)(neigh)
      yield changeBuilder.addChange(allPixels(neigh), before(here)(neigh), after(here)(neigh))

      if changeBuilder.nonEmpty then changes(image.coords) = changeBuilder.done(image.storage)

    for (coords, change) <- changes do change.undo()

    grid.performChange(new ImageGridChange(changes.toMap))
}
