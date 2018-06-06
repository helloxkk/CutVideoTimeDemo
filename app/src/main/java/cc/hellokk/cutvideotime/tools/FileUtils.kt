package cc.hellokk.cutvideotime.tools

import android.os.Environment
import cc.hellokk.cutvideotime.app.App
import java.io.File

/**
 * 作者: Kun on 2018/6/6 14:22
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
class FileUtils {
    fun getSdPath(): String {
        var sdDir: File? = null
        val sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED) // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory() // 获取根目录
        } else {
            sdDir = App.instance.filesDir.absoluteFile
        }
        return if (sdDir != null) {
            sdDir.toString()
        } else {
            ""
        }
    }
}