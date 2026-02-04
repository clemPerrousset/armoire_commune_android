package fr.larmoirecommune.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ItemDashboardBinding

data class DashboardItem(val title: String, val iconRes: Int, val action: () -> Unit)

class DashboardAdapter(private val items: List<DashboardItem>) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDashboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.itemTitle.text = item.title
        holder.binding.itemIcon.setImageResource(item.iconRes)
        holder.itemView.setOnClickListener { item.action() }
    }

    override fun getItemCount() = items.size
}
