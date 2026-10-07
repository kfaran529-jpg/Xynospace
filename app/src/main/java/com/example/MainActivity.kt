package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.Screen
import com.example.viewmodel.XynoSpaceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: XynoSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: XynoSpaceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val orgStats by viewModel.organizationStats.collectAsStateWithLifecycle()
    val buildings by viewModel.buildings.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val seats by viewModel.seats.collectAsStateWithLifecycle()
    val employees by viewModel.filteredEmployees.collectAsStateWithLifecycle()
    val allEmployees by viewModel.employees.collectAsStateWithLifecycle()
    val movements by viewModel.movements.collectAsStateWithLifecycle()

    val selectedBuildingId by viewModel.selectedBuildingId.collectAsStateWithLifecycle()
    val selectedFloorId by viewModel.selectedFloorId.collectAsStateWithLifecycle()
    val selectedRoomId by viewModel.selectedRoomId.collectAsStateWithLifecycle()
    val selectedEmployee by viewModel.selectedEmployee.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Android Hardware / System Back Button Handling
    BackHandler(enabled = currentScreen != Screen.DASHBOARD) {
        viewModel.goBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.DASHBOARD || currentScreen == Screen.BUILDING_DETAIL || currentScreen == Screen.FLOOR_DETAIL || currentScreen == Screen.ROOM_DETAIL,
                    onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                    icon = { Icon(if (currentScreen == Screen.DASHBOARD) Icons.Filled.Apartment else Icons.Outlined.Apartment, contentDescription = "Buildings") },
                    label = { Text("Buildings") },
                    modifier = Modifier.testTag("nav_item_buildings")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.EMPLOYEES,
                    onClick = { viewModel.navigateTo(Screen.EMPLOYEES) },
                    icon = { Icon(if (currentScreen == Screen.EMPLOYEES) Icons.Filled.People else Icons.Outlined.People, contentDescription = "Staff") },
                    label = { Text("Staff") },
                    modifier = Modifier.testTag("nav_item_staff")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.MOVE_EMPLOYEE || currentScreen == Screen.SWAP_SEATS,
                    onClick = { viewModel.navigateTo(Screen.MOVE_EMPLOYEE) },
                    icon = { Icon(if (currentScreen == Screen.MOVE_EMPLOYEE) Icons.Filled.SwapHoriz else Icons.Outlined.SwapHoriz, contentDescription = "Transfer") },
                    label = { Text("Transfer") },
                    modifier = Modifier.testTag("nav_item_transfer")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.VACANT_SPACE,
                    onClick = { viewModel.navigateTo(Screen.VACANT_SPACE) },
                    icon = { Icon(if (currentScreen == Screen.VACANT_SPACE) Icons.Filled.EventSeat else Icons.Outlined.EventSeat, contentDescription = "Vacant") },
                    label = { Text("Vacant") },
                    modifier = Modifier.testTag("nav_item_vacant")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.REPORTS || currentScreen == Screen.MOVEMENTS || currentScreen == Screen.ADMIN,
                    onClick = { viewModel.navigateTo(Screen.REPORTS) },
                    icon = { Icon(if (currentScreen == Screen.REPORTS) Icons.Filled.Assessment else Icons.Outlined.Assessment, contentDescription = "Reports") },
                    label = { Text("Reports") },
                    modifier = Modifier.testTag("nav_item_reports")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(viewModel = viewModel, orgStats = orgStats)
                }

                Screen.BUILDING_DETAIL -> {
                    val bSummary = orgStats.buildings.find { it.building.id == selectedBuildingId }
                    BuildingDetailScreen(viewModel = viewModel, buildingSummary = bSummary)
                }

                Screen.FLOOR_DETAIL -> {
                    val bSummary = orgStats.buildings.find { it.building.id == selectedBuildingId }
                    val fSummary = bSummary?.floors?.find { it.floor.id == selectedFloorId }
                    FloorDetailScreen(
                        viewModel = viewModel,
                        floorSummary = fSummary,
                        buildingId = selectedBuildingId ?: ""
                    )
                }

                Screen.ROOM_DETAIL -> {
                    RoomSeatingMapScreen(
                        viewModel = viewModel,
                        roomId = selectedRoomId ?: "",
                        rooms = rooms,
                        floors = floors,
                        buildings = buildings,
                        seats = seats,
                        employees = allEmployees
                    )
                }

                Screen.EMPLOYEES -> {
                    EmployeesScreen(
                        viewModel = viewModel,
                        employees = employees,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.searchQuery.value = it }
                    )
                }

                Screen.MOVE_EMPLOYEE -> {
                    MoveEmployeeScreen(
                        viewModel = viewModel,
                        preselectedEmployee = selectedEmployee,
                        allEmployees = allEmployees,
                        allBuildings = buildings,
                        allFloors = floors,
                        allRooms = rooms,
                        allSeats = seats
                    )
                }

                Screen.SWAP_SEATS -> {
                    SwapSeatsScreen(
                        viewModel = viewModel,
                        allEmployees = allEmployees
                    )
                }

                Screen.VACANT_SPACE -> {
                    VacantSpaceFinderScreen(
                        viewModel = viewModel,
                        buildings = buildings,
                        floors = floors,
                        rooms = rooms,
                        seats = seats
                    )
                }

                Screen.MOVEMENTS -> {
                    MovementHistoryScreen(
                        viewModel = viewModel,
                        movements = movements
                    )
                }

                Screen.REPORTS -> {
                    ReportsScreen(
                        viewModel = viewModel,
                        orgStats = orgStats
                    )
                }

                Screen.ADMIN -> {
                    AdminManagementScreen(
                        viewModel = viewModel,
                        buildings = buildings,
                        floors = floors,
                        rooms = rooms,
                        seats = seats,
                        employees = allEmployees
                    )
                }
            }
        }
    }
}
