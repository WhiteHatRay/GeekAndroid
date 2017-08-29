package com.geek_era.android.kotlinproject

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.geek_era.android.Interface.ViewInterface.GeekSeekBarOnChangeListener
import kotlinx.android.synthetic.main.activity_teccirclesb_example.*


/**
 * TecCircleSeekBar样例
 * Created by nuoon on 2017/8/29.
 * Website: http://www.geek-era.com
 * Email:nuoone@163.com
 */
class TecCircleSBExample : AppCompatActivity(), GeekSeekBarOnChangeListener {

    companion object {
        private val TAG = "TecCircleSBExample"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teccirclesb_example)

        TCSB_one.setRotation(100)
        TCSB_one.setSumRotation(200)
        TCSB_one.setChangeListener(this)
    }

    override fun onProgressChanged(view: View, progress: Int, fromUser: Boolean) {
        Log.i(TAG, "当前进度" + progress)
    }
    override fun onStartTrackingTouch(view: View) {
        Log.i(TAG, "onStartTrackingTouch")
    }
    override fun onStopTrackingTouch(view: View) {
        Log.i(TAG, "onStopTrackingTouch")
    }
}