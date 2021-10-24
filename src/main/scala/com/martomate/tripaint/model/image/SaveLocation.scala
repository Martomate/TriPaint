package com.martomate.tripaint.model.image

import com.martomate.tripaint.model.coords.StorageCoords

import java.io.File

case class SaveLocation(file: File, offset: StorageCoords = StorageCoords(0, 0))
