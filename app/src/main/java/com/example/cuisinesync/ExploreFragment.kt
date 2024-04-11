package com.example.cuisinesync

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.yelp.com/v3/"

class ExploreFragment : androidx.fragment.app.Fragment(), OnMapReadyCallback{

    private val API_KEY = BuildConfig.YELP_API_KEY
    private lateinit var rvRestaurants: RecyclerView

    private var bottomSheetContainer: CoordinatorLayout? = null
    private var bottomSheetLayout: LinearLayout? = null

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    // Define an ActivityResultLauncher for the permission request
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //the lat-lang of Rhodes College Memphis, TN
    private val defaultLocation = LatLng(35.15571630877271, -89.98937221901873)
    private var locationPermissionGranted = false
    private var locationRequest: LocationRequest? = null

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLng: Array<LatLng?> = arrayOfNulls(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Places and FusedLocationProviderClient

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize the ActivityResultLauncher
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Update the permission state and UI accordingly
            locationPermissionGranted = isGranted
            if (isGranted) {
                updateLocationUI()
                getDeviceLocation()
            } else {
                // Handle the case where permission is denied
                updateLocationUI()
                // Consider guiding the user on how to grant the permission
            }
        }


    }

    private fun initBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout!!)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                adjustMapPaddingToBottomSheet()
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                adjustMapPaddingToBottomSheet()
            }
        })
    }

    private fun adjustMapPaddingToBottomSheet() {
        map?.let { map ->
            if (this.bottomSheetContainer != null && this.bottomSheetLayout != null) {
                val bottomSheetContainerHeight = this.bottomSheetContainer!!.height
                val currentBottomSheetTop = this.bottomSheetLayout!!.top
                map.setPadding(
                    0, // left
                    0, // top
                    0, // right
                    bottomSheetContainerHeight - currentBottomSheetTop // bottom
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupMenu()

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION, Location::class.java)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION, CameraPosition::class.java)
        }

        val mapsApiKey = BuildConfig.MAPS_API_KEY

        Places.initialize(requireActivity(), mapsApiKey )
        Places.createClient(requireActivity())

        // Initialize view
        val view: View = inflater.inflate(R.layout.fragment_explore, container, false)

        rvRestaurants = view.findViewById(R.id.rvRestaurants)

        // Initialize map fragment
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            map = googleMap
            // Once map is ready, check permissions and update UI accordingly
            if (locationPermissionGranted) {
                updateLocationUI()
                getDeviceLocation()
            } else {
                // Request location permission
                requestLocationPermission()
            }
        }

        bottomSheetLayout = view.findViewById(R.id.bottom_sheet)
        bottomSheetContainer = view.findViewById(R.id.root_coordinator_layout)


        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantAdapter(requireContext(), restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(requireContext())

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()

        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY","restaurant", "Memphis").enqueue(object : Callback<YelpSearchResult> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG,"onResponse $response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                    return
                }
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG,"onFailure $t")
            }
        })

        initBottomSheet()

        // Return view
        return view
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.current_place_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                if (menuItem.itemId == R.id.option_get_place) {
                    showCurrentPlace()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    //Sets up map and makes sure the camera moves to the last known location
    override fun onMapReady(p0: GoogleMap) {

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success: Boolean = p0.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(), R.raw.style_json
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        this.map = p0
        // [START_EXCLUDE]
        // [START map_current_place_set_info_window_adapter]
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.

        this.map?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            @SuppressLint("InflateParams")
            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title, and snippet.
                // Assuming you're within an Activity context; otherwise, use getContext() or requireContext() in a Fragment.
                val infoWindow = layoutInflater.inflate(R.layout.custom_info_contents,
                    null, false) // Pass null as the ViewGroup root because this view does not need to be attached.

                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet

                return infoWindow
            }
        })

        // [END map_current_place_set_info_window_adapter]

        // Prompt the user for permission.
        getLocationPermission()
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful && task.result != null) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false

                        requestNewLocationData()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(100)
            .build()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest!!, locationCallback, Looper.myLooper())
    }

    private val locationCallback = object : com.google.android.gms.location.LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            lastKnownLocation = locationResult.lastLocation
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(lastKnownLocation!!.latitude,
                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
            fusedLocationProviderClient.removeLocationUpdates(this)
        }
    }

    private fun getLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // [END maps_current_place_on_request_permissions_result]

    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (map == null) {
            return
        }
        if (locationPermissionGranted) {
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val request = FindCurrentPlaceRequest.newInstance(placeFields)
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result

                    // Set the count, handling cases where less than 5 entries are returned.
                    val count = if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
                        likelyPlaces.placeLikelihoods.size
                    } else {
                        M_MAX_ENTRIES
                    }
                    var i = 0
                    likelyPlaceNames = arrayOfNulls(count)
                    likelyPlaceAddresses = arrayOfNulls(count)
                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
                    likelyPlaceLatLng = arrayOfNulls(count)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        // Build a list of likely places to show the user.
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        likelyPlaceAddresses[i] = placeLikelihood.place.address
                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                        likelyPlaceLatLng[i] = placeLikelihood.place.latLng
                        i++
                        if (i > count - 1) {
                            break
                        }
                    }

                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    openPlacesDialog()
                } else {
                    Log.e(TAG, "Exception: %s", task.exception)
                }
            }
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.")

            // Add a default marker, because the user hasn't selected a place.
            map?.addMarker(MarkerOptions()
                .title(getString(R.string.default_info_title))
                .position(defaultLocation)
                .snippet(getString(R.string.default_info_snippet)))

            // Prompt the user for permission.
            getLocationPermission()
        }
    }

    private fun openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        val listener = DialogInterface.OnClickListener { _, which -> // The "which" argument contains the position of the selected item.
            val markerLatLng = likelyPlaceLatLng[which]
            var markerSnippet = likelyPlaceAddresses[which]
            if (likelyPlaceAttributions[which] != null) {
                markerSnippet = """
                $markerSnippet
                ${likelyPlaceAttributions[which]}
                """.trimIndent()
            }

            if (markerLatLng == null) {
                return@OnClickListener
            }

            // Add a marker for the selected place, with an info window
            // showing information about that place.
            map?.addMarker(MarkerOptions()
                .title(likelyPlaceNames[which])
                .position(markerLatLng)
                .snippet(markerSnippet))

            // Position the map's camera at the location of the marker.
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                DEFAULT_ZOOM.toFloat()))
        }

        // Display the dialog.
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaceNames, listener)
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {// [START maps_current_place_on_request_permissions_result]
        private val TAG = ExploreFragment::class.java.simpleName
        private const val DEFAULT_ZOOM = 15

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

}