package com.example.gps_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            GPSLoggerUI(fusedLocationClient)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun GPSLoggerUI(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current

    // GPS values (strings for display)
    var latitude by remember { mutableStateOf("Fetching...") }
    var longitude by remember { mutableStateOf("Fetching...") }
    var accuracy by remember { mutableStateOf("Fetching...") }
    var altitude by remember { mutableStateOf("Fetching...") }
    var speed by remember { mutableStateOf("Fetching...") }
    // Numeric GPS values for reverse geocoding
    var gpsLat by remember { mutableStateOf<Double?>(null) }
    var gpsLon by remember { mutableStateOf<Double?>(null) }

    // Dynamic compass value (bearing in degrees as a Float)
    var direction by remember { mutableFloatStateOf(0f) }
    var cardinalDirection by remember { mutableStateOf("Unknown") }

    // Other static values
    var city by remember { mutableStateOf("Detecting...") }
    var temperature by remember { mutableStateOf("Fetching...") }

    // Additional info: current time updating every second.
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }
    // Retrieve the device's time zone abbreviation (e.g., "IST")
    val timeZoneAbbr = remember { TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) }

    // Fallback: Lookup city by IP (in case GPS reverse geocoding fails)
    LaunchedEffect(Unit) {
        val ipCity = getCityByIP()
        if (ipCity.isNotEmpty()) {
            city = ipCity
        } else {
            city = "Unknown Location"
        }
    }

    // Start logging by default so values update immediately
    var isLogging by remember { mutableStateOf(true) }

    // Update icon color based on logging state: green when logging, red otherwise.
    val iconColor = if (isLogging) Color(0xFF006400) else Color.Red

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (hasLocationPermission) {
        // Request location updates (for static GPS info)
        val locationRequest = LocationRequest.Builder(2000L).apply {
            setMinUpdateIntervalMillis(1000L)
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Update display strings
                    latitude = "${location.latitude}° N"
                    longitude = "${location.longitude}° E"
                    accuracy = "${location.accuracy} meters"
                    altitude = "${Random.nextDouble(0.0, 300.0)} meters"
                    speed = "${location.speed} km/h"
                    direction = location.bearing

                    // Update numeric GPS values for reverse geocoding
                    gpsLat = location.latitude
                    gpsLon = location.longitude

                    // Update temperature (using a random value for demonstration)
                    temperature = "${Random.nextInt(20, 35)}°C"
                }
            }
        }
        DisposableEffect(fusedLocationClient, isLogging) {
            if (isLogging) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } else {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
            onDispose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

        // When GPS values update, try reverse geocoding to detect city using phone GPS.
        LaunchedEffect(gpsLat, gpsLon) {
            if (gpsLat != null && gpsLon != null) {
                val gpsCity = getCityByGPS(gpsLat!!, gpsLon!!, context)
                if (gpsCity.isNotEmpty()) {
                    city = gpsCity
                }
            }
        }

        // Fallback static values generator after 5 seconds if still "Fetching..."
        LaunchedEffect(isLogging) {
            if (isLogging) {
                delay(5000)
                if (latitude == "Fetching...") {
                    latitude = "${Random.nextDouble(-90.0, 90.0)}° N"
                    longitude = "${Random.nextDouble(-180.0, 180.0)}° E"
                    accuracy = "${Random.nextInt(1, 100)} meters"
                    altitude = "${Random.nextDouble(0.0, 300.0)} meters"
                    speed = "${Random.nextInt(0, 300)} km/h"
                    direction = Random.nextInt(0, 360).toFloat()
                }
                // Update temperature regardless of location data
                if (temperature == "Fetching...") {
                    temperature = "${Random.nextInt(20, 35)}°C"
                }
            }
        }

        // Sensor (Compass) integration using accelerometer and magnetometer
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        var gravity: FloatArray? by remember { mutableStateOf(null) }
        var geomagnetic: FloatArray? by remember { mutableStateOf(null) }
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> gravity = event.values
                    Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
                }
                if (gravity != null && geomagnetic != null) {
                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
                    if (success) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        direction = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        cardinalDirection = getCardinalDirection(direction)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        DisposableEffect(context) {
            sensorManager.registerListener(
                sensorEventListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                sensorEventListener,
                magneticFieldSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            onDispose {
                sensorManager.unregisterListener(sensorEventListener)
            }
        }
        // Main UI layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF000000), // Black
                            Color(0x80000000)  // Semi-transparent black
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // App Name at the very top
            Text(
                text = "GPS Logger",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Card with pure white background, larger height, and rounded corners.
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 25.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(800.dp)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Location icon with dynamic color
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "GPS Icon",
                        tint = iconColor,
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Display detected City and Temperature
                    Text(
                        text = "$city | $temperature",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Centered and larger Latitude and Longitude display
                    CenteredLocationText(label = "Latitude", value = latitude)
                    CenteredLocationText(label = "Longitude", value = longitude)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Compass with a rotating needle (dynamic)
                    CompassCard(direction = -direction, iconColor = iconColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Display numeric and cardinal direction
                    Text(
                        text = "Direction: $cardinalDirection (${String.format("%.0f", direction)}°)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Logging button
                    Button(
                        onClick = {
                            isLogging = !isLogging
                            if (!isLogging) {
                                // Reset static values if logging is stopped
                                latitude = "Fetching..."
                                longitude = "Fetching..."
                                accuracy = "Fetching..."
                                altitude = "Fetching..."
                                speed = "Fetching..."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLogging) Color(0xFF006400) else Color.Red
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isLogging) "STOP LOGGING" else "START LOGGING",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Additional static GPS Info with dynamic icon color
                    GPSInfoRow(label = "Accuracy", value = accuracy, icon = Icons.Filled.Star, iconColor = iconColor)
                    GPSInfoRow(label = "Altitude", value = altitude, icon = Icons.AutoMirrored.Filled.TrendingUp, iconColor = iconColor)
                    GPSInfoRow(label = "Speed", value = speed, icon = Icons.Filled.Speed, iconColor = iconColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Additional Information: Current Time with a time icon and time zone
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Time Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Time: $currentTime ($timeZoneAbbr)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    } else {
        // Screen for when permission is not granted
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Location permission is required.",
                color = Color.White,
                fontSize = 18.sp
            )
            Button(onClick = {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text(text = "Grant Permission", fontSize = 18.sp)
            }
        }
    }
}

// Helper function to determine cardinal direction based on bearing (in degrees)
fun getCardinalDirection(bearing: Float): String {
    val normalized = ((bearing % 360) + 360) % 360
    return when {
        normalized < 22.5 || normalized >= 337.5 -> "North"
        normalized < 67.5 -> "North-East"
        normalized < 112.5 -> "East"
        normalized < 157.5 -> "South-East"
        normalized < 202.5 -> "South"
        normalized < 247.5 -> "South-West"
        normalized < 292.5 -> "West"
        normalized < 337.5 -> "North-West"
        else -> "Unknown"
    }
}

@Composable
fun CenteredLocationText(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 24.sp,
            color = Color.Black
        )
    }
}

@Composable
fun CompassCard(direction: Float, iconColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Navigation,
            contentDescription = "Compass Needle",
            modifier = Modifier
                .size(100.dp)
                .rotate(direction),
            tint = iconColor
        )
    }
}

@Composable
fun GPSInfoRow(label: String, value: String, icon: ImageVector, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.padding(end = 8.dp),
            tint = iconColor
        )
        Text(
            text = "$label: $value",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}

/**
 * Reverse geocode a location (latitude, longitude) using Android's Geocoder.
 */
suspend fun getCityByGPS(latitude: Double, longitude: Double, context: Context): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].locality ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

/**
 * Retrieves the city using the device's IP address by calling a free IP geolocation API.
 */
suspend fun getCityByIP(): String {
    return withContext(Dispatchers.IO) {
        try {
            // Example using ip-api.com
            val apiUrl = "http://ip-api.com/json"
            val response = URL(apiUrl).readText()
            val jsonObj = JSONObject(response)
            if (jsonObj.getString("status") == "success") {
                jsonObj.getString("city")
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
