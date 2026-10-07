package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Building
import com.example.data.model.Employee
import com.example.data.model.Floor
import com.example.data.model.OfficeRoom
import com.example.data.model.Seat
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.XynoSpaceViewModel

@Composable
fun RoomSeatingMapScreen(
    viewModel: XynoSpaceViewModel,
    roomId: String,
    rooms: List<OfficeRoom>,
    floors: List<Floor>,
    buildings: List<Building>,
    seats: List<Seat>,
    employees: List<Employee>
) {
    val room = rooms.find { it.id == roomId }
    val floor = floors.find { it.id == room?.floorId }
    val building = buildings.find { it.id == room?.buildingId }

    val roomSeats = seats.filter { it.roomId == roomId }.sortedBy { it.seatNumber }
    val occupiedCount = roomSeats.count { it.isOccupied }
    val capacity = room?.capacity ?: roomSeats.size
    val vacantCount = (capacity - occupiedCount).coerceAtLeast(0)

    var selectedSeatForAction by remember { mutableStateOf<Seat?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }

    if (room == null || floor == null || building == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("room_seating_map_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "${building.id} → ${floor.floorName} → ${room.roomName}",
                subtitle = "Room Seating Map & Desk Allocation",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() }
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ROOM OCCUPANCY STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Room ${room.roomName}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (vacantCount == 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = XynoOccupiedRedBg
                            ) {
                                Text(
                                    text = "Room Full — No Vacant Seats Available",
                                    color = XynoOccupiedRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = XynoVacantGreenBg
                            ) {
                                Text(
                                    text = "$vacantCount Seats Available",
                                    color = XynoVacantGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricStatCard(
                            title = "Total Capacity",
                            value = "$capacity",
                            icon = Icons.Default.EventSeat,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Occupied",
                            value = "$occupiedCount",
                            icon = Icons.Default.Person,
                            color = XynoOccupiedRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Vacant",
                            value = "$vacantCount",
                            icon = Icons.Default.CheckCircle,
                            color = XynoVacantGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val rate = if (capacity > 0) (occupiedCount.toFloat() / capacity.toFloat()) * 100f else 0f
                    Spacer(modifier = Modifier.height(12.dp))
                    OccupancyProgressBar(occupancyRate = rate)
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
                    text = "Seating Arrangement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Click seat to assign / manage",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(roomSeats) { seat ->
            val emp = employees.find { it.seatId == seat.id || it.seatNumber == seat.seatNumber && it.roomId == room.id }
            SeatRowItem(
                seat = seat,
                employee = emp,
                onClick = {
                    selectedSeatForAction = seat
                    showAssignDialog = true
                },
                onQuickMove = {
                    if (emp != null) {
                        viewModel.startMoveEmployee(emp)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showAssignDialog && selectedSeatForAction != null) {
        val targetSeat = selectedSeatForAction!!
        val currentOccupant = employees.find { it.seatId == targetSeat.id }

        SeatActionDialog(
            seat = targetSeat,
            occupant = currentOccupant,
            onDismiss = {
                showAssignDialog = false
                selectedSeatForAction = null
            },
            onAssign = { name, empId, dept, desig ->
                viewModel.assignEmployee(
                    employeeId = empId,
                    name = name,
                    department = dept,
                    designation = desig,
                    targetSeat = targetSeat,
                    room = room,
                    floor = floor,
                    building = building
                )
                showAssignDialog = false
                selectedSeatForAction = null
            },
            onMove = {
                if (currentOccupant != null) {
                    viewModel.startMoveEmployee(currentOccupant)
                    showAssignDialog = false
                    selectedSeatForAction = null
                }
            },
            onVacate = {
                if (currentOccupant != null) {
                    viewModel.removeEmployee(currentOccupant.employeeId)
                    showAssignDialog = false
                    selectedSeatForAction = null
                }
            }
        )
    }
}

@Composable
fun SeatRowItem(
    seat: Seat,
    employee: Employee?,
    onClick: () -> Unit,
    onQuickMove: () -> Unit
) {
    val isOccupied = seat.isOccupied && employee != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .testTag("seat_item_${seat.seatNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOccupied) MaterialTheme.colorScheme.surface else XynoVacantGreenBg.copy(alpha = 0.2f)
        ),
        border = BorderStroke(
            1.dp,
            if (isOccupied) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f) else XynoVacantGreen.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isOccupied) XynoOccupiedRedBg else XynoVacantGreenBg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventSeat,
                        contentDescription = null,
                        tint = if (isOccupied) XynoOccupiedRed else XynoVacantGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Seat ${seat.seatNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(isOccupied = isOccupied)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    if (isOccupied && employee != null) {
                        Text(
                            text = "${employee.name} (${employee.employeeId})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "${employee.department} • ${employee.designation}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    } else {
                        Text(
                            text = "Available — Click to assign employee",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = XynoVacantGreen,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            if (isOccupied && employee != null) {
                FilledTonalIconButton(
                    onClick = onQuickMove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.DriveFileMove,
                        contentDescription = "Move Employee",
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                FilledTonalIconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = XynoVacantGreenBg,
                        contentColor = XynoVacantGreen
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Assign", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SeatActionDialog(
    seat: Seat,
    occupant: Employee?,
    onDismiss: () -> Unit,
    onAssign: (name: String, empId: String, dept: String, desig: String) -> Unit,
    onMove: () -> Unit,
    onVacate: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf("XY-${(1050..1999).random()}") }
    var dept by remember { mutableStateOf("Engineering") }
    var desig by remember { mutableStateOf("Associate") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (occupant != null) "Manage Seat ${seat.seatNumber}" else "Assign Seat ${seat.seatNumber}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (occupant != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Current Occupant:", style = MaterialTheme.typography.labelSmall)
                            Text(text = occupant.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "ID: ${occupant.employeeId} | Dept: ${occupant.department}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Designation: ${occupant.designation}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "You can transfer this employee to another location, or vacate this seat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text("Enter employee details to allocate Seat ${seat.seatNumber}:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Employee Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("assign_emp_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = empId,
                        onValueChange = { empId = it },
                        label = { Text("Employee ID (e.g. XY-1025)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("assign_emp_id_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dept,
                        onValueChange = { dept = it },
                        label = { Text("Department (e.g. Finance, Engineering)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = desig,
                        onValueChange = { desig = it },
                        label = { Text("Designation (e.g. Executive, Lead)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (occupant != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onVacate,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = XynoOccupiedRedBg,
                            contentColor = XynoOccupiedRed
                        )
                    ) {
                        Text("Vacate Seat")
                    }
                    Button(onClick = onMove) {
                        Text("Move Employee")
                    }
                }
            } else {
                Button(
                    onClick = { onAssign(name, empId, dept, desig) },
                    enabled = name.isNotBlank() && empId.isNotBlank(),
                    modifier = Modifier.testTag("confirm_assign_seat_button")
                ) {
                    Text("Confirm Seat Allocation")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
