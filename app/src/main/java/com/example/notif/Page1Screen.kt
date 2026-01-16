package com.example.notif

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Page1Screen(
    onNavigateBack: () -> Unit
) {
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
                text = "Halaman 1",
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Text(
                text = "Anda datang dari notifikasi Halaman 1!",
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text("Kembali ke Home")
            }
        }
    }
}
