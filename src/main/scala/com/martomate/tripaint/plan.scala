package com.martomate.tripaint

import com.martomate.tripaint.image.format.StorageFormat
import com.martomate.tripaint.image.storage.ImageStorage
import scalafx.scene.canvas.Canvas

import scala.collection.mutable.ArrayBuffer

object plan {
  private object model {
    object image {
      object format {// The format system is not really used at the moment
        trait SimpleStorageFormat extends StorageFormat // current format

        trait SubImageStorageFormat extends StorageFormat // new format where every quadrant can be read as an image
      }
    }
  }

  /**
    * read from model
    * register listeners on model
    */
  private object view {// TODO: this is outdated
    trait ImagePane {
      val image: ImageStorage
      val canvas: Canvas
    }

    trait ImageGridPane {
//      val collection: ImageGrid
      val imagePanes: Seq[ImagePane]
    }
  }

  object listenableWithHandle {
    abstract class Listenable[L] {
      protected val listeners: ArrayBuffer[L] = ArrayBuffer.empty

      final def addListener(listener: L): Unit = listeners += listener

      final def removeListener(listener: L): Unit = listeners -= listener
    }

    class ListenableHandle[L] extends Listenable[L] {
      final def notifyListeners(func: L => Unit): Unit = listeners.foreach(func)
    }

    class TestListenableClass {
      def somethingInteresting: Listenable[Int => String] = somethingInterestingHandle

      private val somethingInterestingHandle = new ListenableHandle[Int => String]
      somethingInterestingHandle.notifyListeners(_ (7))
    }

    trait TestListenableClassUser {
      val listenable: TestListenableClass
      listenable.somethingInteresting.addListener(a => "hello " + a)
      listenable.somethingInteresting.addListener(a => "hello again " + a)
    }
  }
}
