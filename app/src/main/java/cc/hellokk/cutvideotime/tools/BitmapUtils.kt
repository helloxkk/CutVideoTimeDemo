package cc.hellokk.cutvideotime.tools

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 作者: Kun on 2018/6/6 15:35
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
object BitmapUtils {
    fun saveImageToSDForEdit(bmp: Bitmap?, dirPath: String?, fileName: String?): String {
        if (bmp == null) {
            return ""
        }
        val appDir = File(dirPath)
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }
}