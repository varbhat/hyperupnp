package app.vbt.hyperupnp

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

// RecyclerViewPlus is RecyclerView with Empty Size Checking Support
class RecyclerViewPlus : RecyclerView {
    var onEmpty: () -> Unit = {}
        set(value) {
            field = value
            checkIfEmpty()
        }

    var onNotEmpty: () -> Unit = {}
        set(value) {
            field = value
            checkIfEmpty()
        }

    var shouldHideOnEmpty = true
        set(value) {
            field = value
            checkIfEmpty()
        }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private fun checkIfEmpty() {
        if (adapter == null || adapter?.itemCount == 0) onEmpty() else onNotEmpty()
        if (shouldHideOnEmpty) this@RecyclerViewPlus.visibility =
            if (adapter == null || adapter!!.itemCount == 0) GONE else VISIBLE
    }

    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkIfEmpty()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            checkIfEmpty()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            checkIfEmpty()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            checkIfEmpty()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        this.adapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        this.adapter?.registerAdapterDataObserver(observer)
    }
}