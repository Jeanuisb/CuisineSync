package com.example.cuisinesync

import YelpRepository
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume

// Define your User class that extends RealmObject and implements TypedRealmObject
// Define your User class that extends RealmObject and implements TypedRealmObject
// Define your User class that extends RealmObject
open class User() : RealmObject {
    @PrimaryKey var _id: ObjectId? = null
    var name: String? = null
    var preference1: String? = null
    var preference2: String? = null
    var preference3: String? = null
    var preference4: String? = null
    var preference5: String? = null

    constructor(
        _id: ObjectId = BsonObjectId(),
        name: String? = null,
        preference1: String? = null,
        preference2: String? = null,
        preference3: String? = null,
        preference4: String? = null,
        preference5: String? = null
    ) : this() {
        this._id = _id
        this.name = name
        this.preference1 = preference1
        this.preference2 = preference2
        this.preference3 = preference3
        this.preference4 = preference4
        this.preference5 = preference5
    }
}

class EatFragment : Fragment() {
    private lateinit var yelpRepository: YelpRepository
    private lateinit var realm: Realm
    private var previousUsernames: List<String>? = null
    private var previousRestaurant: YelpRestaurant? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_eat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Realm with the correct configuration
        realm = Realm.open(RealmConfiguration.create(schema = setOf(User::class)))



        val apiKey = BuildConfig.YELP_API_KEY
        yelpRepository = YelpRepository(apiKey)

        val updateButton: Button = view.findViewById(R.id.update_button)
        val displayText: TextView = view.findViewById(R.id.display_text)
        val usernameEditText0: EditText = view.findViewById(R.id.username0)
        val usernameEditText1: EditText = view.findViewById(R.id.username1)
        val usernameEditText2: EditText = view.findViewById(R.id.username2)
        val usernameEditText3: EditText = view.findViewById(R.id.username3)

        previousUsernames = emptyList() // Initialize with an empty list


        updateButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val usernames = listOf(
                    usernameEditText0.text.toString().trim(),
                    usernameEditText1.text.toString().trim(),
                    usernameEditText2.text.toString().trim(),
                    usernameEditText3.text.toString().trim()
                )

                if (usernames.all { it.isEmpty() }) {
                    // If all usernames are empty, prompt the user to enter usernames
                    withContext(Dispatchers.Main) {
                        Snackbar.make(view, "Please enter at least one username", Snackbar.LENGTH_LONG).setAction("Close") {}.show()
                    }
                    return@launch
                }


                if (usernames != previousUsernames) {
                    val users = getUsersByNames(usernames)
                    val userPreferences = getUserPreferences(users)
                    val commonCuisine = determineCommonCuisine(userPreferences)

                    val recommendations = fetchRecommendations(commonCuisine, "Memphis")

                    withContext(Dispatchers.Main) {
                        displayText.text = formatRecommendations(recommendations)
                    }

                    previousUsernames = usernames
                    previousRestaurant = null // Reset the previousRestaurant
                } else {
                    withContext(Dispatchers.Main) {
                        if (previousRestaurant != null) {
                            displayText.text = "${previousRestaurant?.name}, Rating: ${previousRestaurant?.rating}, Address: ${previousRestaurant?.location?.address}, ${previousRestaurant?.categories?.joinToString { category -> category.title }}, Distance: ${previousRestaurant?.displayDistance()}, Price: ${previousRestaurant?.price}"
                        } else {
                            displayText.text = "You have just tried this combination"
                        }
                    }
                }

                //displayUserIDs(view)

            }
        }
    }



    // Function to fetch user IDs and display them in a Snackbar
    private fun displayUserIDs(view: View) {
        val userIds = realm.query(User::class).find().mapNotNull { it._id?.toString() }
        val userIdsText = userIds.joinToString(", ")

        CoroutineScope(Dispatchers.Main).launch {
            Snackbar.make(view, "User IDs: $userIdsText", Snackbar.LENGTH_LONG).setAction("Close") {}.show()
        }
    }


    private fun getUsersByNames(usernames: List<String>): List<User> {
        val users = mutableListOf<User>()
        val notFoundUsers = mutableListOf<String>()
        for (username in usernames) {
            val user = realm.query(User::class, "name = $0", username).find().firstOrNull()
            if (user != null) {
                users.add(user)
            } else {
                notFoundUsers.add(username)
            }
        }

        notFoundUsers.forEachIndexed { index, username ->
            Log.d("User not found", "'User${index + 1}' $username is not a user")
        }

        return users
    }

    private fun getUserPreferences(users: List<User>): Map<String, Map<String, Int>> {
        return users.associate { user ->
            (user.name ?: "Unknown") to mapOf(
                (user.preference1 ?: "Unknown") to 5,
                (user.preference2 ?: "Unknown") to 4,
                (user.preference3 ?: "Unknown") to 3,
                (user.preference4 ?: "Unknown") to 2,
                (user.preference5 ?: "Unknown") to 1
            ).filterValues { true }
        }
    }

    private fun determineCommonCuisine(preferences: Map<String, Map<String, Int>>): String {
        val weightedPreferences = preferences.values.flatMap { it.entries }.groupBy({ it.key }, { it.value })
        Log.d("Weighted Preferences", "Weighted Preferences:")
        weightedPreferences.forEach { (preference, weights) ->
            Log.d("Weighted Preferences", "$preference: ${weights.sum()}")
        }
        val highestWeightedPreference = weightedPreferences.maxByOrNull { it.value.sum() }

        return if (highestWeightedPreference != null) {
            highestWeightedPreference.key
        } else {
            // If no highest weighted preference is found, select a random cuisine
            val allPreferences = preferences.values.flatMap { it.keys }
            if (allPreferences.isNotEmpty()) {
                allPreferences.random()
            } else {
                // If no preferences are available, return a default cuisine
                "Default Cuisine"
            }
        }
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
        val highRatedRestaurants = recommendations.filter { it.rating >= 4.0 }
        return if (highRatedRestaurants.isNotEmpty()) {
            val randomRestaurant = highRatedRestaurants.random()
            previousRestaurant = randomRestaurant
            "${randomRestaurant.name}, Rating: ${randomRestaurant.rating}, Address: ${randomRestaurant.location.address}, ${randomRestaurant.categories.joinToString { category -> category.title }}, Distance: ${randomRestaurant.displayDistance()}, Price: ${randomRestaurant.price}"
        } else {
            "No recommendations available with rating above 4"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        realm.close()
    }
}