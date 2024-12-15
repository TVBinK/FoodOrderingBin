package com.example.foododering

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.ceil

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var shipperMarker: Marker
    private lateinit var customerMarker: Marker
    private val database = FirebaseDatabase.getInstance().reference
    private val shipperId = "shipper123"
    private val customerLocation = GeoPoint(20.9947593, 105.8104609)

    private lateinit var requestQueue: RequestQueue

    private var currentPolyline: Polyline? = null

    private lateinit var tvDistance: TextView
    private lateinit var tvEstimatedTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        Configuration.getInstance().userAgentValue = packageName

        mapView = findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        tvDistance = findViewById(R.id.tvDistance)
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime)

        requestQueue = Volley.newRequestQueue(this)

        shipperMarker = Marker(mapView)
        customerMarker = Marker(mapView)

        setupCustomerMarker()

        fetchShipperLocation()
    }

    private fun setupCustomerMarker() {
        customerMarker.position = customerLocation
        customerMarker.title = "Customer Location"
        customerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        val iconDrawable = getResizedDrawable(R.drawable.img_customer_location, 80, 80)
        if (iconDrawable != null) {
            customerMarker.icon = iconDrawable
        } else {
            customerMarker.icon = resources.getDrawable(R.drawable.img_customer_location, null)
        }

        mapView.overlays.add(customerMarker)
        mapView.controller.setCenter(customerLocation)
    }

    private fun fetchShipperLocation() {
        database.child("shippers").child(shipperId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").getValue(Double::class.java)
                val longitude = snapshot.child("longitude").getValue(Double::class.java)

                if (latitude != null && longitude != null) {
                    val shipperLocation = GeoPoint(latitude, longitude)
                    updateShipperMarker(shipperLocation)
                    zoomToLocation(shipperLocation)
                    requestRoute(shipperLocation, customerLocation)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapActivity, "Failed to fetch location: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateShipperMarker(location: GeoPoint) {
        shipperMarker.position = location
        shipperMarker.title = "Shipper's Current Location"

        val iconDrawable = getResizedDrawable(R.drawable.img_bike, 80, 80)

        if (iconDrawable != null) {
            shipperMarker.icon = iconDrawable
            shipperMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        } else {
            shipperMarker.icon = resources.getDrawable(R.drawable.img_bike, null)
            shipperMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        if (!mapView.overlays.contains(shipperMarker)) {
            mapView.overlays.add(shipperMarker)
        }

        mapView.controller.setCenter(location)
        mapView.invalidate()
    }

    private fun getResizedDrawable(resourceId: Int, width: Int, height: Int): Drawable? {
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        return bitmap?.let {
            val resizedBitmap = Bitmap.createScaledBitmap(it, width, height, true)
            BitmapDrawable(resources, resizedBitmap)
        }
    }

    private fun requestRoute(shipperLocation: GeoPoint, customerLocation: GeoPoint) {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${shipperLocation.longitude},${shipperLocation.latitude};" +
                "${customerLocation.longitude},${customerLocation.latitude}?overview=full&geometries=geojson"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val distance = route.getDouble("distance") / 1000 // Convert to kilometers
                    val duration = route.getDouble("duration") / 60 // Convert to minutes

                    // Hiển thị khoảng cách và thời gian dự kiến
                    tvDistance.text = String.format("%.2f km", distance)
                    tvEstimatedTime.text = String.format("~%d phút", ceil(duration).toInt())

                    val geometry = route.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    drawRoute(coordinates)
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Failed to fetch route", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun drawRoute(coordinates: org.json.JSONArray) {
        currentPolyline?.let {
            mapView.overlays.remove(it)
        }

        val routePoints = mutableListOf<GeoPoint>()
        for (i in 0 until coordinates.length()) {
            val point = coordinates.getJSONArray(i)
            val longitude = point.getDouble(0)
            val latitude = point.getDouble(1)
            routePoints.add(GeoPoint(latitude, longitude))
        }

        val polyline = Polyline()
        polyline.setPoints(routePoints)
        polyline.title = "Route"
        polyline.outlinePaint.color = resources.getColor(android.R.color.holo_blue_dark)
        polyline.outlinePaint.strokeWidth = 10f

        mapView.overlays.add(polyline)
        currentPolyline = polyline
        mapView.invalidate()
    }

    private fun zoomToLocation(location: GeoPoint) {
        mapView.controller.setCenter(location)
        mapView.controller.setZoom(18)
        mapView.invalidate()
    }
}
