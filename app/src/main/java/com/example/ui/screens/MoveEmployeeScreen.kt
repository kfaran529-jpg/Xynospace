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
import com.example.data.model.Employee
import com.example.data.model.Floor
import com.example.data.model.OfficeRoom
import com.example.data.model.Seat
import com.example.ui.components.AppHeader
import com.example.ui.theme.*
import com.example.viewmodel.XynoSpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveEmployeeScreen(
    viewModel: XynoSpaceViewModel,
    preselectedEmployee: Employee?,
    allEmployees: List<Employee>,
    allBuildings: List<Building>,
    allFloors: List<Floor>,
    allRooms: List<OfficeRoom>,
    allSeats: List<Seat>
) {
    var selectedEmployee by remember { mutableStateOf(preselectedEmployee ?: allEmployees.firstOrNull()) }
    var searchEmpText by remember { mutableStateOf("") }
    var showEmpDropdown by remember { mutableStateOf(false) }

    var targetBuilding by remember { mutableStateOf<Building?>(null) }
    var targetFloor by remember { mutableStateOf<Floor?>(null) }
    var targetRoom by remember { mutableStateOf<OfficeRoom?>(null) }
    var targetSeat by remember { mutableStateOf<Seat?>(null) }
    var transferReason by remember { mutableStateOf("Department Reassignment") }

    LaunchedEffect(selectedEmployee) {
        val emp = selectedEmployee
        if (emp != null && targetBuilding == null) {
            val alt = allBuildings.find { it.id != emp.buildingId } ?: allBuildings.firstOrNull()
            targetBuilding = alt
        }
    }

    val availableFloors = remember(targetBuilding, allFloors) {
        allFloors.filter { it.buildingId == targetBuilding?.id }
    }

    val availableRooms = remember(targetFloor, allRooms) {
        allRooms.filter { it.floorId == targetFloor?.id }
    }

    val availableSeats = remember(targetRoom, allSeats) {
        allSeats.filter { it.roomId == targetRoom?.id && !it.isOccupied }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("move_employee_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "Transfer Employee",
                subtitle = "Seamless multi-building, floor & room relocation",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() }
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STEP 1: SELECT EMPLOYEE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchEmpText,
                        onValueChange = {
                            searchEmpText = it
                            showEmpDropdown = true
                        },
                        placeholder = { Text("Search by name, ID or seat...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("move_emp_search_input"),
                        singleLine = true
                    )

                    if (searchEmpText.isNotBlank()) {
                        val matches = allEmployees.filter {
                            it.name.contains(searchEmpText, ignoreCase = true) ||
                            it.employeeId.contains(searchEmpText, ignoreCase = true) ||
                            it.seatNumber.contains(searchEmpText, ignoreCase = true)
                        }.take(5)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column {
                                matches.forEach { emp ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedEmployee = emp
                                                searchEmpText = ""
                                            }
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${emp.name} (${emp.employeeId})", fontWeight = FontWeight.Bold)
                                        Text("${emp.buildingId} / ${emp.roomName} / ${emp.seatNumber}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedEmployee != null) {
                        val emp = selectedEmployee!!
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PersonPinCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = emp.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "ID: ${emp.employeeId} • ${emp.department} • ${emp.designation}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Current: ${emp.buildingId} → ${emp.floorName} → Room ${emp.roomName} → Seat ${emp.seatNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STEP 2: SELECT NEW WORKSTATION DESTINATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("1. Select Destination Building", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allBuildings.forEach { b ->
                            val isSelected = targetBuilding?.id == b.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    targetBuilding = b
                                    targetFloor = null
                                    targetRoom = null
                                    targetSeat = null
                                },
                                label = { Text(b.id, fontWeight = FontWeight.Bold) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("2. Select Floor", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (availableFloors.isEmpty()) {
                        Text("Please select a building first", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableFloors.forEach { f ->
                                val isSelected = targetFloor?.id == f.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        targetFloor = f
                                        targetRoom = null
                                        targetSeat = null
                                    },
                                    label = { Text(f.floorName) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("3. Select Room", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (availableRooms.isEmpty()) {
                        Text("Please select a floor first", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableRooms.forEach { r ->
                                val isSelected = targetRoom?.id == r.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        targetRoom = r
                                        targetSeat = null
                                    },
                                    label = { Text("Room ${r.roomName}") },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("4. Select Available Vacant Seat", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (targetRoom == null) {
                        Text("Please select a room to view available seats", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (availableSeats.isEmpty()) {
                        Text(
                            "Room Full — No Vacant Seats Available in Room ${targetRoom?.roomName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = XynoOccupiedRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSeats.forEach { s ->
                                val isSelected = targetSeat?.id == s.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { targetSeat = s },
                                    label = { Text(s.seatNumber) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = XynoVacantGreenBg,
                                        selectedLabelColor = XynoVacantGreen
                                    ),
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = XynoVacantGreen) }
                                    } else null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = transferReason,
                        onValueChange = { transferReason = it },
                        label = { Text("Transfer Reason / Memo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            val emp = selectedEmployee
            val b = targetBuilding
            val f = targetFloor
            val r = targetRoom
            val s = targetSeat

            val canExecute = emp != null && b != null && f != null && r != null && s != null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONFIRMATION & EFFECT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (emp != null && s != null && r != null && f != null && b != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("• Old Location (${emp.buildingId}/${emp.roomName}/${emp.seatNumber}) will become VACANT.")
                                Text("• New Location (${b.id}/${r.roomName}/${s.seatNumber}) will become OCCUPIED by ${emp.name}.")
                                Text("• Employee records and movement history will update automatically.")
                            }
                        }
                    } else {
                        Text(
                            "Complete all steps above to verify and confirm transfer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (canExecute) {
                                viewModel.executeMoveEmployee(
                                    employee = emp!!,
                                    newSeat = s!!,
                                    newRoom = r!!,
                                    newFloor = f!!,
                                    newBuilding = b!!,
                                    reason = transferReason
                                )
                            }
                        },
                        enabled = canExecute,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_move_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm & Move Employee", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
