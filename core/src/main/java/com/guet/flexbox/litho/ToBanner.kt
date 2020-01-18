package com.guet.flexbox.litho

import com.facebook.litho.ComponentContext
import com.guet.flexbox.build.AttributeSet
import com.guet.flexbox.litho.widget.Banner

internal object ToBanner : ToComponent<Banner.Builder>(Common) {
    override val attributeAssignSet: AttributeAssignSet<Banner.Builder> by create {
        register("isCircular") { _, _, value: Boolean ->
            isCircular(value)
        }
        register("timeSpan") { _, _, value: Double ->
            timeSpan(value.toLong())
        }
    }

    override fun create(
            c: ComponentContext,
            visibility: Boolean,
            attrs: AttributeSet
    ): Banner.Builder {
        return Banner.create(c)
    }

    override fun onInstallChildren(
            owner: Banner.Builder,
            visibility: Boolean,
            attrs: AttributeSet,
            children: List<ChildComponent>
    ) {
        owner.children(children.map {
            it
        })
    }
}