package org.jraf.android.fotomator.databinding.adapters

import android.view.View
import androidx.databinding.BindingAdapter

object ViewBindingAdapter {
    @JvmStatic
    @BindingAdapter("visible")
    fun setVisible(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("visible")
    fun setVisibleIfNotNull(view: View, obj: Any?) {
        view.visibility = if (obj != null) View.VISIBLE else View.GONE
    }
}
