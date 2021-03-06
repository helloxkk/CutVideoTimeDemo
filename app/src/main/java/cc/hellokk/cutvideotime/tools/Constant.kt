package cc.hellokk.cutvideotime.tools

import java.io.File

/**
 * 作者: Kun on 2018/6/6 14:21
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
object Constant {
    val MIN_CAT_VIDEO_TIME = 15000 // 最小剪切视频时长
    object Path {
        val BASE_PATH = FileUtils().getSdPath() + File.separator + "CutVideoTime"//存储路径
        var VIDEO_PATH = BASE_PATH + "/video/"
        var VIDEO_IMAGE_PATH = VIDEO_PATH + "/image/"
    }

    object Key {
        val KEY_LOCAL_VIDEO_PATH = "LocalVideoPath"
        val KEY_SAVE_VIDEO_PATH = "SaveVideoPath"
        val KEY_IS_COMPOSITE_VIDEO = "IsCompositeVideo"
        val KEY_PROGRESS = "progress"
        val KEY_IS_CUT_VIDEO = "isCutVideo"
        val KEY_START_M = "startM"
        val KEY_END_M = "endM"
    }
}