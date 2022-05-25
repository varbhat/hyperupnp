package app.vbt.hyperupnp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import app.vbt.hyperupnp.models.CustomListItem
import coil.load
import java.util.*


class CustomListAdapter(
    private var customListItems: ArrayList<CustomListItem>,
    private val onItemClick: (CustomListItem) -> Unit,
    private val onItemLongClick: (CustomListItem) -> Boolean,
) : RecyclerView.Adapter<CustomListAdapter.ViewHolder>(),
    Filterable {


    var customListFilterList = ArrayList<CustomListItem>()

    init {
        customListFilterList = customListItems
    }

    class ViewHolder(
        ItemView: View,
        onItemClick: (CustomListItem) -> Unit,
        onItemLongClick: (CustomListItem) -> Boolean
    ) :
        RecyclerView.ViewHolder(ItemView) {
        lateinit var entry: CustomListItem

        init {
            ItemView.setOnClickListener {
                onItemClick(entry)
            }
            ItemView.setOnLongClickListener {
                onItemLongClick(entry)
            }
        }

        var titleView: TextView = ItemView.findViewById(R.id.title)
        var containerView: RelativeLayout = ItemView.findViewById(R.id.container)
        var descriptionView: TextView = ItemView.findViewById(R.id.description)
        var description2View: TextView = ItemView.findViewById(R.id.description2)
        var imageView: ImageView = ItemView.findViewById(R.id.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hypergrids, parent, false)
        return ViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.isSelected = true
        holder.entry = customListFilterList[position]

        holder.imageView.load(holder.entry.iconUrl) {
            crossfade(true)
            placeholder(holder.entry.icon)
            error(holder.entry.icon)
        }

        holder.imageView.visibility = View.VISIBLE
        holder.containerView.setPadding(
            0,
            holder.containerView.paddingTop,
            holder.containerView.paddingRight,
            holder.containerView.paddingBottom
        )
        holder.titleView.text = holder.entry.title


        val description = holder.entry.description
        if (description == null) holder.descriptionView.visibility = View.GONE
        else {
            holder.descriptionView.visibility = View.VISIBLE
            holder.descriptionView.text = description
        }

        val description2 = holder.entry.description2
        if (description2 == null) holder.description2View.visibility = View.GONE
        else {
            holder.description2View.visibility = View.VISIBLE
            holder.description2View.text = description2
        }
    }

    override fun getItemCount(): Int = customListFilterList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                customListFilterList = if (charSearch.isEmpty()) {
                    customListItems
                } else {
                    val resultList = ArrayList<CustomListItem>()
                    for (row in customListItems) {
                        if (row.title.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = customListFilterList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                customListFilterList = results?.values as ArrayList<CustomListItem>
                notifyDataSetChanged()
            }
        }
    }

}