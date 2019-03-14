package cc.hellokk.cutvideotime.activity

import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import cc.hellokk.cutvideotime.R
import cc.hellokk.cutvideotime.adapter.VideoEditAdapter
import cc.hellokk.cutvideotime.bean.VideoEditInfo
import cc.hellokk.cutvideotime.costomview.CutVideoTimeView
import cc.hellokk.cutvideotime.costomview.EditSpacingItemDecoration
import cc.hellokk.cutvideotime.tools.Constant
import cc.hellokk.cutvideotime.tools.ExtractFrameWorkThread
import cc.hellokk.cutvideotime.tools.ExtractVideoInfoUtil
import cc.hellokk.cutvideotime.tools.Utils.dip2px
import cc.hellokk.cutvideotime.tools.Utils.getScreenWidth
import kotlinx.android.synthetic.main.activity_cut_video_time.*
import org.jetbrains.anko.startActivity
import java.io.File
import java.lang.ref.WeakReference


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class CutVideoTimeActivity : AppCompatActivity() {
    private var mMediaPlayer: MediaPlayer? = null
    private var MIN_CUT_DURATION = 3 * 1000L// 最小剪辑时间3s
    private var MAX_CUT_DURATION = 15 * 1000L//视频最多剪切多长时间
    private var MAX_COUNT_RANGE = 10//显示缩略图的区域内一共有多少张图片
    private var mExtractVideoInfoUtil: ExtractVideoInfoUtil? = null
    private var mMaxWidth: Int = 0
    private var duration: Long = 0
    private var mCutVideoTimeView: CutVideoTimeView? = null
    private var videoEditAdapter: VideoEditAdapter? = null
    private var averageMsPx: Float = 0.toFloat()//每毫秒所占的px
    private var averagePxMs: Float = 0.toFloat()//每px所占用的ms毫秒
    private var mOutPutFileDirPath: String? = null
    private var mExtractFrameWorkThread: ExtractFrameWorkThread? = null
    private var leftProgress: Long = 0
    private var rightProgress: Long = 0
    private var scrollPos: Long = 0
    private var mScaledTouchSlop: Int = 0
    private var lastScrollX: Int = 0
    private var isSeeking: Boolean = false
    private var mVideoEditInfoList: MutableList<VideoEditInfo>? = null

    companion object {
        private val TAG: String = CutVideoTimeActivity::class.java.simpleName
        /** 视频宽度  */
        var VIDEO_WIDTH = 1280
        /** 视频高度  */
        var VIDEO_HEIGHT = 720
        val LOCAL_VIDEO_PATH = "local_video_path"
        var sLocalVideoPath: String? = null

        fun startActivity(context: Context, videoPath: String) {
            context.startActivity<CutVideoTimeActivity>(LOCAL_VIDEO_PATH to videoPath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cut_video_time)
        initWindow()
        initRecyclerView()
        initData()
        initEditVideo()
        initListeners()
    }

    private fun initWindow() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        } else {
            window.decorView.systemUiVisibility = View.GONE
        }
    }

    private fun initListeners() {
        ib_back.setOnClickListener {
            showBackDialog(getString(R.string.whether_to_exit_editing))
        }

        ib_finish.setOnClickListener {
            if (rightProgress - leftProgress > 15000) {
                Log.e(TAG, getString(R.string.video_can_not_be_longer_than_15_seconds))
            } else {
                startPublishVideoActivity()
            }
        }

        texture_view.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                initMediaPlay(surface)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
    }

    private fun initEditVideo() {
        //for video edit
        val startPosition: Long = 0
        val endPosition = duration.toLong()
        val thumbnailsCount: Int
        val rangeWidth: Int
        val isOver_15_s: Boolean
        if (endPosition <= MAX_CUT_DURATION) {
            isOver_15_s = false
            thumbnailsCount = MAX_COUNT_RANGE
            rangeWidth = mMaxWidth
        } else {
            isOver_15_s = true
            thumbnailsCount = (endPosition * 1.0f / (MAX_CUT_DURATION * 1.0f) * MAX_COUNT_RANGE).toInt()
            rangeWidth = mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount
        }
        id_rv_id.addItemDecoration(EditSpacingItemDecoration(dip2px(this, 35f), thumbnailsCount))

        //CutVideoTimeView init
        if (isOver_15_s) {
            mCutVideoTimeView = CutVideoTimeView(this, 0L, MAX_CUT_DURATION)
            mCutVideoTimeView?.setSelectedMinValue(0L)
            mCutVideoTimeView?.setSelectedMaxValue(MAX_CUT_DURATION)
        } else {
            mCutVideoTimeView = CutVideoTimeView(this, 0L, endPosition)
            mCutVideoTimeView?.setSelectedMinValue(0L)
            mCutVideoTimeView?.setSelectedMaxValue(endPosition)
        }

        mCutVideoTimeView?.setMinCutTime(MIN_CUT_DURATION)//设置最小裁剪时间
        mCutVideoTimeView?.setNotifyWhileDragging(true)
        mCutVideoTimeView?.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener)
        id_seekBarLayout.addView(mCutVideoTimeView)
        averageMsPx = duration * 1.0f / rangeWidth * 1.0f
        mOutPutFileDirPath = getSaveEditThumbnailDir(this)
        val extractW = (getScreenWidth(this) - dip2px(this, 70f)) / MAX_COUNT_RANGE
        val extractH = dip2px(this, 55f)
        mExtractFrameWorkThread = ExtractFrameWorkThread(extractW, extractH, mUIHandler, sLocalVideoPath, mOutPutFileDirPath, startPosition, endPosition, thumbnailsCount)
        mExtractFrameWorkThread?.start()

        //init pos icon start
        leftProgress = 0
        rightProgress = if (isOver_15_s) {
            MAX_CUT_DURATION
        } else {
            endPosition
        }
        averagePxMs = mMaxWidth * 1.0f / (rightProgress - leftProgress)
    }

    private val mUIHandler = MainHandler(this)

    private class MainHandler internal constructor(activity: CutVideoTimeActivity) : Handler() {
        private val mActivity: WeakReference<CutVideoTimeActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            if (activity != null) {
                if (msg.what == ExtractFrameWorkThread.MSG_SAVE_SUCCESS) {
                    if (activity.videoEditAdapter != null) {
                        val info = msg.obj as VideoEditInfo
                        activity.mVideoEditInfoList?.add(info)
                        activity.videoEditAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private val mOnRangeSeekBarChangeListener = object : CutVideoTimeView.OnRangeSeekBarChangeListener {
        override fun onRangeSeekBarValuesChanged(bar: CutVideoTimeView, minValue: Long, maxValue: Long, action: Int, isMin: Boolean, pressedThumb: CutVideoTimeView.Thumb) {
            leftProgress = minValue + scrollPos
            rightProgress = maxValue + scrollPos
            setDuration((rightProgress - leftProgress) / 1000)
            Log.e(TAG, "minValue: $minValue   maxValue: $maxValue")
            Log.e(TAG, "leftProgress: " + leftProgress / 1000 + "   rightProgress: " + rightProgress / 1000)
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    isSeeking = false
                    videoPause()
                }
                MotionEvent.ACTION_MOVE -> {
                    isSeeking = true
                    mMediaPlayer?.seekTo(
                            if (pressedThumb === CutVideoTimeView.Thumb.MIN)
                                leftProgress.toInt()
                            else
                                rightProgress.toInt())
                }
                MotionEvent.ACTION_UP -> {
                    isSeeking = false
                    //从minValue开始播
                    mMediaPlayer?.seekTo(leftProgress.toInt())
                    videoStart()
                }
                else -> {
                }
            }
        }
    }

    private fun setDuration(duration: Long) {
        tv_duration.text = getString(R.string.clipped_time, duration.toString())
    }

    private fun videoStart() {
        mMediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                positionIcon.clearAnimation()
                animator?.apply { if (isRunning) cancel() }
                anim()
                handler.removeCallbacks(run)
                handler.post(run)
            }
        }
    }

    private fun initData() {
        // 本地视频路径
        sLocalVideoPath = intent.getStringExtra(LOCAL_VIDEO_PATH)

        if (!File(sLocalVideoPath).exists()) {
            Toast.makeText(this, getString(R.string.video_file_does_not_exist), Toast.LENGTH_LONG).show()
            finish()
        }

        mExtractVideoInfoUtil = ExtractVideoInfoUtil(sLocalVideoPath)
        duration = mExtractVideoInfoUtil?.videoLength?.toLong()!!

        mMaxWidth = getScreenWidth(this) - dip2px(this, 70f)
        mScaledTouchSlop = ViewConfiguration.get(this).scaledTouchSlop
        setDuration(15)
    }

    private fun initRecyclerView() {
        mVideoEditInfoList = arrayListOf()
        id_rv_id.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        videoEditAdapter = VideoEditAdapter(this, mVideoEditInfoList)
        id_rv_id.adapter = videoEditAdapter
        id_rv_id.addOnScrollListener(mOnScrollListener)
    }

    private var isOverScaledTouchSlop: Boolean = false

    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                isSeeking = false
                videoStart()
            } else {
                isSeeking = true
                mMediaPlayer?.apply {
                    if (isOverScaledTouchSlop && isPlaying) {
                        videoPause()
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            isSeeking = false
            val scrollX = getScrollXDistance()
            //达不到滑动的距离
            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                isOverScaledTouchSlop = false
                return
            }
            isOverScaledTouchSlop = true
            //初始状态,why ? 因为默认的时候有35dp的空白！
            if (scrollX == -dip2px(this@CutVideoTimeActivity, 35f)) {
                scrollPos = 0
            } else {
                // why 在这里处理一下,因为onScrollStateChanged早于onScrolled回调
                mMediaPlayer?.apply { if (isPlaying) videoPause() }
                isSeeking = true
                scrollPos = (averageMsPx * (dip2px(this@CutVideoTimeActivity, 35f) + scrollX)).toLong()
                leftProgress = mCutVideoTimeView?.getSelectedMinValue()!! + scrollPos
                rightProgress = mCutVideoTimeView?.getSelectedMaxValue()!! + scrollPos

                mMediaPlayer?.seekTo(leftProgress.toInt())
            }
            lastScrollX = scrollX
        }
    }

    /**
     * 水平滑动了多少px
     *
     * @return int px
     */
    private fun getScrollXDistance(): Int {
        val layoutManager = id_rv_id.layoutManager as LinearLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = layoutManager.findViewByPosition(position)
        val itemWidth = firstVisibleChildView.width
        return position * itemWidth - firstVisibleChildView.left
    }

    private var animator: ValueAnimator? = null

    private fun anim() {
        if (positionIcon.visibility == View.GONE) {
            positionIcon.visibility = View.VISIBLE
        }
        val params = positionIcon.layoutParams as FrameLayout.LayoutParams
        val start = (dip2px(this, 35f) + (leftProgress - scrollPos) * averagePxMs).toInt()
        val end = (dip2px(this, 35f) + (rightProgress - scrollPos) * averagePxMs).toInt()
        animator = ValueAnimator
                .ofInt(start, end)
                .setDuration(rightProgress - scrollPos - (leftProgress - scrollPos))
        animator?.interpolator = LinearInterpolator()
        animator?.addUpdateListener { animation ->
            params.leftMargin = animation.animatedValue as Int
            positionIcon.layoutParams = params
        }
        animator?.start()
    }

    private fun videoPause() {
        isSeeking = false
        mMediaPlayer?.apply {
            if (isPlaying) {
                pause()
                handler.removeCallbacks(run)
            }
        }
        if (positionIcon.visibility == View.VISIBLE) {
            positionIcon.visibility = View.GONE
        }
        positionIcon.clearAnimation()
        if (animator != null && animator?.isRunning!!) {
            animator?.cancel()
        }
    }

    private val handler = Handler()
    private val run = object : Runnable {

        override fun run() {
            videoProgressUpdate()
            handler.postDelayed(this, 1000)
        }
    }

    private fun videoProgressUpdate() {
        mMediaPlayer?.apply {
            if (currentPosition >= rightProgress) {
                seekTo(leftProgress.toInt())
                positionIcon.clearAnimation()
                animator?.apply { if (isRunning) cancel() }
                anim()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.apply { release() }
        animator?.apply { cancel() }
        mExtractVideoInfoUtil?.apply { release() }
        id_rv_id.removeOnScrollListener(mOnScrollListener)
        mExtractFrameWorkThread?.apply { stopExtract() }
        mUIHandler.removeCallbacksAndMessages(null)
        handler.removeCallbacksAndMessages(null)
        if (!TextUtils.isEmpty(mOutPutFileDirPath)) {
            deleteFile(File(mOutPutFileDirPath))
        }
    }

    private fun startPublishVideoActivity() {
        PublishVideoActivity.startActivity(this, sLocalVideoPath.toString(), true, (leftProgress / 1000).toInt(), (rightProgress / 1000).toInt())
        finish()
    }

    private fun initMediaPlay(surface: SurfaceTexture) {
        try {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer?.apply {
                setDataSource(sLocalVideoPath)
                setSurface(Surface(surface))
                isLooping = true
                //设置videoview的OnPrepared监听
                setOnPreparedListener({ mp ->
                    initVideoSize(videoWidth, videoHeight)
                    Handler().postDelayed({ videoStart() }, 500)
                    //设置MediaPlayer的OnSeekComplete监听
                    mp.setOnSeekCompleteListener {
                        if (!isSeeking) {
                            videoStart()
                        }
                    }
                })
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化视频大小
     *
     * @param videoWidth
     * @param videoHeight
     */
    private fun initVideoSize(videoWidth: Int, videoHeight: Int) {
        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        val max: Float = if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            Math.max(videoWidth.toFloat() / texture_view.width, videoHeight.toFloat() / texture_view.height)
        } else {
            //横屏模式下按视频高度计算放大倍数值
            Math.max(videoWidth.toFloat() / texture_view.height, videoHeight.toFloat() / texture_view.width)
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        texture_view.layoutParams = RelativeLayout.LayoutParams(Math.ceil((videoWidth.toFloat() / max).toDouble()).toInt(), Math.ceil((videoHeight.toFloat() / max).toDouble()).toInt())
    }

    override fun onResume() {
        super.onResume()
        mMediaPlayer?.apply {
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        mMediaPlayer?.apply {
            pause()
        }
    }

    private fun getSaveEditThumbnailDir(context: Context): String {
        val folderDir = File(Constant.Path.VIDEO_IMAGE_PATH)
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }
        return folderDir.absolutePath
    }

    private fun deleteFile(f: File) {
        if (f.isDirectory) {
            val files = f.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (i in files.indices) {
                    deleteFile(files[i])
                }
            }
        }
        f.delete()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBackDialog(getString(R.string.whether_to_exit_editing))
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private var mDialog: AlertDialog.Builder? = null

    /**
     * 按返回键显示的对话框
     */
    private fun showBackDialog(message: String) {
        if (mDialog == null) {
            mDialog = AlertDialog.Builder(this)
        }
        mDialog?.setTitle(getString(R.string.dialog_hint))
        mDialog?.setMessage(message)

        mDialog?.setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ -> dialog.dismiss() }
        mDialog?.setPositiveButton(getString(R.string.dialog_confirm)) { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        mDialog?.show()
    }
}
