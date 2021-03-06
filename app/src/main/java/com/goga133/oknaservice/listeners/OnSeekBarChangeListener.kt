package com.goga133.oknaservice.listeners

import android.widget.SeekBar

open class OnSeekBarChangeListener(private val callback: (SeekBar?, Int) -> Unit) :
    SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        callback(seekBar, progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}