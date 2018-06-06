package cc.hellokk.cutvideotime.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import cc.hellokk.cutvideotime.R
import cc.hellokk.cutvideotime.tools.Constant
import org.jetbrains.anko.startActivity

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class CutVideoTimeActivity : AppCompatActivity() {

    companion object {
        val LOCAL_VIDEO_PATH = "local_video_path"
        val sSaveVideoPath = Constant.Path.VIDEO_PATH + System.currentTimeMillis().toString()
        var sLocalVideoPath: String? = null

        fun startActivity(context: Context, videoPath: String) {
            context.startActivity<CutVideoTimeActivity>(LOCAL_VIDEO_PATH to videoPath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cut_video_time)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Log.e(" 视频存储路径 ", sSaveVideoPath)
        sLocalVideoPath = intent.getStringExtra(LOCAL_VIDEO_PATH)
    }
}
