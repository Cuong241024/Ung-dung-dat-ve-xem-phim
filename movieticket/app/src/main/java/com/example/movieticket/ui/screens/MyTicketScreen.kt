package com.example.movieticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.movieticket.data.model.Ticket
import com.example.movieticket.ui.viewmodel.MyTicketViewModel
import androidx.compose.foundation.clickable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketScreen(
    viewModel: MyTicketViewModel = hiltViewModel(),
    onTicketClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Vé của tôi",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Có lỗi xảy ra",
                    color = Color.Red
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.tickets) { ticket ->
                    TicketItem(
                        ticket = ticket,
                        onClick = { onTicketClick(ticket.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TicketItem(
    ticket: Ticket,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B2C3D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            // Ticket Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = ticket.movieTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${ticket.date} | ${ticket.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = "Ghế: ${ticket.seats.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = "${formatPrice(ticket.totalAmount)} VND",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold
                )

                if (ticket.status == "active") {
                    Text(
                        text = "Còn hiệu lực",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatPrice(amount: Int): String {
    return String.format("%,d", amount)
        .replace(",", ".")
}

// Data class cho vé
data class Ticket(
    val id: String,
    val movieTitle: String,
    val moviePoster: String,
    val date: String,
    val time: String,
    val seats: List<String>,
    val totalAmount: Int,
    val status: String
)

// Sample data
val sampleTickets = listOf(
    Ticket(
        id = "1",
        movieTitle = "Một bộ phim Minecraft",
        moviePoster = "/path_to_minecraft_poster.jpg",
        date = "29/04/2025",
        time = "15:00",
        seats = listOf("F4", "F5"),
        totalAmount = 192000,
        status = "active"
    ),
    Ticket(
        id = "2",
        movieTitle = "G20",
        moviePoster = "/path_to_g20_poster.jpg",
        date = "25/04/2025",
        time = "12:30",
        seats = listOf("A4", "A5"),
        totalAmount = 232000,
        status = "active"
    )
) 