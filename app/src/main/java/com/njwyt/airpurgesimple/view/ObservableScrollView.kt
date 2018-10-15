package com.njwyt.airpurgesimple.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class ObservableScrollView : ScrollView {
    private var onScrollChangedListener: OnScrollChangedListener? = null

    constructor(context: Context, attrs: AttributeSet,
                defStyle: Int) : super(context, attrs, defStyle) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (this.onScrollChangedListener != null) {
            onScrollChangedListener!!.onScrollChanged(t, oldt)
        }
    }

    fun setOnScrollChangedListener(onScrollChangedListener: OnScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener
    }

    interface OnScrollChangedListener {
        fun onScrollChanged(top: Int, oldTop: Int)
    }
}