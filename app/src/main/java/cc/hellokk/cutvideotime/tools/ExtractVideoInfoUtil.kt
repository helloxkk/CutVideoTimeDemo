package cc.hellokk.cutvideotime.tools

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.text.TextUtils

import java.io.File

/**
 * author: Kun on 2017/11/1 16:40
 * address: vip@hellokk.cc
 * description: #
 */

class ExtractVideoInfoUtil(path: String?) {
    private val mMetadataRetriever: MediaMetadataRetriever?
    private var fileLength: Long = 0//毫秒

    val videoWidth: Int
        get() {
            val w = mMetadataRetriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            var width = -1
            if (!TextUtils.isEmpty(w)) {
                width = Integer.valueOf(w)!!
            }
            return width
        }

    val videoHeight: Int
        get() {
            val h = mMetadataRetriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            var height = -1
            if (!TextUtils.isEmpty(h)) {
                height = Integer.valueOf(h)!!
            }
            return height
        }


    /***
     * 获取视频的长度时间
     *
     * @return String 毫秒
     */
    val videoLength: String
        get() = mMetadataRetriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

    /**
     * 获取视频旋转角度
     *
     * @return
     */
    val videoDegree: Int
        get() {
            var degree = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val degreeStr = mMetadataRetriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                if (!TextUtils.isEmpty(degreeStr)) {
                    degree = Integer.valueOf(degreeStr)!!
                }
            }
            return degree
        }

    init {
        if (TextUtils.isEmpty(path)) {
            throw RuntimeException("path must be not null !")
        }
        val file = File(path)
        if (!file.exists()) {
            throw RuntimeException("path file   not exists !")
        }
        mMetadataRetriever = MediaMetadataRetriever()
        mMetadataRetriever.setDataSource(file.absolutePath)
        val len = videoLength
        fileLength = if (TextUtils.isEmpty(len)) 0 else java.lang.Long.valueOf(len)

    }

    /**
     * 获取视频的典型的一帧图片，不耗时
     *
     * @return Bitmap
     */
    fun extractFrame(): Bitmap {
        return mMetadataRetriever!!.frameAtTime
    }

    /**
     * 获取视频某一帧,不一定是关键帧
     *
     * @param timeMs 毫秒
     */
    fun extractFrame(timeMs: Long): Bitmap? {
        //第一个参数是传入时间，只能是us(微秒)
        //OPTION_CLOSEST ,在给定的时间，检索最近一个帧,这个帧不一定是关键帧。
        //OPTION_CLOSEST_SYNC   在给定的时间，检索最近一个同步与数据源相关联的的帧（关键帧）
        //OPTION_NEXT_SYNC 在给定时间之后检索一个同步与数据源相关联的关键帧。
        //OPTION_PREVIOUS_SYNC  顾名思义，同上
        //        Bitmap bitmap = mMetadataRetriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        var bitmap: Bitmap? = null
        var i = timeMs
        while (i < fileLength) {
            bitmap = mMetadataRetriever!!.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap != null) {
                break
            }
            i += 1000
        }
        return bitmap
    }

    fun release() {
        mMetadataRetriever?.release()
    }

}
