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
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.FloorSummary
import com.example.viewmodel.RoomSummary
import com.example.viewmodel.XynoSpaceViewModel

@Composable
fun FloorDetailScreen(
    viewModel: XynoSpaceViewModel,
    floorSummary: FloorSummary?,
    buildingId: String
) {
    var showAddRoomDialog by remember { mutableStateOf(false) }

    if (floorSummary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val floor = floorSummary.floor

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("floor_detail_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "$buildingId — ${floor.floorName}",
                subtitle = "Rooms: ${floorSummary.rooms.joinToString(" | ") { it.room.roomName }}",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddRoomDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_room_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Room", fontSize = 12.sp)
                    }
                }
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FLOOR SEATING SUMMARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricStatCard(
                            title = "Rooms",
                            value = "${floorSummary.rooms.size}",
                            icon = Icons.Default.MeetingRoom,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Capacity",
                            value = "${floorSummary.totalCapacity}",
                            icon = Icons.Default.Apartment,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Occupied",
                            value = "${floorSummary.occupied}",
                            icon = Icons.Default.Person,
                            color = XynoOccupiedRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Vacant",
                            value = "${floorSummary.vacant}",
                            icon = Icons.Default.EventSeat,
                            color = XynoVacantGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OccupancyProgressBar(occupancyRate = floorSummary.occupancyRate)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rooms & Seating Arrangements",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${floorSummary.rooms.size} Active Rooms",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(floorSummary.rooms) { rSummary ->
            RoomCard(
                roomSummary = rSummary,
                buildingId = buildingId,
                floorName = floor.floorName,
                onClick = { viewModel.openRoom(rSummary.room.id) },
                onEditCapacity = { newCap -> viewModel.updateRoomCapacity(rSummary.room.id, newCap) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            buildingId = buildingId,
            floorName = floor.floorName,
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { name, capacity ->
                viewModel.createRoom(buildingId, floor.id, name, capacity)
                showAddRoomDialog = false
            }
        )
    }
}

@Composable
fun RoomCard(
    roomSummary: RoomSummary,
    buildingId: String,
    floorName: String,
    onClick: () -> Unit,
    onEditCapacity: (Int) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .testTag("room_card_${roomSummary.room.roomName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = roomSummary.room.roomName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Room ${roomSummary.room.roomName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$buildingId → $floorName",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Capacity", modifier = Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Open Seating Map", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Capacity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${roomSummary.capacity}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text(text = "Occupied", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${roomSummary.occupied}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = XynoOccupiedRed))
                }
                Column {
                    Text(text = "Vacant", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${roomSummary.vacant}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = XynoVacantGreen))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Occupancy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${"%.0f".format(roomSummary.occupancyRate)}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OccupancyProgressBar(occupancyRate = roomSummary.occupancyRate)
        }
    }

    if (showEditDialog) {
        var capStr by remember { mutableStateOf("${roomSummary.capacity}") }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Room Capacity") },
            text = {
                Column {
                    Text("Change seating capacity for Room ${roomSummary.room.roomName}:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = capStr,
                        onValueChange = { capStr = it },
                        label = { Text("Capacity") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = capStr.toIntOrNull() ?: roomSummary.capacity
                        onEditCapacity(c)
                        showEditDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddRoomDialog(
    buildingId: String,
    floorName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, capacity: Int) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room to $floorName", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Seats will be generated automatically based on capacity (e.g. G6-01 to G6-10).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Room Name / Code (e.g. G6, F7, S2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_room_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    label = { Text("Seating Capacity (e.g. 10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_room_capacity_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityStr.toIntOrNull() ?: 10
                    onConfirm(roomName, cap)
                },
                enabled = roomName.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_room_button")
            ) {
                Text("Create Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
