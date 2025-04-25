package com.example.movieticket.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movieticket.ui.viewmodel.WalletViewModel
import com.example.movieticket.ui.viewmodel.Transaction
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.walletState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Ví của tôi") }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = state.cardHolderName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Số dư: ${formatCurrency(state.balance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.showAddMoneyDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nạp tiền")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Lịch sử giao dịch",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn {
                items(state.transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }

                if (state.transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có giao dịch nào",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showAddMoneyDialog) {
        AddMoneyDialog(
            onDismiss = { viewModel.hideAddMoneyDialog() },
            onConfirm = { amount ->
                viewModel.addMoney(amount) {
                    // Show success message
                }
            }
        )
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    state.error?.let { error ->
        Toast.makeText(LocalContext.current, error, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatDate(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.type == "Nạp tiền") 
                    Color(0xFF4CAF50) else Color(0xFFE91E63)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatCurrency(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(amount)} VNĐ"
}

@Composable
fun AddMoneyDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nạp tiền vào ví") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it.filter { char -> char.isDigit() }
                        showError = false 
                    },
                    label = { Text("Số tiền") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountLong = amount.toLongOrNull() ?: 0
                    when {
                        amountLong < 10000 -> {
                            showError = true
                            errorMessage = "Số tiền nạp tối thiểu là 10.000 VNĐ"
                        }
                        amountLong > 50000000 -> {
                            showError = true
                            errorMessage = "Số tiền nạp tối đa là 50.000.000 VNĐ"
                        }
                        else -> {
                            onConfirm(amountLong)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
} 