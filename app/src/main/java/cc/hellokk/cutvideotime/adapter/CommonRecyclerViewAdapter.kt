package com.qiongliao.qiongliao.base

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * Created by 小金子 on 2016/4/28.
 * 通用的RecyclerView的适配器
 * 1.支持添加头部
 * 2.支持监听条目点击事件[CommonRecyclerViewAdapter.setOnRecyclerViewItemClickListener]
 * 3.支持监听条目中的控件的点击事件[CommonRecyclerViewAdapter.setOnViewInItemClickListener]
 */
abstract class CommonRecyclerViewAdapter<T> : RecyclerView.Adapter<CommonRecyclerViewHolder> {

    /**
     * 条目的点击事件的监听接口
     */
    private var onRecyclerViewItemClickListener: OnRecyclerViewItemClickListener? = null

    /**
     * 条目里面的控件的点击事件的监听接口
     */
    private var onViewInItemClickListener: OnViewInItemClickListener? = null

    /**
     * item中的需要监听的view的id
     */
    private var viewIdsInItem: IntArray? = null

    /**
     *
     */
    private var itemTypesForItem: IntArray? = null

    /**
     *
     */
    private var itemTypesForViewInItem: IntArray? = null

    /**
     * 上下文对象
     */
    protected var context: Context

    /**
     * 要显示的数据
     */
    var data: MutableList<T>? = null

    /**
     * 记录当前的position
     */
    private var mCurrentPosition = -1

    /**
     * 头部的试图
     */
    private val headers = ArrayList<View>()

    /**
     * 获取头部试图的个数
     *
     * @return
     */
    val headerCounts: Int
        get() = headers.size

    /**
     * 添加头部的试图
     *
     * @param view
     * @param isNotify 是否通知插入一个条目
     */
    @JvmOverloads
    fun addHeaderView(view: View?, isNotify: Boolean = false) {
        if (view == null) {
            throw NullPointerException("the header view can not be null")
        }
        headers.add(view)
        if (isNotify) {
            notifyItemInserted(headers.size - 1)
        }
    }

    /**
     * 删除一个试图
     *
     * @param position
     */
    @JvmOverloads
    fun removeHeaderView(position: Int, isNotify: Boolean = false) {
        headers.removeAt(position)
        if (isNotify) {
            notifyItemRemoved(position)
        }
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param data    显示的数据
     */
    constructor(context: Context?, data: MutableList<T>?) {
        this.data = data
        this.context = context!!
    }

    constructor(context: Context) {
        this.data = ArrayList()
        this.context = context
    }

    /**
     * viewType 是通过[RecyclerView.Adapter.getItemViewType]获取到的
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonRecyclerViewHolder {
        var view: View? = null
        view = if (viewType == RecyclerView.INVALID_TYPE && mCurrentPosition != -1 && mCurrentPosition < headers.size) { //说明是头部
            headers[mCurrentPosition]
        } else {
            LayoutInflater.from(context).inflate(getLayoutViewId(viewType), parent, false)
            //            view = View.inflate(context, getLayoutViewId(viewType), parent,false);
            //            Log.e("aiitec","LayoutParams:"+view.getLayoutParams());

            //            if (view.getLayoutParams() == null) {//view.getLayoutParams()居然是空的，然后xml写的宽满屏，跑起来就不满屏了，所以加上下面这句，让布局宽满屏
            //                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //                view.setLayoutParams(layoutParams);
            //            }
        }

        val vh = CommonRecyclerViewHolder(view!!)
        //视图被创建的时候调用
        viewCreated(vh, viewType)
        return vh
    }

    /**
     * 获取条目的类型
     *
     * @param position
     * @return
     */
    override fun getItemViewType(position: Int): Int {
        mCurrentPosition = position
        val size = headers.size
        return if (position < size) {
            RecyclerView.INVALID_TYPE
        } else getItemType(position - size)
    }

    fun updateList(list: List<T>) {
        this.data?.clear()
        this.data?.addAll(list)
        notifyDataSetChanged()
    }


    fun getItem(position: Int): T {
        return data!![position]
    }

    /**
     * 获取条目的类型
     *
     * @return
     */
    private fun getItemType(position: Int): Int {
        return 0
    }

    override fun onBindViewHolder(h: CommonRecyclerViewHolder, position: Int) {
        var position = position
        val size = headers.size
        if (position < size) {
            //如果是头部,不处理
        } else {
            position -= size
            if (isNeedSetClickListener(position)) { //如果需要设置监听
                h.itemView.setOnClickListener(MyItemClickListenerAdapter(h))
            }
            if (viewIdsInItem != null && viewIdsInItem!!.isNotEmpty() && isNeedSetViewInItemClickListener(position)) {
                for (i in viewIdsInItem!!.indices) {
                    val v = h.getView<View>(viewIdsInItem!![i])
                    v?.setOnClickListener(MyViewInItemClickListenerAdapter(h, viewIdsInItem!![i]))
                }
            }
            convert(h, data!![position], position)
        }
    }

    /**
     * 判断是否需要设置监听的
     *
     * @param position
     * @return
     */
    private fun isNeedSetClickListener(position: Int): Boolean {
        if (itemTypesForItem == null || itemTypesForItem!!.isEmpty()) {
            return true
        }
        val itemType = getItemType(position)
        return itemTypesForItem!!.indices.any { itemType == itemTypesForItem!![it] }
    }

    /**
     * 判断是否需要设置item内的控件的监听事件
     *
     * @param position
     * @return
     */
    private fun isNeedSetViewInItemClickListener(position: Int): Boolean {
        if (itemTypesForViewInItem == null || itemTypesForViewInItem!!.isEmpty()) {
            return true
        }
        val itemType = getItemType(position)
        return itemTypesForViewInItem!!.indices.any { itemType == itemTypesForViewInItem!![it] }
    }

    /**
     * 实现列表的显示
     *
     * @param h        RecycleView的ViewHolder
     * @param entity   实体对象
     * @param position 当前的下标
     */
    abstract fun convert(h: CommonRecyclerViewHolder, entity: T, position: Int)

    /**
     * 布局文件被转化成View的时候调用
     *
     * @param vh
     * @param viewType
     */
    private fun viewCreated(vh: CommonRecyclerViewHolder, viewType: Int) {}

    /**
     * @param viewType 返回值就是根据这个值进行判断返回的
     * 对头部不起作用
     * @return
     */
    abstract fun getLayoutViewId(viewType: Int): Int

    /**
     * 集合的长度和头部试图的个数
     *
     * @return
     */
    override fun getItemCount(): Int {
        return data!!.size + headers.size
    }

    /**
     * 设置所有的条目的监听事件
     *
     * @param onRecyclerViewItemClickListener
     */
    open fun setOnRecyclerViewItemClickListener(onRecyclerViewItemClickListener: OnRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener
    }

    /**
     * 设置条目的监听,具有一个筛选作用<br></br>
     * 针对[CommonRecyclerViewAdapter.getItemType]}的值在
     *
     * @param onRecyclerViewItemClickListener
     * @param itemTypes                       只有通过
     */
    open fun setOnRecyclerViewItemClickListener(onRecyclerViewItemClickListener: OnRecyclerViewItemClickListener, vararg itemTypes: Int) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener
        this.itemTypesForItem = itemTypes
    }

