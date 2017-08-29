package com.geek_era.android.Interface.ViewInterface

import android.view.View

/**
 * GeekSeekBar的滑动监听接口
 * Created by nuoon on 2017/8/29.
 * ebsite: http://www.geek-era.com
 * Email:nuoone@163.com
 */
interface GeekSeekBarOnChangeListener{
    fun onProgressChanged(view: View, progress: Int, fromUser: Boolean);
    fun onStartTrackingTouch(view: View);
    fun onStopTrackingTouch(view: View);
}