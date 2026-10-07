package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.theme.*
import com.example.viewmodel.BuildingSummary
import com.example.viewmodel.OrganizationStats
import com.example.viewmodel.XynoSpaceViewModel

@Composable
fun ReportsScreen(
    viewModel: XynoSpaceViewModel,
    orgStats: OrganizationStats
) {
    val context = LocalContext.current
    var selectedBuildingId by remember { mutableStateOf(orgStats.buildings.firstOrNull()?.building?.id ?: "") }

    LaunchedEffect(orgStats.buildings) {
        if (selectedBuildingId.isEmpty() && orgStats.buildings.isNotEmpty()) {
            selectedBuildingId = orgStats.buildings.first().building.id
        }
    }

    val currentBuildingSummary = orgStats.buildings.find { it.building.id == selectedBuildingId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            AppHeader(
                title = "Building-Wise Reports",
                subtitle = "Tabular occupancy statements & exportable audit reports",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = { viewModel.goBack() },
                actions = {
                    FilledTonalIconButton(
                        onClick = {
                            if (currentBuildingSummary != null) {
                                val csv = generateCsvReport(currentBuildingSummary)
                                copyToClipboard(context, "XynoSpace_${currentBuildingSummary.building.id}_Report.csv", csv)
                                Toast.makeText(context, "Building report copied to clipboard as CSV", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", modifier = Modifier.size(20.dp))
                    }
                }
            )
        }

        // Building Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                orgStats.buildings.forEach { b ->
                    FilterChip(
                        selected = selectedBuildingId == b.building.id,
                        onClick = { selectedBuildingId = b.building.id },
                        label = { Text(b.building.id, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Report Table Card matching Requirement 15:
        // Floor | Rooms | Capacity | Occupied | Vacant
        if (currentBuildingSummary != null) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OFFICIAL REPORT: ${currentBuildingSummary.building.id}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = "${"%.1f".format(currentBuildingSummary.occupancyRate)}% Occupied",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Table Header
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Floor", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Rooms", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Capacity", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Occupied", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = XynoOccupiedRed)
                                Text("Vacant", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = XynoVacantGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Table Rows
                        currentBuildingSummary.floors.forEach { f ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(f.floor.floorName.replace(" Floor", ""), modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("${f.rooms.size}", modifier = Modifier.weight(0.8f), fontSize = 13.sp)
                                Text("${f.totalCapacity}", modifier = Modifier.weight(0.9f), fontSize = 13.sp)
                                Text("${f.occupied}", modifier = Modifier.weight(0.9f), fontSize = 13.sp, color = XynoOccupiedRed, fontWeight = FontWeight.SemiBold)
                                Text("${f.vacant}", modifier = Modifier.weight(0.8f), fontSize = 13.sp, color = XynoVacantGreen, fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }

                        // Total Footer Row
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                Text("${currentBuildingSummary.totalRooms}", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                Text("${currentBuildingSummary.totalCapacity}", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                Text("${currentBuildingSummary.occupied}", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = XynoOccupiedRed)
                                Text("${currentBuildingSummary.vacant}", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = XynoVacantGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun generateCsvReport(summary: BuildingSummary): String {
    val sb = StringBuilder()
    sb.appendLine("Floor,Rooms,Capacity,Occupied,Vacant,OccupancyRate")
    summary.floors.forEach { f ->
        sb.appendLine("${f.floor.floorName},${f.rooms.size},${f.totalCapacity},${f.occupied},${f.vacant},${"%.1f".format(f.occupancyRate)}%")
    }
    sb.appendLine("TOTAL,${summary.totalRooms},${summary.totalCapacity},${summary.occupied},${summary.vacant},${"%.1f".format(summary.occupancyRate)}%")
    return sb.toString()
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
