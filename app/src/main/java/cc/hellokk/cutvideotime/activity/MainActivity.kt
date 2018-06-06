package cc.hellokk.cutvideotime.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import cc.hellokk.cutvideotime.R
import cc.hellokk.cutvideotime.tools.Constant
import cc.hellokk.cutvideotime.tools.ExtractVideoInfoUtil
import cc.hellokk.cutvideotime.tools.UriUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {

            //检查当前权限（若没有该权限，值为-1；若有该权限，值为0）
            val hasReadExternalStoragePermission = ContextCompat.checkSelfPermission(application, Manifest.permission.READ_EXTERNAL_STORAGE)
            Log.e("PERMISION_CODE", hasReadExternalStoragePermission.toString())
            if (hasReadExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                selectVideo()
            } else {
                //若没有授权，会弹出一个对话框（这个对话框是系统的，开发者不能自己定制），用户选择是否授权应用使用系统权限
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    //用户选择是否同意授权后，会回调这个方法
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意授权，执行读取文件的代码
                selectVideo()
            } else {
                //用户不同意授权
                Snackbar.make(fab, "未开启读写权限", Snackbar.LENGTH_LONG).setAction("ok", null).show()
            }
        }
    }

    private fun selectVideo() {
        val i = Intent()
        //intent.setType("image/*");
        // intent.setType("audio/*"); //选择音频
        i.type = "video/*" //选择视频 （mp4 3gp 是android支持的视频格式）
        // intent.setType("video/*;image/*");//同时选择视频和图片
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(i, REQUEST_SELECT_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_VIDEO -> if (resultCode == Activity.RESULT_OK && null != data) {
                val uri = data.data
                val videoPath = UriUtils.getPath(this, uri).toString()
                Log.e("localVideoPath: ", videoPath)
                if (ExtractVideoInfoUtil(videoPath).videoLength.toLong() > Constant.MIN_CAT_VIDEO_TIME) {
                    CutVideoTimeActivity.startActivity(this, videoPath)
                    finish()
                } else {
                    Snackbar.make(fab, "请选择大于15秒的视频", Snackbar.LENGTH_LONG).setAction("ok", null).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return if (item.itemId == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    companion object {
        private val REQUEST_SELECT_VIDEO = 101
    }
}
