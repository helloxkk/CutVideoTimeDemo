package cc.hellokk.cutvideotime.tools

import android.os.Handler

/**
 * 作者: Kun on 2018/6/6 10:18
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
class ExtractFrameWorkThread(extractW: Int, extractH: Int, handler: Handler?, private var videoPath: String?, private var outPutFileDirPath: String?, private var startPosition: Long, private var endPosition: Long, private var thumbnailsCount: Int) : Thread() {
    private var mVideoExtractFrameAsyncUtils: VideoExtractFrameAsyncUtils? = VideoExtractFrameAsyncUtils(extractW, extractH, handler)

    override fun run() {
        super.run()
        mVideoExtractFrameAsyncUtils?.getVideoThumbnailsInfoForEdit(
                videoPath,
                outPutFileDirPath,
                startPosition,
                endPosition,
                thumbnailsCount)
    }

    fun stopExtract() {
        mVideoExtractFrameAsyncUtils?.stopExtract()
    }

    companion object {
        val MSG_SAVE_SUCCESS = 0
    }

}