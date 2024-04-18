package com.example.cuisinesync

import YelpRepository
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume

class EatFragment : Fragment(){
    private lateinit var yelpRepository: YelpRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_eat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = BuildConfig.YELP_API_KEY
        yelpRepository = YelpRepository(apiKey)

        val updateButton: Button = view.findViewById(R.id.update_button)
        val displayText: TextView = view.findViewById(R.id.display_text)
        val usernameEditText0: EditText = view.findViewById(R.id.username0)
        val usernameEditText1: EditText = view.findViewById(R.id.username1)

        updateButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val user1 = usernameEditText0.text.toString().trim()
                val user2 = usernameEditText1.text.toString().trim()

                val userPreferences = getUserPreferences(user1, user2)
                val commonCuisine = determineCommonCuisine(userPreferences)

                val recommendations = fetchRecommendations(commonCuisine, "Memphis")

                withContext(Dispatchers.Main) {
                    displayText.text = formatRecommendations(recommendations)
                }
            }
        }
    }

    private fun getUserPreferences(user1: String, user2: String): Map<String, List<String>> {
        return mapOf(
            user1 to listOf("Ethiopian", "Somali"),
            user2 to listOf("Italian", "Mexican")
        )
    }

    private fun determineCommonCuisine(preferences: Map<String, List<String>>): String {
        val cuisineCount = preferences.values.flatten().groupingBy { it }.eachCount()
        return cuisineCount.maxByOrNull { it.value }?.key ?: "Chinese"
    }

    private suspend fun fetchRecommendations(cuisine: String, location: String): List<YelpRestaurant> {
        return suspendCancellableCoroutine { continuation ->
            yelpRepository.searchRestaurants(cuisine, location, object :
                Callback<YelpSearchResult> {
                override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                    if (response.isSuccessful) {
                        response.body()?.let { searchResult ->
                            continuation.resume(searchResult.restaurants)
                        } ?: continuation.resume(emptyList())
                    } else {
                        continuation.resume(emptyList())
                    }
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    continuation.resume(emptyList())
                }
            })
        }
    }

    private fun formatRecommendations(recommendations: List<YelpRestaurant>): String {
        val highestRatedRestaurant = recommendations.maxByOrNull { it.rating }
        return highestRatedRestaurant?.let {
            "${it.name}, Rating: ${it.rating}, Address: ${it.location.address}, ${it.categories.joinToString { category -> category.title }}, Distance: ${it.displayDistance()}, Price: ${it.price}"
        } ?: "No recommendations available"
    }


}
