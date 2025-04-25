package com.example.movieticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieticket.data.model.Seat
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _seats = MutableStateFlow<List<Seat>>(generateInitialSeats())
    val seats: StateFlow<List<Seat>> = _seats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private fun generateInitialSeats(): List<Seat> {
        val rows = ('A'..'G')
        val columns = 1..8
        return rows.flatMap { row ->
            columns.map { col ->
                Seat(
                    id = "$row$col",
                    isBooked = false,
                    isSelected = false
                )
            }
        }
    }

    fun loadBookedSeats(movieId: Int, date: String, time: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                Log.d("BookingViewModel", "Loading booked seats for movie: $movieId, date: $date, time: $time")

                // Kiểm tra tất cả các vé trước
                val allTickets = firestore.collection("tickets")
                    .get()
                    .await()

                Log.d("BookingViewModel", "Total tickets in collection: ${allTickets.documents.size}")
                allTickets.documents.forEach { doc ->
                    Log.d("BookingViewModel", "Ticket: movieId=${doc.getLong("movieId")}, date=${doc.getString("date")}, time=${doc.getString("time")}, seats=${doc.get("seats")}")
                }

                // Chuẩn hóa định dạng ngày tháng
                val normalizedDate = date.replace("-", "/")
                
                // Lấy danh sách vé đã đặt từ Firebase
                val bookedSeats = firestore.collection("tickets")
                    .whereEqualTo("movieId", movieId.toLong()) // Chuyển Int thành Long
                    .get()
                    .await()
                    .documents
                    .filter { doc -> 
                        // Lọc theo ngày và giờ trong memory vì có nhiều định dạng khác nhau
                        val ticketDate = doc.getString("date")?.replace("-", "/")
                        val ticketTime = doc.getString("time")
                        ticketDate == normalizedDate && ticketTime == time
                    }

                Log.d("BookingViewModel", "Found ${bookedSeats.size} tickets for specific query")

                val bookedSeatsList = bookedSeats.flatMap { doc -> 
                    val seats = doc.get("seats") as? List<String>
                    Log.d("BookingViewModel", "Document ${doc.id} data: ${doc.data}")
                    Log.d("BookingViewModel", "Seats from document: $seats")
                    seats ?: emptyList()
                }

                Log.d("BookingViewModel", "Total booked seats: ${bookedSeatsList.size}, Seats: $bookedSeatsList")

                // Cập nhật trạng thái ghế
                _seats.update { currentSeats ->
                    currentSeats.map { seat ->
                        val isBooked = bookedSeatsList.contains(seat.id)
                        Log.d("BookingViewModel", "Seat ${seat.id} isBooked: $isBooked")
                        seat.copy(isBooked = isBooked)
                    }
                }

                // Log final state
                Log.d("BookingViewModel", "Final seats state: ${_seats.value.filter { it.isBooked }.map { it.id }}")
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error loading booked seats", e)
                _error.value = "Không thể tải thông tin ghế: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSeatSelection(seatId: String) {
        _seats.update { currentSeats ->
            currentSeats.map { seat ->
                if (seat.id == seatId && !seat.isBooked) {
                    seat.copy(isSelected = !seat.isSelected)
                } else {
                    seat
                }
            }
        }
    }

    fun getSelectedSeats(): List<String> {
        return seats.value.filter { it.isSelected }.map { it.id }
    }

    fun clearSelectedSeats() {
        _seats.update { currentSeats ->
            currentSeats.map { seat ->
                if (seat.isSelected) {
                    seat.copy(isSelected = false)
                } else {
                    seat
                }
            }
        }
    }
} 