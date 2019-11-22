package com.guet.flexbox.build

import android.view.View
import android.widget.ImageView.ScaleType.*
import com.facebook.litho.ComponentContext
import com.guet.flexbox.widget.NetworkImage

internal object ImageFactory : WidgetFactory<NetworkImage.Builder>() {

    init {
        enumAttr("scaleType",
                mapOf(
                        "center" to CENTER,
                        "fitCenter" to FIT_CENTER,
                        "fitXY" to FIT_XY,
                        "fitStart" to FIT_START,
                        "fitEnd" to FIT_END,
                        "centerInside" to CENTER_INSIDE,
                        "centerCrop" to CENTER_CROP
                ),
                FIT_XY
        ) { _, _, it ->
            scaleType(it)
        }
        colorAttr("borderColor") { _, _, it ->
            borderColor(it)
        }
        numberAttr<Double>("borderRadius") { _, _, it ->
            borderRadius(it.toPx())
        }
        numberAttr<Double>("borderWidth") { _, _, it ->
            borderWidth(it.toPx())
        }
        numberAttr<Float>("blurRadius") { _, _, it ->
            blurRadius(it)
        }
        numberAttr("blurSampling", 1f) { _, _, it ->
            blurSampling(it)
        }
    }

    override fun onCreateWidget(
            c: ComponentContext,
            buildContext: BuildContext,
            attrs: Map<String, String>?,
            visibility: Int
    ): NetworkImage.Builder {
        return NetworkImage.create(c).url(if (visibility == View.GONE) {
            ""
        } else {
            buildContext.tryGetValue(attrs?.get("url"), "")
        })
    }
}
