package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.ui.components.AppHeader
import com.example.ui.theme.*
import com.example.viewmodel.XynoSpaceViewModel

@Composable
fun SwapSeatsScreen(
    viewModel: XynoSpaceViewModel,
    allEmployees: List<Employee>
) {
    var emp1 by remember { mutableStateOf<Employee?>(allEmployees.firstOrNull()) }
    var emp2 by remember { mutableStateOf<Employee?>(allEmployees.getOrNull(1)) }

    var search1 by remember { mutableStateOf("") }
    var search2 by remember { mutableStateOf("") }

    val canSwap = emp1 != null && emp2 != null && emp1?.id != emp2?.id

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("swap_seats_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "Swap Employee Seats",
                subtitle = "Mutual exchange of workstations between two employees",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() }
            )
        }

        // Employee 1 Selection Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FIRST EMPLOYEE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = search1,
                        onValueChange = { search1 = it },
                        placeholder = { Text("Search by name or ID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (search1.isNotBlank()) {
                        val matches = allEmployees.filter {
                            it.name.contains(search1, ignoreCase = true) || it.employeeId.contains(search1, ignoreCase = true)
                        }.take(4)
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            matches.forEach { e ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            emp1 = e
                                            search1 = ""
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(e.name, fontWeight = FontWeight.Bold)
                                    Text("${e.buildingId} / ${e.roomName} / ${e.seatNumber}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (emp1 != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(emp1!!.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("ID: ${emp1!!.employeeId} • ${emp1!!.department} • ${emp1!!.designation}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Location: ${emp1!!.buildingId} → ${emp1!!.floorName} → Room ${emp1!!.roomName} → Seat ${emp1!!.seatNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Swap Icon Center Indicator
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Exchange",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Employee 2 Selection Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SECOND EMPLOYEE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = search2,
                        onValueChange = { search2 = it },
                        placeholder = { Text("Search by name or ID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (search2.isNotBlank()) {
                        val matches = allEmployees.filter {
                            it.name.contains(search2, ignoreCase = true) || it.employeeId.contains(search2, ignoreCase = true)
                        }.take(4)
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            matches.forEach { e ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            emp2 = e
                                            search2 = ""
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(e.name, fontWeight = FontWeight.Bold)
                                    Text("${e.buildingId} / ${e.roomName} / ${e.seatNumber}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (emp2 != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(emp2!!.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("ID: ${emp2!!.employeeId} • ${emp2!!.department} • ${emp2!!.designation}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Location: ${emp2!!.buildingId} → ${emp2!!.floorName} → Room ${emp2!!.roomName} → Seat ${emp2!!.seatNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confirmation Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXCHANGE PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (canSwap) {
                        Text("• ${emp1!!.name} will move to Seat ${emp2!!.seatNumber} in ${emp2!!.buildingId}/${emp2!!.roomName}")
                        Text("• ${emp2!!.name} will move to Seat ${emp1!!.seatNumber} in ${emp1!!.buildingId}/${emp1!!.roomName}")
                    } else {
                        Text("Select two distinct employees to execute mutual seat swap.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (canSwap) {
                                viewModel.executeSwapSeats(emp1!!, emp2!!)
                            }
                        },
                        enabled = canSwap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_swap_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm & Swap Seats", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
