package com.example.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notif.ui.theme.NotifTheme
import java.util.concurrent.TimeUnit

// Constants for navigation and notifications
const val EXTRA_PAGE = "page"
const val PAGE_HOME = "home"
const val PAGE_1 = "page1"
const val PAGE_2 = "page2"
const val NOTIFICATION_ID_1 = 1
const val NOTIFICATION_ID_2 = 2

class MainActivity : ComponentActivity() {
    private val channelId = "test_notification_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        setContent {
            NotifTheme {
                AppNavigation(
                    startDestination = getStartDestination()
                )
            }
        }
    }

    private fun getStartDestination(): String {
        val page = intent.getStringExtra(EXTRA_PAGE)
        return when (page) {
            PAGE_1 -> PAGE_1
            PAGE_2 -> PAGE_2
            else -> PAGE_HOME
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Test Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel untuk testing notifikasi"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(PAGE_HOME) {
            NotificationScreen(
                channelId = "test_notification_channel",
                onNavigateToPage1 = { navController.navigate(PAGE_1) },
                onNavigateToPage2 = { navController.navigate(PAGE_2) }
            )
        }
        composable(PAGE_1) {
            Page1Screen(
                onNavigateBack = { navController.popBackStack(PAGE_HOME, inclusive = false) }
            )
        }
        composable(PAGE_2) {
            Page2Screen(
                onNavigateBack = { navController.popBackStack(PAGE_HOME, inclusive = false) }
            )
        }
    }
}

@Composable
fun NotificationScreen(
    channelId: String,
    onNavigateToPage1: () -> Unit,
    onNavigateToPage2: () -> Unit
) {
    val context = LocalContext.current

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingAction?.invoke()
        }
        pendingAction = null
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Test Notification",
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                showNotificationToPage1(context, channelId)
                            }
                            else -> {
                                pendingAction = { showNotificationToPage1(context, channelId) }
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        showNotificationToPage1(context, channelId)
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Notifikasi ke Halaman 1")
            }

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                showNotificationToPage2(context, channelId)
                            }
                            else -> {
                                pendingAction = { showNotificationToPage2(context, channelId) }
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        showNotificationToPage2(context, channelId)
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Notifikasi ke Halaman 2")
            }

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                scheduleDelayedNotification(context)
                            }
                            else -> {
                                pendingAction = { scheduleDelayedNotification(context) }
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        scheduleDelayedNotification(context)
                    }
                }
            ) {
                Text("Notifikasi Setelah 10 Detik")
            }
        }
    }
}

private fun showNotificationToPage1(context: Context, channelId: String) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_PAGE, PAGE_1)
    }

    val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        pendingIntentFlags
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Notifikasi ke Halaman 1")
        .setContentText("Klik untuk membuka Halaman 1")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    ) {
        notificationManager.notify(NOTIFICATION_ID_1, notification)
    }
}

private fun showNotificationToPage2(context: Context, channelId: String) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_PAGE, PAGE_2)
    }

    val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        1,
        intent,
        pendingIntentFlags
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Notifikasi ke Halaman 2")
        .setContentText("Klik untuk membuka Halaman 2")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    ) {
        notificationManager.notify(NOTIFICATION_ID_2, notification)
    }
}

private fun scheduleDelayedNotification(context: Context) {
    val workRequest = OneTimeWorkRequestBuilder<DelayedNotificationWorker>()
        .setInitialDelay(10, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}
