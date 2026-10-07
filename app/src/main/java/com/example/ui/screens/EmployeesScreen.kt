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
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*
import com.example.viewmodel.XynoSpaceViewModel

@Composable
fun EmployeesScreen(
    viewModel: XynoSpaceViewModel,
    employees: List<Employee>,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    var selectedEmployeeForDetail by remember { mutableStateOf<Employee?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("employees_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "Employee Directory",
                subtitle = "Global organizational search & profile management",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() }
            )
        }

        // Global Search Bar as specified in Requirement 13
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search by Name, ID, Department, Building, Seat...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("global_employee_search_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Found ${employees.size} employees across all locations",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        items(employees) { emp ->
            EmployeeCard(
                employee = emp,
                onClick = { selectedEmployeeForDetail = emp },
                onMoveClick = { viewModel.startMoveEmployee(emp) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (employees.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No employees matching search criteria", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (selectedEmployeeForDetail != null) {
        EmployeeDetailDialog(
            employee = selectedEmployeeForDetail!!,
            onDismiss = { selectedEmployeeForDetail = null },
            onMove = {
                val emp = selectedEmployeeForDetail!!
                selectedEmployeeForDetail = null
                viewModel.startMoveEmployee(emp)
            },
            onDelete = {
                viewModel.removeEmployee(selectedEmployeeForDetail!!.employeeId)
                selectedEmployeeForDetail = null
            }
        )
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onClick: () -> Unit,
    onMoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .testTag("emp_card_${employee.employeeId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = employee.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = employee.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${employee.employeeId} • ${employee.department} • ${employee.designation}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${employee.buildingId} → ${employee.floorName} → ${employee.roomName} → ${employee.seatNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onMoveClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.DriveFileMove, contentDescription = "Move", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun EmployeeDetailDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(employee.name, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Employee ID: ${employee.employeeId}", fontWeight = FontWeight.Bold)
                        Text("Department: ${employee.department}")
                        Text("Designation: ${employee.designation}")
                        Text("Status: ${employee.status}")
                        Text("Date Added: ${employee.dateAdded}")
                        Text("Last Location Change: ${employee.lastLocationChange}")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Current Seating Location:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Building: ${employee.buildingId}", fontWeight = FontWeight.Bold)
                        Text("Floor: ${employee.floorName}")
                        Text("Room: ${employee.roomName}")
                        Text("Seat: ${employee.seatNumber}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = XynoOccupiedRedBg, contentColor = XynoOccupiedRed)
                ) {
                    Text("Remove")
                }
                Button(onClick = onMove) {
                    Text("Move Employee")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
