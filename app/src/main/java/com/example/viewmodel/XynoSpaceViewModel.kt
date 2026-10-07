package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DatabaseInitializer
import com.example.data.model.*
import com.example.data.repository.XynoSpaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Screen Navigation Enum
enum class Screen {
    DASHBOARD,
    BUILDING_DETAIL,
    FLOOR_DETAIL,
    ROOM_DETAIL,
    EMPLOYEES,
    MOVE_EMPLOYEE,
    SWAP_SEATS,
    VACANT_SPACE,
    MOVEMENTS,
    REPORTS,
    ADMIN
}

// Calculated UI Data Models
data class RoomSummary(
    val room: OfficeRoom,
    val capacity: Int,
    val occupied: Int,
    val vacant: Int,
    val occupancyRate: Float
)

data class FloorSummary(
    val floor: Floor,
    val rooms: List<RoomSummary>,
    val totalCapacity: Int,
    val occupied: Int,
    val vacant: Int,
    val occupancyRate: Float
)

data class BuildingSummary(
    val building: Building,
    val floors: List<FloorSummary>,
    val totalRooms: Int,
    val totalCapacity: Int,
    val occupied: Int,
    val vacant: Int,
    val occupancyRate: Float
)

data class OrganizationStats(
    val totalBuildings: Int,
    val totalFloors: Int,
    val totalRooms: Int,
    val totalCapacity: Int,
    val totalOccupied: Int,
    val totalVacant: Int,
    val overallOccupancyRate: Float,
    val buildings: List<BuildingSummary>
)

class XynoSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = XynoSpaceRepository(db)

    // Current Navigation State
    val currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val selectedBuildingId = MutableStateFlow<String?>(null)
    val selectedFloorId = MutableStateFlow<String?>(null)
    val selectedRoomId = MutableStateFlow<String?>(null)
    val selectedEmployee = MutableStateFlow<Employee?>(null)

    val searchQuery = MutableStateFlow("")

    val statusMessage = MutableStateFlow<String?>(null)
    val errorMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            DatabaseInitializer.populateInitialDataIfEmpty(db)
        }
    }

    val buildings: StateFlow<List<Building>> = repository.allBuildings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val floors: StateFlow<List<Floor>> = repository.allFloors.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val rooms: StateFlow<List<OfficeRoom>> = repository.allRooms.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val seats: StateFlow<List<Seat>> = repository.allSeats.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val employees: StateFlow<List<Employee>> = repository.allEmployees.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val movements: StateFlow<List<EmployeeMovement>> = repository.allMovements.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val organizationStats: StateFlow<OrganizationStats> = combine(
        buildings, floors, rooms, seats
    ) { bList, fList, rList, sList ->
        val buildingSummaries = bList.map { b ->
            val bFloors = fList.filter { it.buildingId == b.id }
            val floorSummaries = bFloors.map { f ->
                val fRooms = rList.filter { it.floorId == f.id }
                val roomSummaries = fRooms.map { r ->
                    val rSeats = sList.filter { it.roomId == r.id }
                    val cap = r.capacity.coerceAtLeast(rSeats.size)
                    val occ = rSeats.count { it.isOccupied }
                    val vac = (cap - occ).coerceAtLeast(0)
                    val rate = if (cap > 0) (occ.toFloat() / cap.toFloat()) * 100f else 0f
                    RoomSummary(r, cap, occ, vac, rate)
                }
                val fCap = roomSummaries.sumOf { it.capacity }
                val fOcc = roomSummaries.sumOf { it.occupied }
                val fVac = (fCap - fOcc).coerceAtLeast(0)
                val fRate = if (fCap > 0) (fOcc.toFloat() / fCap.toFloat()) * 100f else 0f
                FloorSummary(f, roomSummaries, fCap, fOcc, fVac, fRate)
            }
            val bRoomsCount = floorSummaries.sumOf { it.rooms.size }
            val bCap = floorSummaries.sumOf { it.totalCapacity }
            val bOcc = floorSummaries.sumOf { it.occupied }
            val bVac = (bCap - bOcc).coerceAtLeast(0)
            val bRate = if (bCap > 0) (bOcc.toFloat() / bCap.toFloat()) * 100f else 0f
            BuildingSummary(b, floorSummaries, bRoomsCount, bCap, bOcc, bVac, bRate)
        }

        val totalBuildings = buildingSummaries.size
        val totalFloors = buildingSummaries.sumOf { it.floors.size }
        val totalRooms = buildingSummaries.sumOf { it.totalRooms }
        val totalCapacity = buildingSummaries.sumOf { it.totalCapacity }
        val totalOccupied = buildingSummaries.sumOf { it.occupied }
        val totalVacant = (totalCapacity - totalOccupied).coerceAtLeast(0)
        val overallRate = if (totalCapacity > 0) (totalOccupied.toFloat() / totalCapacity.toFloat()) * 100f else 0f

        OrganizationStats(
            totalBuildings = totalBuildings,
            totalFloors = totalFloors,
            totalRooms = totalRooms,
            totalCapacity = totalCapacity,
            totalOccupied = totalOccupied,
            totalVacant = totalVacant,
            overallOccupancyRate = overallRate,
            buildings = buildingSummaries
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OrganizationStats(0, 0, 0, 0, 0, 0, 0f, emptyList())
    )

    val filteredEmployees: StateFlow<List<Employee>> = combine(employees, searchQuery) { empList, query ->
        if (query.isBlank()) {
            empList
        } else {
            val q = query.trim().lowercase()
            empList.filter { emp ->
                emp.name.lowercase().contains(q) ||
                emp.employeeId.lowercase().contains(q) ||
                emp.department.lowercase().contains(q) ||
                emp.designation.lowercase().contains(q) ||
                emp.buildingId.lowercase().contains(q) ||
                emp.floorName.lowercase().contains(q) ||
                emp.roomName.lowercase().contains(q) ||
                emp.seatNumber.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }

    fun openBuilding(buildingId: String) {
        selectedBuildingId.value = buildingId
        currentScreen.value = Screen.BUILDING_DETAIL
    }

    fun openFloor(floorId: String) {
        selectedFloorId.value = floorId
        currentScreen.value = Screen.FLOOR_DETAIL
    }

    fun openRoom(roomId: String) {
        selectedRoomId.value = roomId
        currentScreen.value = Screen.ROOM_DETAIL
    }

    fun startMoveEmployee(employee: Employee) {
        selectedEmployee.value = employee
        currentScreen.value = Screen.MOVE_EMPLOYEE
    }

    fun goBack(): Boolean {
        return when (currentScreen.value) {
            Screen.ROOM_DETAIL -> {
                currentScreen.value = Screen.FLOOR_DETAIL
                true
            }
            Screen.FLOOR_DETAIL -> {
                currentScreen.value = Screen.BUILDING_DETAIL
                true
            }
            Screen.BUILDING_DETAIL -> {
                currentScreen.value = Screen.DASHBOARD
                true
            }
            Screen.MOVE_EMPLOYEE,
            Screen.SWAP_SEATS,
            Screen.VACANT_SPACE,
            Screen.MOVEMENTS,
            Screen.REPORTS,
            Screen.ADMIN,
            Screen.EMPLOYEES -> {
                currentScreen.value = Screen.DASHBOARD
                true
            }
            Screen.DASHBOARD -> false
        }
    }

    fun createBuilding(id: String, name: String, address: String) {
        viewModelScope.launch {
            if (id.isBlank() || name.isBlank()) {
                errorMessage.value = "Building ID and Name are required."
                return@launch
            }
            val building = Building(id = id.trim().uppercase(), name = name.trim(), address = address.trim())
            repository.addBuilding(building, defaultFloors = true)
            statusMessage.value = "Building ${building.id} created successfully with default floors & rooms."
        }
    }

    fun createFloor(buildingId: String, floorName: String, floorLevel: Int) {
        viewModelScope.launch {
            if (floorName.isBlank()) {
                errorMessage.value = "Floor name cannot be empty."
                return@launch
            }
            repository.addFloor(buildingId, floorName.trim(), floorLevel)
            statusMessage.value = "Floor '$floorName' added to $buildingId."
        }
    }

    fun deleteFloor(floorId: String) {
        viewModelScope.launch {
            repository.removeFloor(floorId)
            statusMessage.value = "Floor removed successfully."
        }
    }

    fun createRoom(buildingId: String, floorId: String, roomName: String, capacity: Int) {
        viewModelScope.launch {
            if (roomName.isBlank() || capacity <= 0) {
                errorMessage.value = "Please provide valid room name and positive capacity."
                return@launch
            }
            repository.addRoom(buildingId, floorId, roomName.trim().uppercase(), capacity)
            statusMessage.value = "Room $roomName added with $capacity seats."
        }
    }

    fun updateRoomCapacity(roomId: String, newCapacity: Int) {
        viewModelScope.launch {
            if (newCapacity <= 0) {
                errorMessage.value = "Capacity must be greater than 0."
                return@launch
            }
            repository.updateRoomCapacity(roomId, newCapacity)
            statusMessage.value = "Room capacity updated to $newCapacity."
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            repository.removeRoom(roomId)
            statusMessage.value = "Room removed successfully."
        }
    }

    fun assignEmployee(
        employeeId: String,
        name: String,
        department: String,
        designation: String,
        targetSeat: Seat,
        room: OfficeRoom,
        floor: Floor,
        building: Building
    ) {
        viewModelScope.launch {
            val result = repository.assignEmployeeToSeat(
                employeeId, name, department, designation, targetSeat, room, floor, building
            )
            result.onSuccess {
                statusMessage.value = "Employee $name assigned to Seat ${targetSeat.seatNumber}."
            }.onFailure {
                errorMessage.value = it.message ?: "Failed to assign employee."
            }
        }
    }

    fun executeMoveEmployee(
        employee: Employee,
        newSeat: Seat,
        newRoom: OfficeRoom,
        newFloor: Floor,
        newBuilding: Building,
        reason: String
    ) {
        viewModelScope.launch {
            val result = repository.moveEmployee(
                employee, newSeat, newRoom, newFloor, newBuilding, reason
            )
            result.onSuccess {
                statusMessage.value = "Successfully moved ${employee.name} to ${newBuilding.id}/${newFloor.floorName}/${newRoom.roomName}/${newSeat.seatNumber}."
                currentScreen.value = Screen.DASHBOARD
            }.onFailure {
                errorMessage.value = it.message ?: "Failed to move employee."
            }
        }
    }

    fun executeSwapSeats(emp1: Employee, emp2: Employee) {
        viewModelScope.launch {
            val result = repository.swapEmployees(emp1, emp2)
            result.onSuccess {
                statusMessage.value = "Swapped seats between ${emp1.name} and ${emp2.name} successfully."
                currentScreen.value = Screen.DASHBOARD
            }.onFailure {
                errorMessage.value = it.message ?: "Failed to swap employees."
            }
        }
    }

    fun removeEmployee(empId: String) {
        viewModelScope.launch {
            repository.removeEmployee(empId)
            statusMessage.value = "Employee removed and seat vacated."
        }
    }

    fun importCsv(csv: String) {
        viewModelScope.launch {
            val (count, errs) = repository.importEmployeesFromCsv(csv)
            if (errs.isEmpty()) {
                statusMessage.value = "Successfully imported $count employees."
            } else {
                statusMessage.value = "Imported $count employees with ${errs.size} notes: ${errs.firstOrNull()}"
            }
        }
    }

    fun clearMessages() {
        statusMessage.value = null
        errorMessage.value = null
    }
}
