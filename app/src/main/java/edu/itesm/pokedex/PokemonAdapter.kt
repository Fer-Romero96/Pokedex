package edu.itesm.pokedex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PokemonAdapter(private val data: MutableList<Pokemon>?) : RecyclerView.Adapter<PokemonAdapter.ViewHolder>()  {

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){

        fun bind(property: Pokemon){
            val title = view.findViewById<TextView>(R.id.tvTitle)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val description = view.findViewById<TextView>(R.id.tvDescription)

            title.text = property.nombre
            description.text = property.tipo

            Glide.with(view.context)
                .load(property.foto)
                .circleCrop()
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.pokemon_renglon, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return data!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data!![position])
    }

    fun deleteItem(index:Int){
        data?.removeAt(index)
        notifyDataSetChanged()
    }

    fun getItem(index: Int): Pokemon? {
        return data?.get(index)
    }

}