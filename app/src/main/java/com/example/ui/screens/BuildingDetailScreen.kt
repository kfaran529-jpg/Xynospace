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
import com.example.viewmodel.BuildingSummary
import com.example.viewmodel.FloorSummary
import com.example.viewmodel.XynoSpaceViewModel

@Composable
fun BuildingDetailScreen(
    viewModel: XynoSpaceViewModel,
    buildingSummary: BuildingSummary?
) {
    var showAddFloorDialog by remember { mutableStateOf(false) }

    if (buildingSummary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val building = buildingSummary.building

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("building_detail_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = building.id,
                subtitle = "${building.name} • Seating Management",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddFloorDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_floor_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Floor", fontSize = 12.sp)
                    }
                }
            )
        }

        // Building Stats Overview Card
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
                        text = "BUILDING OCCUPANCY SUMMARY",
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
                            title = "Floors",
                            value = "${buildingSummary.floors.size}",
                            icon = Icons.Default.Layers,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Capacity",
                            value = "${buildingSummary.totalCapacity}",
                            icon = Icons.Default.Apartment,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Occupied",
                            value = "${buildingSummary.occupied}",
                            icon = Icons.Default.Person,
                            color = XynoOccupiedRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Vacant",
                            value = "${buildingSummary.vacant}",
                            icon = Icons.Default.EventSeat,
                            color = XynoVacantGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OccupancyProgressBar(occupancyRate = buildingSummary.occupancyRate)
                }
            }
        }

        // Section header
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
                    text = "Floors in ${building.id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${buildingSummary.floors.size} Floors Configured",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(buildingSummary.floors) { fSummary ->
            FloorCard(
                floorSummary = fSummary,
                onClick = { viewModel.openFloor(fSummary.floor.id) },
                onDelete = { viewModel.deleteFloor(fSummary.floor.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showAddFloorDialog) {
        AddFloorDialog(
            buildingId = building.id,
            onDismiss = { showAddFloorDialog = false },
            onConfirm = { name, level ->
                viewModel.createFloor(building.id, name, level)
                showAddFloorDialog = false
            }
        )
    }
}

@Composable
fun FloorCard(
    floorSummary: FloorSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .testTag("floor_card_${floorSummary.floor.id}"),
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
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = floorSummary.floor.floorName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${floorSummary.rooms.size} Rooms • Capacity: ${floorSummary.totalCapacity}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Open Floor", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room chip preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                floorSummary.rooms.take(6).forEach { rSummary ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Text(
                            text = "${rSummary.room.roomName} (${rSummary.occupied}/${rSummary.capacity})",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                if (floorSummary.rooms.size > 6) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "+${floorSummary.rooms.size - 6} more",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Floor Capacity Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Occupied: ${floorSummary.occupied}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = XynoOccupiedRed
                    )
                )
                Text(
                    text = "Vacant: ${floorSummary.vacant}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = XynoVacantGreen
                    )
                )
                Text(
                    text = "${"%.1f".format(floorSummary.occupancyRate)}% Occupancy",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OccupancyProgressBar(occupancyRate = floorSummary.occupancyRate)
        }
    }
}

@Composable
fun AddFloorDialog(
    buildingId: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, level: Int) -> Unit
) {
    var floorName by remember { mutableStateOf("") }
    var levelStr by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Floor to $buildingId", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Add an independent floor (e.g. Third Floor, Basement, Executive Floor).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = floorName,
                    onValueChange = { floorName = it },
                    label = { Text("Floor Name (e.g. Third Floor)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_floor_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = levelStr,
                    onValueChange = { levelStr = it },
                    label = { Text("Floor Level (e.g. 3)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lvl = levelStr.toIntOrNull() ?: 0
                    onConfirm(floorName, lvl)
                },
                enabled = floorName.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_floor_button")
            ) {
                Text("Add Floor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
