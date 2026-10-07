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
import com.example.data.model.Building
import com.example.data.model.Floor
import com.example.data.model.OfficeRoom
import com.example.data.model.Seat
import com.example.ui.components.AppHeader
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*
import com.example.viewmodel.XynoSpaceViewModel

@Composable
fun VacantSpaceFinderScreen(
    viewModel: XynoSpaceViewModel,
    buildings: List<Building>,
    floors: List<Floor>,
    rooms: List<OfficeRoom>,
    seats: List<Seat>
) {
    var selectedBuildingFilter by remember { mutableStateOf<String?>("ALL") }
    var expandedRoomId by remember { mutableStateOf<String?>(null) }

    val vacantSeats = seats.filter { !it.isOccupied }
    val totalVacant = vacantSeats.size

    val filteredBuildings = if (selectedBuildingFilter == "ALL" || selectedBuildingFilter == null) {
        buildings
    } else {
        buildings.filter { it.id == selectedBuildingFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vacant_space_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "Available Space Finder",
                subtitle = "Organization-wide vacant desk explorer",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() }
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TOTAL VACANT WORKSTATIONS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "$totalVacant Seats Available",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = XynoVacantGreen
                            )
                        )
                        Text("Across ${buildings.size} Xynotech corporate buildings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = XynoVacantGreenBg,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.EventSeat, contentDescription = null, tint = XynoVacantGreen)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedBuildingFilter == "ALL",
                    onClick = { selectedBuildingFilter = "ALL" },
                    label = { Text("All Buildings") }
                )
                buildings.forEach { b ->
                    FilterChip(
                        selected = selectedBuildingFilter == b.id,
                        onClick = { selectedBuildingFilter = b.id },
                        label = { Text(b.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(filteredBuildings) { b ->
            val bFloors = floors.filter { it.buildingId == b.id }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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
                        Text(
                            text = b.id,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        val bVacantCount = seats.count { it.buildingId == b.id && !it.isOccupied }
                        Surface(shape = RoundedCornerShape(8.dp), color = XynoVacantGreenBg) {
                            Text(
                                text = "$bVacantCount Vacant",
                                color = XynoVacantGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    bFloors.forEach { f ->
                        val fRooms = rooms.filter { it.floorId == f.id }
                        val fVacantRooms = fRooms.map { r ->
                            val rSeats = seats.filter { it.roomId == r.id && !it.isOccupied }
                            r to rSeats
                        }.filter { it.second.isNotEmpty() }

                        if (fVacantRooms.isNotEmpty()) {
                            Text(
                                text = f.floorName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            fVacantRooms.forEach { (room, rVacantSeats) ->
                                val isExpanded = expandedRoomId == room.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            expandedRoomId = if (isExpanded) null else room.id
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Room ${room.roomName}", fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                "${rVacantSeats.size} vacant",
                                                fontWeight = FontWeight.Bold,
                                                color = XynoVacantGreen
                                            )
                                        }

                                        if (isExpanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Exact Available Seats:",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                rVacantSeats.forEach { s ->
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = XynoVacantGreenBg
                                                    ) {
                                                        Text(
                                                            text = s.seatNumber,
                                                            color = XynoVacantGreen,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}
