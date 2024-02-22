package com.example.cuisinesync.AlgorithmAPIDB

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Aggregates
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bson.Document

// Define data classes to represent Yelp API response
@JsonIgnoreProperties(ignoreUnknown = true)
data class YelpBusiness(val name: String, val rating: Double, val location: YelpLocation)

@JsonIgnoreProperties(ignoreUnknown = true)
data class YelpLocation(val address1: String, val city: String, val state: String, val country: String)

fun getMongoClient(): com.mongodb.client.MongoClient {
    return MongoClients.create("mongodb+srv://testtest123:20golf20@cluster0.xc5igiw.mongodb.net/?retryWrites=true&w=majority")
}

fun fetchAllUsers(collection: MongoCollection<Document>): List<Document> {
    // Define your aggregation pipeline stages here
    val pipeline = listOf(
        Aggregates.project(
            Document("_id", 0) // Exclude "_id" field from the output
                .append("name", 1) // Include "name" field in the output
                .append("preference1", 1) // Include "preference1" field in the output
                .append("preference2", 1) // Include "preference2" field in the output
                .append("preference3", 1) // Include "preference3" field in the output
                .append("preference4", 1) // Include "preference4" field in the output
                .append("preference5", 1) // Include "preference5" field in the output
        )
    )

    // Execute the aggregation pipeline
    val result = collection.aggregate(pipeline)

    // Convert the result to a list of documents
    val documents = mutableListOf<Document>()
    result.into(documents)

    return documents
}

// Function to fetch restaurant recommendations from Yelp API
fun fetchRestaurantRecommendations(cuisine: String, location: String): List<YelpBusiness> {
    val apiKey = "oSuHJbfpnFIWDhC3UbQK6K_evGapqZwsmFiihq_wgqR1r9Oj8kgGLSn3ThSmCf0Fy6bmzAIsAuvfiZ4LDDFmF9RpYI2RKOv8M89-IYKQOVScOLC56hTVgKWQJzrWZXYx"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.yelp.com/v3/businesses/search?term=$cuisine&location=$location")
        .header("Authorization", "Bearer $apiKey")
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()

    // Parse JSON response using Jackson library
    val mapper = jacksonObjectMapper()
    val yelpResponse: Map<String, Any> = mapper.readValue(responseBody ?: "")
    val businesses: List<Map<String, Any>> = yelpResponse["businesses"] as List<Map<String, Any>>

    // Map Yelp response to Kotlin data classes
    return businesses.map { business ->
        val name = business["name"] as String
        val rating = business["rating"] as Double
        val location = business["location"] as Map<String, Any>
        val address1 = location["address1"] as String
        val city = location["city"] as String
        val state = location["state"] as String
        val country = location["country"] as String
        YelpBusiness(name, rating, YelpLocation(address1, city, state, country))
    }
}

fun recommendation(memberToPreferences: Map<String, List<String>>) {
    val location = "Memphis"
    val tallyDict = mutableMapOf<String, Int>()
    val maxDict = mutableMapOf<String, Int>()

    // Count the occurrence of each cuisine preference
    for (preferences in memberToPreferences.values) {
        for (preference in preferences) {
            tallyDict[preference] = tallyDict.getOrDefault(preference, 0) + 1
        }
    }

    // Find the most common cuisine preference(s)
    val maxVal = tallyDict.values.maxOrNull()
    val commonCuisines = tallyDict.filterValues { it == maxVal }.keys.toList()

    // Print the most common cuisine(s)
    println("Most common cuisine(s) among users: ${commonCuisines.joinToString(", ")}")

    // Retrieve restaurant recommendations for each common cuisine
    for (cuisine in commonCuisines) {
        val recommendations = fetchRestaurantRecommendations(cuisine, location)
        println("Recommendations for $cuisine cuisine in $location:")
        recommendations.forEach { restaurant ->
            println("${restaurant.name}, Rating: ${restaurant.rating}, Address: ${restaurant.location.address1}, ${restaurant.location.city}, ${restaurant.location.state}, ${restaurant.location.country}")
        }
    }
}

fun main() {
    val client = getMongoClient()
    val database = client.getDatabase("users")
    val collection = database.getCollection("users")

    // Fetch all users with their preferences
    val users = fetchAllUsers(collection)

    // Map user names to their preferences
    val memberToPreferences = users.associate { user ->
        user.getString("name") to listOfNotNull(
            user.getString("preference1"),
            user.getString("preference2"),
            user.getString("preference3"),
            user.getString("preference4"),
            user.getString("preference5")
        )
    }

    // Provide restaurant recommendations based on common cuisine preferences
    recommendation(memberToPreferences)

    client.close()
}

