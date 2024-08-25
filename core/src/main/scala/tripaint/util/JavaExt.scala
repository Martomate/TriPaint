package tripaint.util

import java.util.Optional

object JavaExt {

  extension [T](opt: Optional[T]) {
    def toScala: Option[T] = if opt.isPresent then Some(opt.get) else None
  }
}