    /**
     * 每一个item由于都是一样的,那么里面的有些控件有时候需要点击事件,那么这里框架代为处理
     */
    interface OnViewInItemClickListener {

        /**
         * 回调的方法
         *
         * @param v
         * @param position
         */
        fun onViewInItemClick(v: View, position: Int)

    }

    /**
     * 设置条目里面的控件的监听事件
     *
     * @param onViewInItemClickListener 回掉接口
     * @param viewIdsInItem             item中需要监听的view的id数组,可以为null
     */
    open fun setOnViewInItemClickListener(onViewInItemClickListener: OnViewInItemClickListener, vararg viewIdsInItem: Int) {
        this.viewIdsInItem = viewIdsInItem
        this.onViewInItemClickListener = onViewInItemClickListener
    }

    /**
     * 设置item里面的控件的点击事件起作用的ItemType
     *
     * @param itemTypes
     */
    fun setItemTypesInItem(vararg itemTypes: Int) {
        this.itemTypesForViewInItem = itemTypes
    }

    /**
     * 条目的点击事件
     */
    interface OnRecyclerViewItemClickListener {

        /**
         * 回调的方法
         *
         * @param v
         * @param position
         */
        fun onItemClick(v: View, position: Int)

    }

    /**
     * 实现点击的接口,每一个ViewItem都对应一个这个类,每一个都不一样的对象
     */
    private inner class MyItemClickListenerAdapter(
            /**
             * 条目的下标
             */
            private val h: CommonRecyclerViewHolder) : View.OnClickListener {

        override fun onClick(v: View) {
            if (onRecyclerViewItemClickListener != null) {
                //回调方法
                onRecyclerViewItemClickListener!!.onItemClick(v, h.adapterPosition - headers.size)
            }
        }

    }

    /**
     * 实现点击的接口,每一个ViewInItem都对应一个这个类,每一个都不一样的对象
     */
    private inner class MyViewInItemClickListenerAdapter(
            /**
             * 条目的下标
             */
            private val h: CommonRecyclerViewHolder, private val viewId: Int) : View.OnClickListener {

        override fun onClick(v: View) {
            if (onViewInItemClickListener != null) {
                //回调方法
                onViewInItemClickListener!!.onViewInItemClick(v, h.adapterPosition - headers.size)
            }
        }

    }

}
