package com.dimsseung.dailycheck.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.recyclerview.widget.RecyclerView
import com.dimsseung.dailycheck.R
import com.dimsseung.dailycheck.data.model.DailyLog



class LogAdapter(private val dataSet: List<DailyLog>, private val onItemClickListener: (DailyLog) -> Unit) :
    RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_item_title: TextView
        val tv_item_content: TextView
        val tv_item_date: TextView


        init {
            // Define click listener for the ViewHolder's View
            tv_item_title = view.findViewById(R.id.tv_item_title)
            tv_item_content = view.findViewById(R.id.tv_item_content)
            tv_item_date = view.findViewById(R.id.tv_item_date)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_log, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.tv_item_title.text = dataSet[position].title
        viewHolder.tv_item_content.text = dataSet[position].content
//        viewHolder.tv_item_date.text = dataSet[position].createdAt.toString()
        if (dataSet[position].createdAt != null) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            viewHolder.tv_item_date.text = formatter.format(dataSet[position].createdAt!!)
        } else {
            viewHolder.tv_item_date.text = "Baru saja..."
        }

        // Add onItemClick event to the item view
        viewHolder.itemView.setOnClickListener {
            onItemClickListener(dataSet[position])
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}