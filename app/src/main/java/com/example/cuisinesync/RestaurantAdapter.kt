package com.example.cuisinesync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisinesync.databinding.ItemRestaurantBinding


class RestaurantAdapter(val context: Context, private val restaurants: List<YelpRestaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false))
    }

    override fun getItemCount()= restaurants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.bind(restaurant)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemRestaurantBinding.bind(itemView)

        fun bind(restaurant: YelpRestaurant){
            binding.tvName.text = restaurant.name

        }
    }

}
