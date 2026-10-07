package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class Building(
    @PrimaryKey val id: String, // e.g., "B-112", "B-144"
    val name: String, // e.g., "B-112 Technology Center"
    val address: String = "Xynotech Corporate Park",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "floors",
    foreignKeys = [
        ForeignKey(
            entity = Building::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingId")]
)
data class Floor(
    @PrimaryKey val id: String, // e.g. "B-112_GF", "B-112_1F"
    val buildingId: String,
    val floorName: String, // e.g. "Ground Floor", "First Floor", "Second Floor"
    val floorLevel: Int, // 0 for Ground, 1 for First, 2 for Second, etc.
    val isActive: Boolean = true
)

@Entity(
    tableName = "office_rooms",
    foreignKeys = [
        ForeignKey(
            entity = Floor::class,
            parentColumns = ["id"],
            childColumns = ["floorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("floorId"), Index("buildingId")]
)
data class OfficeRoom(
    @PrimaryKey val id: String, // e.g. "B-112_G1"
    val floorId: String,
    val buildingId: String,
    val roomName: String, // e.g. "G1", "G2", "F1", "S1"
    val capacity: Int, // e.g. 10
    val wingOrSection: String = "Main Wing",
    val isActive: Boolean = true
)

@Entity(
    tableName = "seats",
    foreignKeys = [
        ForeignKey(
            entity = OfficeRoom::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roomId"), Index("buildingId")]
)
data class Seat(
    @PrimaryKey val id: String, // e.g. "B-112_G1_G1-01"
    val roomId: String,
    val floorId: String,
    val buildingId: String,
    val seatNumber: String, // e.g. "G1-01"
    val isOccupied: Boolean = false,
    val currentEmployeeId: String? = null // FK reference to Employee
)

@Entity(
    tableName = "employees",
    indices = [Index("employeeId", unique = true), Index("seatId", unique = true)]
)
data class Employee(
    @PrimaryKey val id: String, // UUID or XY-1025
    val employeeId: String, // e.g. "XY-1025"
    val name: String, // e.g. "Ahmed Khan"
    val department: String, // e.g. "Finance", "Engineering", "HR"
    val designation: String, // e.g. "Executive", "Senior Architect"
    val buildingId: String, // e.g. "B-112"
    val floorName: String, // e.g. "Ground Floor"
    val floorId: String,
    val roomName: String, // e.g. "G1"
    val roomId: String,
    val seatNumber: String, // e.g. "G1-01"
    val seatId: String,
    val status: String = "Active", // "Active", "Remote", "On Leave"
    val email: String = "",
    val phone: String = "",
    val dateAdded: String = "2026-10-07",
    val lastLocationChange: String = "2026-10-07"
)

@Entity(tableName = "movements")
data class EmployeeMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val fromBuilding: String,
    val fromFloor: String,
    val fromRoom: String,
    val fromSeat: String,
    val toBuilding: String,
    val toFloor: String,
    val toRoom: String,
    val toSeat: String,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = "07 October 2026",
    val reason: String = "Department Reassignment"
)
