package cc.hellokk.cutvideotime.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cc.hellokk.cutvideotime.R
import cc.hellokk.cutvideotime.tools.Constant
import org.jetbrains.anko.startActivity

class PublishVideoActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = PublishVideoActivity::class.java.simpleName
        val sSaveVideoPath = Constant.Path.VIDEO_PATH + System.currentTimeMillis().toString()
        fun startActivity(context: Context, localVideoPath: String?, isCutVideo: Boolean, startM: Int, endM: Int) {
            context.startActivity<PublishVideoActivity>(
                    Constant.Key.KEY_LOCAL_VIDEO_PATH to localVideoPath.toString(),
                    Constant.Key.KEY_IS_CUT_VIDEO to isCutVideo,
                    Constant.Key.KEY_START_M to startM,
                    Constant.Key.KEY_END_M to endM)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_video)
        Log.e(TAG, " 视频存储路径 $sSaveVideoPath")
    }
}
