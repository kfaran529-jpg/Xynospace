package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class XynoSpaceRepository(private val db: AppDatabase) {

    val allBuildings: Flow<List<Building>> = db.buildingDao().getAllBuildings()
    val allFloors: Flow<List<Floor>> = db.floorDao().getAllFloors()
    val allRooms: Flow<List<OfficeRoom>> = db.roomDao().getAllRooms()
    val allSeats: Flow<List<Seat>> = db.seatDao().getAllSeats()
    val allEmployees: Flow<List<Employee>> = db.employeeDao().getAllEmployees()
    val allMovements: Flow<List<EmployeeMovement>> = db.movementDao().getAllMovements()

    fun getFloorsForBuilding(buildingId: String): Flow<List<Floor>> =
        db.floorDao().getFloorsForBuilding(buildingId)

    fun getRoomsForFloor(floorId: String): Flow<List<OfficeRoom>> =
        db.roomDao().getRoomsForFloor(floorId)

    fun getSeatsForRoom(roomId: String): Flow<List<Seat>> =
        db.seatDao().getSeatsForRoom(roomId)

    suspend fun getBuildingById(id: String): Building? = db.buildingDao().getBuildingById(id)
    suspend fun getFloorById(id: String): Floor? = db.floorDao().getFloorById(id)
    suspend fun getRoomById(id: String): OfficeRoom? = db.roomDao().getRoomById(id)
    suspend fun getSeatById(id: String): Seat? = db.seatDao().getSeatById(id)
    suspend fun getEmployeeByEmpId(empId: String): Employee? = db.employeeDao().getEmployeeByEmpId(empId)
    suspend fun getEmployeeBySeatId(seatId: String): Employee? = db.employeeDao().getEmployeeBySeatId(seatId)

    suspend fun addBuilding(building: Building, defaultFloors: Boolean = true) = withContext(Dispatchers.IO) {
        db.buildingDao().insertBuilding(building)
        if (defaultFloors) {
            val bId = building.id
            // Ground Floor: G1-G5
            val gf = Floor(id = "${bId}_GF", buildingId = bId, floorName = "Ground Floor", floorLevel = 0)
            db.floorDao().insertFloor(gf)
            val gfRooms = listOf("G1", "G2", "G3", "G4", "G5")
            for (rName in gfRooms) {
                val rId = "${bId}_${rName}"
                val room = OfficeRoom(id = rId, floorId = gf.id, buildingId = bId, roomName = rName, capacity = 10)
                db.roomDao().insertRoom(room)
                val seats = (1..10).map { i ->
                    val sNum = String.format("%s-%02d", rName, i)
                    Seat(id = "${bId}_${rName}_$sNum", roomId = rId, floorId = gf.id, buildingId = bId, seatNumber = sNum)
                }
                db.seatDao().insertSeats(seats)
            }

            // First Floor: F1-F5
            val ff = Floor(id = "${bId}_1F", buildingId = bId, floorName = "First Floor", floorLevel = 1)
            db.floorDao().insertFloor(ff)
            val ffRooms = listOf("F1", "F2", "F3", "F4", "F5")
            for (rName in ffRooms) {
                val rId = "${bId}_${rName}"
                val room = OfficeRoom(id = rId, floorId = ff.id, buildingId = bId, roomName = rName, capacity = 10)
                db.roomDao().insertRoom(room)
                val seats = (1..10).map { i ->
                    val sNum = String.format("%s-%02d", rName, i)
                    Seat(id = "${bId}_${rName}_$sNum", roomId = rId, floorId = ff.id, buildingId = bId, seatNumber = sNum)
                }
                db.seatDao().insertSeats(seats)
            }

            // Second Floor: S1
            val sf = Floor(id = "${bId}_2F", buildingId = bId, floorName = "Second Floor", floorLevel = 2)
            db.floorDao().insertFloor(sf)
            val s1Id = "${bId}_S1"
            val s1Room = OfficeRoom(id = s1Id, floorId = sf.id, buildingId = bId, roomName = "S1", capacity = 10)
            db.roomDao().insertRoom(s1Room)
            val s1Seats = (1..10).map { i ->
                val sNum = String.format("S1-%02d", i)
                Seat(id = "${bId}_S1_$sNum", roomId = s1Id, floorId = sf.id, buildingId = bId, seatNumber = sNum)
            }
            db.seatDao().insertSeats(s1Seats)
        }
    }

    suspend fun updateBuilding(building: Building) = withContext(Dispatchers.IO) {
        db.buildingDao().updateBuilding(building)
    }

    suspend fun addFloor(buildingId: String, floorName: String, floorLevel: Int): Floor = withContext(Dispatchers.IO) {
        val sanitized = floorName.replace(" ", "")
        val floorId = "${buildingId}_${sanitized}_${System.currentTimeMillis() % 10000}"
        val floor = Floor(id = floorId, buildingId = buildingId, floorName = floorName, floorLevel = floorLevel)
        db.floorDao().insertFloor(floor)
        floor
    }

    suspend fun removeFloor(floorId: String) = withContext(Dispatchers.IO) {
        db.floorDao().deleteFloorById(floorId)
    }

    suspend fun addRoom(buildingId: String, floorId: String, roomName: String, capacity: Int): OfficeRoom = withContext(Dispatchers.IO) {
        val roomId = "${buildingId}_${roomName}_${System.currentTimeMillis() % 10000}"
        val room = OfficeRoom(id = roomId, floorId = floorId, buildingId = buildingId, roomName = roomName, capacity = capacity)
        db.roomDao().insertRoom(room)

        val seats = (1..capacity).map { i ->
            val sNum = String.format("%s-%02d", roomName, i)
            Seat(id = "${roomId}_$sNum", roomId = roomId, floorId = floorId, buildingId = buildingId, seatNumber = sNum)
        }
        db.seatDao().insertSeats(seats)
        room
    }

    suspend fun updateRoomCapacity(roomId: String, newCapacity: Int) = withContext(Dispatchers.IO) {
        val room = db.roomDao().getRoomById(roomId) ?: return@withContext
        if (newCapacity == room.capacity) return@withContext

        if (newCapacity > room.capacity) {
            val seatsToAdd = ((room.capacity + 1)..newCapacity).map { i ->
                val sNum = String.format("%s-%02d", room.roomName, i)
                Seat(id = "${room.id}_$sNum", roomId = room.id, floorId = room.floorId, buildingId = room.buildingId, seatNumber = sNum)
            }
            db.seatDao().insertSeats(seatsToAdd)
        }
        db.roomDao().updateRoom(room.copy(capacity = newCapacity))
    }

    suspend fun removeRoom(roomId: String) = withContext(Dispatchers.IO) {
        db.roomDao().deleteRoomById(roomId)
    }

    suspend fun assignEmployeeToSeat(
        employeeId: String,
        name: String,
        department: String,
        designation: String,
        targetSeat: Seat,
        room: OfficeRoom,
        floor: Floor,
        building: Building
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Validation 1: Seat already occupied?
        val currentSeat = db.seatDao().getSeatById(targetSeat.id)
        if (currentSeat?.isOccupied == true) {
            return@withContext Result.failure(Exception("Seat ${targetSeat.seatNumber} is already occupied!"))
        }

        // Validation 2: Check if employee already exists and has a seat
        val existingEmp = db.employeeDao().getEmployeeByEmpId(employeeId)
        val todayStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

        if (existingEmp != null) {
            // Free old seat
            val oldSeat = db.seatDao().getSeatById(existingEmp.seatId)
            if (oldSeat != null) {
                db.seatDao().updateSeat(oldSeat.copy(isOccupied = false, currentEmployeeId = null))
            }
            // Record movement
            val movement = EmployeeMovement(
                employeeId = existingEmp.employeeId,
                employeeName = existingEmp.name,
                fromBuilding = existingEmp.buildingId,
                fromFloor = existingEmp.floorName,
                fromRoom = existingEmp.roomName,
                fromSeat = existingEmp.seatNumber,
                toBuilding = building.id,
                toFloor = floor.floorName,
                toRoom = room.roomName,
                toSeat = targetSeat.seatNumber,
                timestamp = System.currentTimeMillis(),
                formattedDate = todayStr,
                reason = "Seat Reassignment"
            )
            db.movementDao().insertMovement(movement)

            // Update employee
            val updated = existingEmp.copy(
                name = name,
                department = department,
                designation = designation,
                buildingId = building.id,
                floorName = floor.floorName,
                floorId = floor.id,
                roomName = room.roomName,
                roomId = room.id,
                seatNumber = targetSeat.seatNumber,
                seatId = targetSeat.id,
                lastLocationChange = todayStr
            )
            db.employeeDao().updateEmployee(updated)
        } else {
            // New Employee
            val newEmp = Employee(
                id = UUID.randomUUID().toString(),
                employeeId = employeeId,
                name = name,
                department = department,
                designation = designation,
                buildingId = building.id,
                floorName = floor.floorName,
                floorId = floor.id,
                roomName = room.roomName,
                roomId = room.id,
                seatNumber = targetSeat.seatNumber,
                seatId = targetSeat.id,
                status = "Active",
                dateAdded = todayStr,
                lastLocationChange = todayStr
            )
            db.employeeDao().insertEmployee(newEmp)
        }

        // Occupy new seat
        db.seatDao().updateSeat(targetSeat.copy(isOccupied = true, currentEmployeeId = employeeId))
        Result.success(Unit)
    }

    suspend fun moveEmployee(
        employee: Employee,
        newSeat: Seat,
        newRoom: OfficeRoom,
        newFloor: Floor,
        newBuilding: Building,
        reason: String = "Location Transfer"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Validate target seat is vacant
        val targetSeatFresh = db.seatDao().getSeatById(newSeat.id)
        if (targetSeatFresh == null || targetSeatFresh.isOccupied) {
            return@withContext Result.failure(Exception("Target seat ${newSeat.seatNumber} is not available!"))
        }

        // 1. Vacate old seat
        val oldSeat = db.seatDao().getSeatById(employee.seatId)
        if (oldSeat != null) {
            db.seatDao().updateSeat(oldSeat.copy(isOccupied = false, currentEmployeeId = null))
        }

        // 2. Mark new seat occupied
        db.seatDao().updateSeat(newSeat.copy(isOccupied = true, currentEmployeeId = employee.employeeId))

        // 3. Record movement history
        val todayStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        val movement = EmployeeMovement(
            employeeId = employee.employeeId,
            employeeName = employee.name,
            fromBuilding = employee.buildingId,
            fromFloor = employee.floorName,
            fromRoom = employee.roomName,
            fromSeat = employee.seatNumber,
            toBuilding = newBuilding.id,
            toFloor = newFloor.floorName,
            toRoom = newRoom.roomName,
            toSeat = newSeat.seatNumber,
            timestamp = System.currentTimeMillis(),
            formattedDate = todayStr,
            reason = reason
        )
        db.movementDao().insertMovement(movement)

        // 4. Update employee record
        val updatedEmployee = employee.copy(
            buildingId = newBuilding.id,
            floorName = newFloor.floorName,
            floorId = newFloor.id,
            roomName = newRoom.roomName,
            roomId = newRoom.id,
            seatNumber = newSeat.seatNumber,
            seatId = newSeat.id,
            lastLocationChange = todayStr
        )
        db.employeeDao().updateEmployee(updatedEmployee)

        Result.success(Unit)
    }

    suspend fun swapEmployees(emp1: Employee, emp2: Employee): Result<Unit> = withContext(Dispatchers.IO) {
        val seat1 = db.seatDao().getSeatById(emp1.seatId)
        val seat2 = db.seatDao().getSeatById(emp2.seatId)

        if (seat1 == null || seat2 == null) {
            return@withContext Result.failure(Exception("One or both seats could not be found for swapping."))
        }

        val todayStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

        // Record movement for emp1
        db.movementDao().insertMovement(
            EmployeeMovement(
                employeeId = emp1.employeeId,
                employeeName = emp1.name,
                fromBuilding = emp1.buildingId,
                fromFloor = emp1.floorName,
                fromRoom = emp1.roomName,
                fromSeat = emp1.seatNumber,
                toBuilding = emp2.buildingId,
                toFloor = emp2.floorName,
                toRoom = emp2.roomName,
                toSeat = emp2.seatNumber,
                timestamp = System.currentTimeMillis(),
                formattedDate = todayStr,
                reason = "Mutual Seat Swap with ${emp2.name}"
            )
        )

        // Record movement for emp2
        db.movementDao().insertMovement(
            EmployeeMovement(
                employeeId = emp2.employeeId,
                employeeName = emp2.name,
                fromBuilding = emp2.buildingId,
                fromFloor = emp2.floorName,
                fromRoom = emp2.roomName,
                fromSeat = emp2.seatNumber,
                toBuilding = emp1.buildingId,
                toFloor = emp1.floorName,
                toRoom = emp1.roomName,
                toSeat = emp1.seatNumber,
                timestamp = System.currentTimeMillis() + 1,
                formattedDate = todayStr,
                reason = "Mutual Seat Swap with ${emp1.name}"
            )
        )

        // Update Seats
        db.seatDao().updateSeat(seat1.copy(currentEmployeeId = emp2.employeeId))
        db.seatDao().updateSeat(seat2.copy(currentEmployeeId = emp1.employeeId))

        // Update Employees
        val updatedEmp1 = emp1.copy(
            buildingId = emp2.buildingId,
            floorName = emp2.floorName,
            floorId = emp2.floorId,
            roomName = emp2.roomName,
            roomId = emp2.roomId,
            seatNumber = emp2.seatNumber,
            seatId = emp2.seatId,
            lastLocationChange = todayStr
        )

        val updatedEmp2 = emp2.copy(
            buildingId = emp1.buildingId,
            floorName = emp1.floorName,
            floorId = emp1.floorId,
            roomName = emp1.roomName,
            roomId = emp1.roomId,
            seatNumber = emp1.seatNumber,
            seatId = emp1.seatId,
            lastLocationChange = todayStr
        )

        db.employeeDao().updateEmployee(updatedEmp1)
        db.employeeDao().updateEmployee(updatedEmp2)

        Result.success(Unit)
    }

    suspend fun removeEmployee(empId: String) = withContext(Dispatchers.IO) {
        val emp = db.employeeDao().getEmployeeByEmpId(empId) ?: return@withContext
        val seat = db.seatDao().getSeatById(emp.seatId)
        if (seat != null) {
            db.seatDao().updateSeat(seat.copy(isOccupied = false, currentEmployeeId = null))
        }
        db.employeeDao().deleteEmployeeById(emp.id)
    }

    suspend fun importEmployeesFromCsv(csvContent: String): Pair<Int, List<String>> = withContext(Dispatchers.IO) {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return@withContext 0 to listOf("Empty CSV")

        var importedCount = 0
        val errors = mutableListOf<String>()

        val startIndex = if (lines.first().contains("Employee ID", ignoreCase = true) || lines.first().contains("id", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i]
            val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (tokens.size < 8) {
                errors.add("Line ${i + 1}: Insufficient columns (expected 8 columns)")
                continue
            }
            val empId = tokens[0]
            val empName = tokens[1]
            val dept = tokens[2]
            val desig = tokens[3]
            val bId = tokens[4]
            val floorName = tokens[5]
            val roomName = tokens[6]
            val seatNum = tokens[7]

            var building = db.buildingDao().getBuildingById(bId)
            if (building == null) {
                building = Building(id = bId, name = "$bId Technology Office")
                addBuilding(building, defaultFloors = true)
            }

            val rId = "${bId}_${roomName}"
            var room = db.roomDao().getRoomById(rId)
            val fId = "${bId}_GF"
            if (room == null) {
                room = OfficeRoom(id = rId, floorId = fId, buildingId = bId, roomName = roomName, capacity = 10)
                db.roomDao().insertRoom(room)
            }

            val sId = "${rId}_${seatNum}"
            var seat = db.seatDao().getSeatById(sId)
            if (seat == null) {
                seat = Seat(id = sId, roomId = rId, floorId = room.floorId, buildingId = bId, seatNumber = seatNum)
                db.seatDao().insertSeat(seat)
            }

            if (seat.isOccupied) {
                errors.add("Line ${i + 1} ($empName): Seat $seatNum in $roomName is already occupied")
                continue
            }

            val todayStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            val employee = Employee(
                id = UUID.randomUUID().toString(),
                employeeId = empId,
                name = empName,
                department = dept,
                designation = desig,
                buildingId = bId,
                floorName = floorName,
                floorId = room.floorId,
                roomName = roomName,
                roomId = room.id,
                seatNumber = seatNum,
                seatId = seat.id,
                dateAdded = todayStr,
                lastLocationChange = todayStr
            )

            db.employeeDao().insertEmployee(employee)
            db.seatDao().updateSeat(seat.copy(isOccupied = true, currentEmployeeId = empId))
            importedCount++
        }

        importedCount to errors
    }
}
