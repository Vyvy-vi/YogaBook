package com.vyvyvi.yogabook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object InternalStorageHelper {
    fun loadImageFromInternalStorage(context: Context, filename: String): Bitmap {
        val file = File(context.filesDir, filename)
        val imgBitmap = BitmapFactory.decodeFile(file.absolutePath)
        return imgBitmap
    }

    fun saveImageToInternalStorage(context: Context, filename: String, bitmap: Bitmap) {
        val file = File(context.filesDir, filename)
        val stream = file.outputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG,100,stream)
        stream.close()
    }
}
