package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Building
import com.example.data.model.Employee
import com.example.data.model.EmployeeMovement
import com.example.data.model.Floor
import com.example.data.model.OfficeRoom
import com.example.data.model.Seat
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings ORDER BY id ASC")
    fun getAllBuildings(): Flow<List<Building>>

    @Query("SELECT * FROM buildings WHERE id = :id LIMIT 1")
    suspend fun getBuildingById(id: String): Building?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: Building)

    @Update
    suspend fun updateBuilding(building: Building)

    @Delete
    suspend fun deleteBuilding(building: Building)
}

@Dao
interface FloorDao {
    @Query("SELECT * FROM floors WHERE buildingId = :buildingId ORDER BY floorLevel ASC")
    fun getFloorsForBuilding(buildingId: String): Flow<List<Floor>>

    @Query("SELECT * FROM floors ORDER BY floorLevel ASC")
    fun getAllFloors(): Flow<List<Floor>>

    @Query("SELECT * FROM floors WHERE id = :id LIMIT 1")
    suspend fun getFloorById(id: String): Floor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloor(floor: Floor)

    @Update
    suspend fun updateFloor(floor: Floor)

    @Delete
    suspend fun deleteFloor(floor: Floor)

    @Query("DELETE FROM floors WHERE id = :floorId")
    suspend fun deleteFloorById(floorId: String)
}

@Dao
interface OfficeRoomDao {
    @Query("SELECT * FROM office_rooms WHERE floorId = :floorId ORDER BY roomName ASC")
    fun getRoomsForFloor(floorId: String): Flow<List<OfficeRoom>>

    @Query("SELECT * FROM office_rooms WHERE buildingId = :buildingId ORDER BY roomName ASC")
    fun getRoomsForBuilding(buildingId: String): Flow<List<OfficeRoom>>

    @Query("SELECT * FROM office_rooms ORDER BY roomName ASC")
    fun getAllRooms(): Flow<List<OfficeRoom>>

    @Query("SELECT * FROM office_rooms WHERE id = :id LIMIT 1")
    suspend fun getRoomById(id: String): OfficeRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: OfficeRoom)

    @Update
    suspend fun updateRoom(room: OfficeRoom)

    @Delete
    suspend fun deleteRoom(room: OfficeRoom)

    @Query("DELETE FROM office_rooms WHERE id = :roomId")
    suspend fun deleteRoomById(roomId: String)
}

@Dao
interface SeatDao {
    @Query("SELECT * FROM seats WHERE roomId = :roomId ORDER BY seatNumber ASC")
    fun getSeatsForRoom(roomId: String): Flow<List<Seat>>

    @Query("SELECT * FROM seats WHERE buildingId = :buildingId ORDER BY seatNumber ASC")
    fun getSeatsForBuilding(buildingId: String): Flow<List<Seat>>

    @Query("SELECT * FROM seats ORDER BY seatNumber ASC")
    fun getAllSeats(): Flow<List<Seat>>

    @Query("SELECT * FROM seats WHERE isOccupied = 0")
    fun getAllVacantSeats(): Flow<List<Seat>>

    @Query("SELECT * FROM seats WHERE id = :id LIMIT 1")
    suspend fun getSeatById(id: String): Seat?

    @Query("SELECT * FROM seats WHERE roomId = :roomId AND isOccupied = 0")
    suspend fun getVacantSeatsInRoom(roomId: String): List<Seat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeat(seat: Seat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeats(seats: List<Seat>)

    @Update
    suspend fun updateSeat(seat: Seat)

    @Query("DELETE FROM seats WHERE id = :seatId")
    suspend fun deleteSeatById(seatId: String)

    @Query("DELETE FROM seats WHERE roomId = :roomId")
    suspend fun deleteSeatsForRoom(roomId: String)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: String): Employee?

    @Query("SELECT * FROM employees WHERE employeeId = :empId LIMIT 1")
    suspend fun getEmployeeByEmpId(empId: String): Employee?

    @Query("SELECT * FROM employees WHERE seatId = :seatId LIMIT 1")
    suspend fun getEmployeeBySeatId(seatId: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: String)
}

@Dao
interface MovementDao {
    @Query("SELECT * FROM movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<EmployeeMovement>>

    @Query("SELECT * FROM movements WHERE employeeId = :empId ORDER BY timestamp DESC")
    fun getMovementsForEmployee(empId: String): Flow<List<EmployeeMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: EmployeeMovement)
}
