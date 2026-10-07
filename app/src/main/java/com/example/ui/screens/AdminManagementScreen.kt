package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun AdminManagementScreen(
    viewModel: XynoSpaceViewModel,
    buildings: List<Building>,
    floors: List<Floor>,
    rooms: List<OfficeRoom>,
    seats: List<Seat>,
    employees: List<Employee>
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddBuildingDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "Admin Console",
                subtitle = "Infrastructure management, CSV data import & export",
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DATA IMPORT / EXPORT (CSV / EXCEL)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Import bulk employees or export full organizational seating sheets (Employee ID, Name, Department, Designation, Building, Floor, Room, Seat Number).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f).testTag("import_csv_button")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import CSV")
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = exportEmployeesCsv(employees)
                                copyToClipboard(context, "XynoSpace_Employees.csv", csv)
                                Toast.makeText(context, "Exported ${employees.size} employees to clipboard as CSV", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f).testTag("export_csv_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV")
                        }
                    }
                }
            }
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ORGANIZATION INVENTORY SNAPSHOT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Buildings", style = MaterialTheme.typography.labelSmall)
                            Text("${buildings.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Floors", style = MaterialTheme.typography.labelSmall)
                            Text("${floors.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Rooms", style = MaterialTheme.typography.labelSmall)
                            Text("${rooms.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Total Desks", style = MaterialTheme.typography.labelSmall)
                            Text("${seats.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Staff", style = MaterialTheme.typography.labelSmall)
                            Text("${employees.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BUILDING INFRASTRUCTURE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Create new physical facilities (e.g. B-101, B-200)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(onClick = { showAddBuildingDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Building")
                        }
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        CsvImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { csvText ->
                viewModel.importCsv(csvText)
                showImportDialog = false
            }
        )
    }

    if (showAddBuildingDialog) {
        AddBuildingDialog(
            onDismiss = { showAddBuildingDialog = false },
            onConfirm = { id, name, address ->
                viewModel.createBuilding(id, name, address)
                showAddBuildingDialog = false
            }
        )
    }
}

@Composable
fun CsvImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    val sampleCsv = """
Employee ID,Employee Name,Department,Designation,Building,Floor,Room,Seat Number
XY-1090,Zainab Fatima,Finance,Senior Analyst,B-112,Ground Floor,G1,G1-05
XY-1091,Mustafa Qureshi,Engineering,Tech Lead,B-112,Ground Floor,G1,G1-09
XY-1092,Hira Mani,Human Resources,Manager,B-144,Ground Floor,G1,G1-05
""".trimIndent()

    var csvText by remember { mutableStateOf(sampleCsv) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Employees CSV", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Paste CSV lines with 8 columns (Employee ID, Name, Department, Designation, Building, Floor, Room, Seat Number):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("csv_input_field"),
                    placeholder = { Text("Paste CSV data...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(csvText) },
                enabled = csvText.isNotBlank(),
                modifier = Modifier.testTag("confirm_import_csv_button")
            ) {
                Text("Process Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun exportEmployeesCsv(employees: List<Employee>): String {
    val sb = StringBuilder()
    sb.appendLine("Employee ID,Employee Name,Department,Designation,Building,Floor,Room,Seat Number")
    employees.forEach { e ->
        sb.appendLine("${e.employeeId},${e.name},${e.department},${e.designation},${e.buildingId},${e.floorName},${e.roomName},${e.seatNumber}")
    }
    return sb.toString()
}
