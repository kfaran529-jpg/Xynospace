package com.example

import com.example.data.model.Building
import com.example.data.model.Employee
import com.example.data.model.EmployeeMovement
import com.example.data.model.Floor
import com.example.data.model.OfficeRoom
import com.example.data.model.Seat
import com.example.ui.screens.exportEmployeesCsv
import com.example.ui.screens.generateCsvReport
import com.example.viewmodel.BuildingSummary
import com.example.viewmodel.FloorSummary
import com.example.viewmodel.RoomSummary
import org.junit.Assert.*
import org.junit.Test

class XynoSpaceLogicTest {

    @Test
    fun testOccupancyCalculation() {
        val capacity = 10
        val occupied = 7
        val vacant = capacity - occupied
        val rate = (occupied.toFloat() / capacity.toFloat()) * 100f

        assertEquals(3, vacant)
        assertEquals(70.0f, rate, 0.01f)
    }

    @Test
    fun testCsvReportGeneration() {
        val b = Building("B-112", "Technology Center")
        val f = Floor("B-112_GF", "B-112", "Ground Floor", 0)
        val r = OfficeRoom("B-112_G1", f.id, b.id, "G1", 10)
        val roomSummary = RoomSummary(r, 10, 8, 2, 80.0f)
        val floorSummary = FloorSummary(f, listOf(roomSummary), 10, 8, 2, 80.0f)
        val bSummary = BuildingSummary(b, listOf(floorSummary), 1, 10, 8, 2, 80.0f)

        val csv = generateCsvReport(bSummary)
        assertTrue(csv.contains("Ground Floor,1,10,8,2,80.0%"))
        assertTrue(csv.contains("TOTAL,1,10,8,2,80.0%"))
    }

    @Test
    fun testEmployeeCsvExport() {
        val emp = Employee(
            id = "EMP_1",
            employeeId = "XY-1025",
            name = "Ahmed Khan",
            department = "Finance",
            designation = "Executive",
            buildingId = "B-112",
            floorName = "First Floor",
            floorId = "B-112_1F",
            roomName = "F2",
            roomId = "B-112_F2",
            seatNumber = "F2-07",
            seatId = "B-112_F2_F2-07"
        )
        val csv = exportEmployeesCsv(listOf(emp))
        assertTrue(csv.contains("XY-1025,Ahmed Khan,Finance,Executive,B-112,First Floor,F2,F2-07"))
    }
}
