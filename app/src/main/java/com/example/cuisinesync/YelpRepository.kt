import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.example.cuisinesync.YelpSearchResult
import com.example.cuisinesync.YelpService

class YelpRepository(private val apiKey: String) {
    private val baseUrl = "https://api.yelp.com/v3/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val yelpService: YelpService = retrofit.create(YelpService::class.java)

    fun searchRestaurants(term: String, location: String, callback: Callback<YelpSearchResult>) {
        yelpService.searchRestaurants("Bearer $apiKey", term, location).enqueue(object : Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i("YelpRepository", "onResponse: $response")
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.e("YelpRepository", "onFailure: $t")
                callback.onFailure(call, t)
            }
        })
    }
}
