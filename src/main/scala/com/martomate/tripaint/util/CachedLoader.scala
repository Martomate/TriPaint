package com.martomate.tripaint.util

import scala.util.{Failure, Success, Try}

object CachedLoader {

  /** @return `Success((value, foundInCache))` or `Failure(errorDuringLoad)` */
  def apply[T](cached: => Option[T], load: => Try[T]): Try[(T, Boolean)] =
    cached match
      case Some(value) => Success((value, true))
      case None =>
        load match
          case Success(value) => Success((value, false))
          case Failure(error) => Failure(error)
}
