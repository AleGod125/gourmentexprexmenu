package com.example.gourmentexprexmenu.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gourmentexprexmenu.R

class bebidasAdapter(private val proteinasList: List<String>) :
    RecyclerView.Adapter<bebidasAdapter.ProteinaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProteinaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_proteinas, parent, false)
        return ProteinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProteinaViewHolder, position: Int) {
        val proteina = proteinasList[position]
        holder.bind(proteina)
    }

    override fun getItemCount(): Int {
        return proteinasList.size
    }

    inner class ProteinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textProteina)

        fun bind(proteina: String) {
            textView.text = proteina
        }
    }
}