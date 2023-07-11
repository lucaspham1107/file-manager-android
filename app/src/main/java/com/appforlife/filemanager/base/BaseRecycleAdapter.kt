package com.appforlife.filemanager.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appforlife.filemanager.utils.handleException

abstract class BaseRecycleAdapter<T, R : BaseRecycleViewHolder>(var listItem: MutableList<T>) :
    RecyclerView.Adapter<R>() {
    override fun getItemCount(): Int = listItem.size

    @SuppressLint("NotifyDataSetChanged")
    open fun setList(newList: List<T>) {
        listItem.clear()
        listItem.addAll(newList)
        notifyDataSetChanged()
    }

    var onItemClick: ((position: Int, item: T) -> Unit)? = null
    open var allowForItemClick = true
    var isDisabled = false

    abstract fun getLayoutResourceId(): Int
    abstract fun onCreateViewHolder(view: View): R

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): R {
        val view = LayoutInflater.from(parent.context).inflate(getLayoutResourceId(), parent, false)
        return onCreateViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutResourceId()
    }

    final override fun onBindViewHolder(holder: R, position: Int) {
        if (allowForItemClick) {
            onItemClick?.let { callback ->
                holder.itemView.setOnClickListener {
                    if (!isDisabled) {
                        callback.invoke(
                            position, listItem[position]
                        )
                    }
                }
            }
        }
        try {
            onBindView(holder, position, listItem[position])
        } catch (e: Exception) {
            handleException(e)
        }
    }

    abstract fun onBindView(holder: R, position: Int, item: T)

    fun setRecycleView(view: RecyclerView, layoutOrientation: Int = RecyclerView.VERTICAL) {
        view.layoutManager =
            LinearLayoutManager(view.context, layoutOrientation, false)
        view.adapter = this
    }
}

abstract class BaseRecycleViewHolder(view: View) : RecyclerView.ViewHolder(view)