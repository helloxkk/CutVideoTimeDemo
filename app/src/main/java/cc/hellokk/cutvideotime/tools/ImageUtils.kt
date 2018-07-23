package cc.hellokk.cutvideotime.tools

import android.content.Context
import android.widget.ImageView
import cc.hellokk.cutvideotime.app.App
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

/**
 * 作者: Kun on 2018/7/23 09:44
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
object ImageUtils {
    /**
     * 统一加载图片方法

     * @param context   context
     * *
     * @param path      图片地址
     * *
     * @param imageView 图片展示控件
     */
    fun loadImage(context: Context?, path: String?, imageView: ImageView?) {
        var requestOptions = RequestOptions()
        requestOptions.dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        Glide.with(App.instance).load(path).apply(requestOptions).into(imageView)
    }
}