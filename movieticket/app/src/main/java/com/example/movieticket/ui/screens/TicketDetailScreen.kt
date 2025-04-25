package com.example.movieticket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movieticket.R
import com.example.movieticket.data.model.Ticket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticket: Ticket,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết vé") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1B1E25))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Movie Info Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Movie Poster
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${ticket.moviePoster}",
                    contentDescription = ticket.movieTitle,
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Movie Details
                Column {
                    Text(
                        text = ticket.movieTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (ticket.status == "active") "Còn hiệu lực" else "Đã sử dụng",
                        color = if (ticket.status == "active") Color(0xFF4CAF50) else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Ticket Info
            InfoRow(label = "Rạp", value = "Galaxy Linh Trung")
            InfoRow(label = "Ngày giờ", value = "${ticket.date}, ${ticket.time}")
            InfoRow(label = "Số ghế", value = ticket.seats.joinToString(", "))
            InfoRow(label = "Giá vé", value = "${formatPrice(ticket.totalAmount)} VND")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // QR Code
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qr_code),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "ID Order",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = ticket.id,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatPrice(amount: Int): String {
    return String.format("%,d", amount)
        .replace(",", ".")
} 