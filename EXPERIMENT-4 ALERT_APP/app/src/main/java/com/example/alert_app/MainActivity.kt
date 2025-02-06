package com.example.alert_app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme()
        }

        // Create Notification Channel (for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel", // Channel ID
                "Default Channel", // Channel name
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun AppTheme() {
    var isDarkTheme by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        AlertAppScreen(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = !isDarkTheme }
        )
    }
}

@Composable
fun AlertAppScreen(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    var messages by remember { mutableStateOf(listOf<String>()) }
    var newMessage by remember { mutableStateOf("") }

    val context = LocalContext.current // Get the context

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else Color.White) // Set background based on theme
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header with Theme Toggle Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Alert App",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground // Ensure visibility in both themes
                ),
                modifier = Modifier.weight(1f)
            )

            // Theme Toggle Icon Button
            IconButton(onClick = onThemeToggle) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = if (isDarkTheme) Color.White else Color.Black // Change icon color based on theme
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Message Input Field
        TextField(
            value = newMessage,
            onValueChange = { newMessage = it },
            label = { Text("Enter your message") },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isDarkTheme) Color.Gray else Color.White,
                    shape = MaterialTheme.shapes.small // Apply rounded corners using the theme's shape
                )
                .border(1.dp, color = if (isDarkTheme) Color.White else Color.Black, shape = MaterialTheme.shapes.small) // Border with rounded corners
                .padding(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (isDarkTheme) Color.White else Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Send Button
        Button(
            onClick = {
                if (newMessage.isNotBlank()) {
                    messages = messages + newMessage
                    sendNotification(context, newMessage) // Send notification when a message is sent
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "FIRE")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message History
        MessageHistory(messages)

        Spacer(modifier = Modifier.height(16.dp))

        // Clear Messages Button
        Button(onClick = { messages = emptyList() }, modifier = Modifier.fillMaxWidth()) {
            Text("Clear All Messages")
        }
    }
}

@Composable
fun MessageHistory(messages: List<String>) {
    LazyColumn {
        items(messages) { message ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun sendNotification(context: Context, message: String) {
    val notificationManager = NotificationManagerCompat.from(context)

    // Create a unique notification ID (using a timestamp or an incremented number)
    val notificationId = System.currentTimeMillis().toInt() // Use the current time as a unique ID

    // Create the notification
    val notification = NotificationCompat.Builder(context, "default_channel")
        .setSmallIcon(androidx.core.R.drawable.notification_icon_background) // Change icon as needed
        .setContentTitle("New Message")
        .setContentText("You sent a message: $message")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setGroup("message_group") // Group all message notifications together
        .build()

    // Notify with the unique ID, this will keep the previous notifications visible
    notificationManager.notify(notificationId, notification)

    // Optionally, you can create a summary notification for the group
    val summaryNotification = NotificationCompat.Builder(context, "default_channel")
        .setContentTitle("Messages")
        .setContentText("You have new messages")
        .setSmallIcon(androidx.core.R.drawable.notification_icon_background) // Change icon as needed
        .setGroup("message_group")
        .setGroupSummary(true) // Indicates it's a summary of the group
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    // Notify the summary notification, which will show the group of messages
    notificationManager.notify(0, summaryNotification) // 0 for the summary notification
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme()
}
