package com.martomate.tripaint.model

import java.io.File

import com.martomate.tripaint.model.format.StorageFormat

case class SaveLocation(file: File, offset: (Int, Int) = (0, 0))

case class SaveInfo(format: StorageFormat)
