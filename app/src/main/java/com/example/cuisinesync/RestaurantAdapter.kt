package com.example.cuisinesync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class RestaurantAdapter(val context: Context, private val restaurants: List<YelpRestaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false))
    }

    override fun getItemCount() = restaurants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.bind(restaurant)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvName)
        private val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
        private val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)
        private val tvNumReviews = itemView.findViewById<TextView>(R.id.tvNumReviews)
        private val tvCategory = itemView.findViewById<TextView>(R.id.tvCategory)
        private val tvDistance = itemView.findViewById<TextView>(R.id.tvDistance)
        private val tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)
        private val imageView = itemView.findViewById<android.widget.ImageView>(R.id.imageView)

        fun bind(restaurant: YelpRestaurant){
            tvName.text = restaurant.name
            ratingBar.rating = restaurant.rating.toFloat()
            tvAddress.text = restaurant.location.address
            tvNumReviews.text = "${restaurant.numReviews} Reviews"
            tvCategory.text = restaurant.categories[0].title
            tvDistance.text = restaurant.displayDistance()
            tvPrice.text = restaurant.price
            Glide.with(context).load(restaurant.imageUrl).apply(RequestOptions().transform(
                CenterCrop(),
                RoundedCorners(20)
            )
            ).into(imageView)

        }
    }

}
