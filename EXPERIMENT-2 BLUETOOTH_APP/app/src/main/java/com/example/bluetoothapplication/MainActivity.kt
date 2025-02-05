package com.example.bluetoothapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (bluetoothAdapter?.isEnabled == true) {
                Toast.makeText(this, "Bluetooth enabled successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to enable Bluetooth", Toast.LENGTH_SHORT).show()
            }
        }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                Toast.makeText(this, "File Selected: $uri", Toast.LENGTH_SHORT).show()
                sendFileViaBluetooth(uri)
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothFileTransferUI()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BluetoothFileTransferUI() {
        val isBluetoothEnabled = remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }

        // Generate colors for buttons dynamically
        val enableButtonColor = remember { generateLightColor() }
        val settingsButtonColor = remember { generateLightColor() }
        val fileButtonColor = remember { generateLightColor() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("BlueShare", color = Color.Black,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))  // Reduced the width to minimize space
                            Icon(
                                imageVector = Icons.Filled.Bluetooth,
                                contentDescription = "Bluetooth Icon",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier.background(Color.White)
                )

            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.Black)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StyledButton(text = "Enable Bluetooth", color = enableButtonColor) {
                        if (bluetoothAdapter == null) {
                            Toast.makeText(this@MainActivity, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
                        } else {
                            checkAndEnableBluetooth()
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    StyledButton(text = "Open Bluetooth Settings", color = settingsButtonColor) {
                        openBluetoothSettings()
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    StyledButton(text = "Select and Send File", color = fileButtonColor) {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }
                }
            }
        )
    }

    @Composable
    fun StyledButton(text: String, color: Color, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(0.95f) // Covers almost the entire width
                .height(75.dp) // Much bigger button
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(50.dp), // Fully rounded pill shape
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    private fun checkAndEnableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
            if (!requiredPermissions.all {
                    ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                }
            ) {
                ActivityCompat.requestPermissions(this, requiredPermissions, 1)
                return
            }
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intent)
    }

    private fun sendFileViaBluetooth(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = contentResolver.getType(uri)
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.android.bluetooth") // Force use of Bluetooth
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Bluetooth app not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateLightColor(): Color {
        val baseColors = listOf(
            Color(0xFFF5EACF), // Soft Beige
            Color(0xFFECE8E1), // Off-White Gray
            Color(0xFFFAF3DD), // Cream
            Color(0xFFE8E3D3), // Light Khaki
            Color(0xFFF8E5D1)  // Pale Peach
        )
        return baseColors.random()
    }
}
