package cc.hellokk.cutvideotime.app

import android.app.Application
import cc.hellokk.cutvideotime.tools.NotNullSingleValueVar

/**
 * 作者: Kun on 2018/6/6 14:23
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
class App : Application() {
    companion object {
        /**将Application 单例化，可供全局调用 Context */
        var instance: App by NotNullSingleValueVar.DelegatesExt.notNullSingleValue()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}