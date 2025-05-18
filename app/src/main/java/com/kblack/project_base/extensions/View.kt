package com.kblack.project_base.extensions

import android.animation.Animator
import android.animation.IntEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.setVisible(isVisible: Boolean) {
    if (isVisible) visible() else gone()
}

fun View.setGone(isGone: Boolean) {
    if (isGone) visible() else gone()
}

fun View.height(height: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.height = height
    layoutParams = params
    return this
}

fun View.width(width: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.width = width
    layoutParams = params
    return this
}

fun View.widthAndHeight(width: Int, height: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.width = width
    params.height = height
    layoutParams = params
    return this
}

fun View.animateWidth(
    targetValue: Int, duration: Long = 400, listener: Animator.AnimatorListener? = null,
    action: ((Float) -> Unit)? = null
): ValueAnimator? {
    var animator: ValueAnimator? = null
    post {
        animator = ValueAnimator.ofInt(width, targetValue).apply {
            addUpdateListener {
                width(it.animatedValue as Int)
                action?.invoke((it.animatedFraction))
            }
            if (listener != null) addListener(listener)
            setDuration(duration)
            start()
        }
    }
    return animator
}

fun View.animateHeight(
    targetValue: Int,
    duration: Long = 400,
    listener: Animator.AnimatorListener? = null,
    action: ((Float) -> Unit)? = null
): ValueAnimator? {
    var animator: ValueAnimator? = null
    post {
        animator = ValueAnimator.ofInt(height, targetValue).apply {
            addUpdateListener {
                height(it.animatedValue as Int)
                action?.invoke((it.animatedFraction))
            }
            if (listener != null) addListener(listener)
            setDuration(duration)
            start()
        }
    }
    return animator
}

fun View.animateWidthAndHeight(
    targetWidth: Int,
    targetHeight: Int,
    duration: Long = 400,
    listener: Animator.AnimatorListener? = null,
    action: ((Float) -> Unit)? = null
): ValueAnimator? {
    var animator: ValueAnimator? = null
    post {
        val startHeight = height
        val evaluator = IntEvaluator()
        animator = ValueAnimator.ofInt(width, targetWidth).apply {
            addUpdateListener {
                widthAndHeight(
                    it.animatedValue as Int,
                    evaluator.evaluate(it.animatedFraction, startHeight, targetHeight)
                )
                action?.invoke((it.animatedFraction))
            }
            if (listener != null) addListener(listener)
            setDuration(duration)
            start()
        }
    }
    return animator
}

fun View.getViewId(): Int {
    var id = id
    if (id == View.NO_ID) {
        id = View.generateViewId()
    }
    return id
}

fun View.setOnSingleClickListener(delayTime: Int = 500, listener: (v: View) -> Unit) {
    setOnClickListener(OnSingleClickListener(delayTime, listener))
}

class OnSingleClickListener(
    private val mDelayTime: Int = 500,
    private val mListener: (v: View) -> Unit
) : View.OnClickListener {

    private var mLastClickTime = 0L
    override fun onClick(v: View) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - mLastClickTime >= mDelayTime) {
            mLastClickTime = currentTimeMillis
            mListener.invoke(v)
        }
    }
}

private lateinit var viewsToAnimate: Array<ViewGroup>

fun showViewsWithAnimation() {
    viewsToAnimate.forEach { view ->
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 500L
            interpolator = DecelerateInterpolator()
            view.isVisible = true
            start()
        }
    }
}

fun hideViewsWithAnimation() {
    viewsToAnimate.forEach { view ->
        if (view.isVisible) {
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                duration = 300L
                interpolator = AccelerateInterpolator()
                start()
            }.doOnEnd { view.isVisible = false }
        }
    }
}


fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
}