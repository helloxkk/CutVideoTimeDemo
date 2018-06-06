package cc.hellokk.cutvideotime.tools

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.os.Handler
import cc.hellokk.cutvideotime.bean.VideoEditInfo

/**
 * 作者: Kun on 2018/6/6 10:17
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
class VideoExtractFrameAsyncUtils(private val extractW: Int, private val extractH: Int, private val mHandler: Handler?) {


    @Volatile private var stop: Boolean = false

    fun getVideoThumbnailsInfoForEdit(videoPath: String?, OutPutFileDirPath: String?, startPosition: Long, endPosition: Long, thumbnailsCount: Int) {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(videoPath)
        val interval = (endPosition - startPosition) / (thumbnailsCount - 1)
        for (i in 0 until thumbnailsCount) {
            if (stop) {
                metadataRetriever.release()
                break
            }
            val time = startPosition + interval * i
            if (i == thumbnailsCount - 1) {
                if (interval > 1000) {
                    val path = extractFrame(metadataRetriever, endPosition - 800, OutPutFileDirPath)
                    sendAPic(path, endPosition - 800)
                } else {
                    val path = extractFrame(metadataRetriever, endPosition, OutPutFileDirPath)
                    sendAPic(path, endPosition)
                }
            } else {
                val path = extractFrame(metadataRetriever, time, OutPutFileDirPath)
                sendAPic(path, time)
            }
        }
        metadataRetriever.release()
    }

    /**
     * 成功add一张
     *
     * @param path path
     * @param time time
     */
    private fun sendAPic(path: String?, time: Long) {
        val info = VideoEditInfo()
        info.path = path
        info.time = time
        val msg = mHandler?.obtainMessage(ExtractFrameWorkThread.MSG_SAVE_SUCCESS)
        msg?.obj = info
        mHandler?.sendMessage(msg)
    }

    private fun extractFrame(metadataRetriever: MediaMetadataRetriever, time: Long, OutPutFileDirPath: String?): String? {
        val bitmap = metadataRetriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        if (bitmap != null) {
            var bitmapNew = scaleImage(bitmap)
            val path = BitmapUtils.saveImageToSDForEdit(bitmapNew, OutPutFileDirPath, System.currentTimeMillis().toString() + "_" + time + ".jpeg")
            if (bitmapNew != null && !bitmapNew.isRecycled) {
                bitmapNew.recycle()
                bitmapNew = null
            }
            return path
        }
        return null
    }

    /**
     * 设置固定的宽度，高度随之变化，使图片不会变形
     *
     * @param bm Bitmap
     * @return Bitmap
     */
    private fun scaleImage(bitmap: Bitmap?): Bitmap? {
        var bm: Bitmap? = bitmap ?: return null
        val width = bm!!.width
        val height = bm.height
        val scaleWidth = extractW * 1.0f / width
        //        float scaleHeight =extractH*1.0f / height;
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleWidth)
        val newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true)
        if (!bm.isRecycled) {
            bm.recycle()
            bm = null
        }
        return newBm
    }

    fun stopExtract() {
        stop = true
    }
}
