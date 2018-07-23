package cc.hellokk.cutvideotime.adapter

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import cc.hellokk.cutvideotime.R
import cc.hellokk.cutvideotime.bean.VideoEditInfo
import cc.hellokk.cutvideotime.tools.ImageUtils
import cc.hellokk.cutvideotime.tools.Utils.dip2px
import cc.hellokk.cutvideotime.tools.Utils.getScreenWidth
import com.qiongliao.qiongliao.base.CommonRecyclerViewAdapter
import com.qiongliao.qiongliao.base.CommonRecyclerViewHolder

/**
 * 作者: Kun on 2018/4/2 10:27
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
class VideoEditAdapter(context: Context?, list: MutableList<VideoEditInfo>?) : CommonRecyclerViewAdapter<VideoEditInfo>(context, list) {

    override fun convert(h: CommonRecyclerViewHolder, entity: VideoEditInfo, position: Int) {
        val iv = h.getView<ImageView>(R.id.id_image)
        val layoutParams = iv?.layoutParams as LinearLayout.LayoutParams
        layoutParams.width = (getScreenWidth(context) - dip2px(context, 70f)) / 10
        iv.layoutParams = layoutParams

        ImageUtils.loadImage(context, "file://" + entity.path, h.getView(R.id.id_image))
    }

    override fun getLayoutViewId(viewType: Int) = R.layout.item_video
}