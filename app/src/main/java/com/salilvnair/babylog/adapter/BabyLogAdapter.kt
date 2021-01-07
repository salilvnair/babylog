package com.salilvnair.babylog.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.salilvnair.babylog.database.FireBaseDataHandler
import com.salilvnair.babylog.databinding.ActivityBabyFeedLogItemBinding
import com.salilvnair.babylog.model.BabyLogModel
import com.salilvnair.babylog.util.DateUtil

open class BabyLogAdapter (
    private val context: Context,
    private var list: ArrayList<BabyLogModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            ActivityBabyFeedLogItemBinding
                .inflate(LayoutInflater.from(context), parent, false)
                .root
        )
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        ActivityBabyFeedLogItemBinding.bind(holder.itemView).apply {
            tvLastFeedNum.text = "Feed #"+model.id.toString()
            tvLastFeedTime.text = DateUtil.formattedDateTime(model.lastFed)
            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    /**
     * An Interface to move from List activity to child.
     */
    interface OnClickListener {
        fun onClick(position: Int, model: BabyLogModel)
    }

    fun removeAt(position: Int) {
        val dbHandler = FireBaseDataHandler()
        dbHandler.remove(list[position])
        list.removeAt(position)
        notifyItemRemoved(position)
    }
}
// END