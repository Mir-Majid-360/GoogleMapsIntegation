package edu.cs371m.fcgooglemaps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.cs371m.fcgooglemaps.databinding.ActivityMainBinding
import edu.cs371m.fcgooglemaps.databinding.ContentMainBinding
import kotlinx.android.synthetic.main.content_main.*
import java.text.DecimalFormat
import java.util.*


/**
 * AIzaSyBzXgr3w6i1sz-oSuTqXXQdxHFoAtb2kd8
 * **/

class MainActivity : AppCompatActivity() ,OnMapReadyCallback,GoogleMap.OnMarkerClickListener{

    val position = LatLng(30.28433304950023, -97.74122007354248)

    var markerOptions = MarkerOptions().position(position)

    lateinit var marker : Marker
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private var locationPermissionGranted = false
    private lateinit var binding: ContentMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        binding = activityMainBinding.contentMain


        geocoder = Geocoder(this)

        checkGooglePlayServices()
        requestPermission()

        binding.mapET.setOnEditorActionListener { /*v*/_, actionId, event ->
            // If user has pressed enter, or if they hit the soft keyboard "send" button
            // (which sends DONE because of the XML)
            if ((event != null
                        && (event.action == KeyEvent.ACTION_DOWN)
                        && (event.keyCode == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)
            ) {
                hideKeyboard()
                binding.goBut.callOnClick()
            }
            false
        }

        goBut.setOnClickListener {

            searchLocation()
        }



        with(mapView) {
            // Initialise the MapView
            onCreate(null)
            // Set the map ready callback to receive the GoogleMap object
            getMapAsync {
                MapsInitializer.initialize(applicationContext)
                setMapLocation(it)
            }
        }

        findMyLocation.setOnClickListener {

            getmyCurrentLocation()

        }

        icClear.setOnClickListener {
            map.clear()
        }




    }

    private fun getmyCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
           var gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
           var  network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)


            var  latLng = LatLng(network_loc?.latitude!! ,network_loc?.longitude!!);
            map.addMarker( MarkerOptions ().position(latLng));
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));




        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


    }


    private fun setMapLocation(map : GoogleMap) {
        this.map=map
        with(map) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f))
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnMapClickListener {
//                if(::marker.isInitialized){
//                    marker.remove()
//                }
              //  markerOptions.position(it)
//DecimalFormat df = new DecimalFormat("#.00"); float number = Float.valueOf(df.format(decimal));
//
//Read more: https://www.java67.com/2020/04/4-examples-to-round-floating-point-numbers-in-java.html#ixzz7jx5d1TTR

                var markerOptions = MarkerOptions().position(it)
                var marker = addMarker(markerOptions)
                marker.title = " ${getFormatUptoThree(it.latitude)},${getFormatUptoThree(it.longitude)}"




            }

        }

        map.setOnMapLongClickListener {

                map.clear()

        }
    }

    fun getFormatUptoThree(amount: Double?): String? {
        var value = "0.0"
        val decimalFormat = DecimalFormat("#.###")
        decimalFormat.setGroupingUsed(true)
        decimalFormat.setGroupingSize(3)
        try {
            value = decimalFormat.format(amount)
            if (!value.contains(".")) {
                value = "$value.000"
            }
        } catch (e: java.lang.Exception) {
            return value
        }
        return value
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if( locationPermissionGranted ) {
            // XXX Write me.
            // Note, we checked location permissions in requestPermission, but the compiler
            // might complain about our not checking it.
        }

        // XXX Write me.
        // Start the map at the Harry Ransom center
    }

    // Everything below here is correct

    // An Android nightmare
    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0);
    }

    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode =
            googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 257)?.show()
            } else {
                Log.i(javaClass.simpleName,
                    "This device must install Google Play Services.")
                finish()
            }
        }
    }

    private fun requestPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    locationPermissionGranted = true;
                    findMyLocation.visibility = View.VISIBLE

                } else -> {
                Toast.makeText(this,
                    "Unable to show location - permission required",
                    Toast.LENGTH_LONG).show()
            }
            }
        }
        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

  fun  searchLocation()
    {

        var location: String =mapET.text.toString()


        if (location != null || !location.equals("")) {

            try {
               var addressList = geocoder.getFromLocationName(location, 1);


            var address :Address= addressList?.get(0)!!
            var  latLng = LatLng(address.getLatitude(), address.getLongitude());
            map.addMarker( MarkerOptions ().position(latLng).title(location))

            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))

            } catch ( e: Exception) {

            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        marker.showInfoWindow()
     return true
    }


//"${position.latitude.toString().substring(1,3)} ${position.longitude.toString().substring(1,3)}"


}
