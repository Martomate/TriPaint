package com.martomate.tripaint.model

import java.io.File

case class SaveLocation(file: File, offset: (Int, Int) = (0, 0))
