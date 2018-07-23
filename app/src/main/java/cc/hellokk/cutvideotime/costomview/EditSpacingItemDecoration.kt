package cc.hellokk.cutvideotime.costomview

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * 作者: Kun on 2018/7/23 09:55
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
class EditSpacingItemDecoration(private val space: Int, private val thumbnailsCount: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        // 第一个的前面和最后一个的后面
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.left = space
            outRect.right = 0
        } else if (thumbnailsCount > 10 && position == thumbnailsCount - 1) {
            outRect.left = 0
            outRect.right = space
        } else {
            outRect.left = 0
            outRect.right = 0
        }
    }
}