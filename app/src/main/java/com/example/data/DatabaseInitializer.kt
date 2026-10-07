package com.example.data

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun populateInitialDataIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        val existingBuildings = db.buildingDao().getBuildingById("B-112")
        if (existingBuildings != null) return@withContext

        // Initial Setup as required by prompt:
        // Building 1: B-112
        // Building 2: B-144
        val b112 = Building(
            id = "B-112",
            name = "B-112 Technology Center",
            address = "Xynotech Tower East, Tech District"
        )
        val b144 = Building(
            id = "B-144",
            name = "B-144 Innovation Hub",
            address = "Xynotech Tower West, Tech District"
        )
        db.buildingDao().insertBuilding(b112)
        db.buildingDao().insertBuilding(b144)

        // Seed B-112 floors: Ground Floor (G1-G5), First Floor (F1-F5), Second Floor (S1)
        seedBuildingStructure(
            db = db,
            buildingId = "B-112",
            groundRooms = listOf("G1" to 10, "G2" to 12, "G3" to 8, "G4" to 10, "G5" to 10),
            firstRooms = listOf("F1" to 12, "F2" to 12, "F3" to 12, "F4" to 12, "F5" to 12),
            secondRooms = listOf("S1" to 40)
        )

        // Seed B-144 floors: Ground Floor (G1-G5), First Floor (F1-F5), Second Floor (S1)
        seedBuildingStructure(
            db = db,
            buildingId = "B-144",
            groundRooms = listOf("G1" to 10, "G2" to 10, "G3" to 10, "G4" to 10, "G5" to 10),
            firstRooms = listOf("F1" to 10, "F2" to 10, "F3" to 10, "F4" to 10, "F5" to 10),
            secondRooms = listOf("S1" to 30)
        )

        val initialEmployees = listOf(
            Triple("Ahmed Khan", "XY-1001", "G1-01" to "Engineering"),
            Triple("Bilal Ahmed", "XY-1002", "G1-02" to "Engineering"),
            Triple("Usman Ali", "XY-1003", "G1-03" to "DevOps"),
            Triple("Hamza Khan", "XY-1004", "G1-04" to "QA"),
            // G1-05 Available
            Triple("Fahad Khan", "XY-1006", "G1-06" to "Product"),
            Triple("Saad Ahmed", "XY-1007", "G1-07" to "Design"),
            Triple("Ali Raza", "XY-1008", "G1-08" to "Engineering"),
            // G1-09 Available
            Triple("Salman Ahmed", "XY-1010", "G1-10" to "Engineering"),

            // G2: Capacity 12, Occupied 12, Vacant 0
            Triple("Zubair Tariq", "XY-1011", "G2-01" to "Marketing"),
            Triple("Kashif Mehmood", "XY-1012", "G2-02" to "Marketing"),
            Triple("Hassan Shah", "XY-1013", "G2-03" to "Sales"),
            Triple("Tariq Jamil", "XY-1014", "G2-04" to "Sales"),
            Triple("Noman Ijaz", "XY-1015", "G2-05" to "Operations"),
            Triple("Shahid Afridi", "XY-1016", "G2-06" to "Operations"),
            Triple("Adnan Sami", "XY-1017", "G2-07" to "HR"),
            Triple("Babar Azam", "XY-1018", "G2-08" to "Leadership"),
            Triple("Shaheen Afridi", "XY-1019", "G2-09" to "Engineering"),
            Triple("Rizwan Ahmed", "XY-1020", "G2-10" to "Engineering"),
            Triple("Fakhar Zaman", "XY-1021", "G2-11" to "DevOps"),
            Triple("Haris Rauf", "XY-1022", "G2-12" to "Infrastructure"),

            // G3: Capacity 8, Occupied 6, Vacant 2
            Triple("Kamran Akmal", "XY-1023", "G3-01" to "Finance"),
            Triple("Umar Akmal", "XY-1024", "G3-02" to "Finance"),
            Triple("Ahmed Khan (Exec)", "XY-1025", "G3-03" to "Finance"),
            Triple("Sarfraz Ahmed", "XY-1026", "G3-04" to "Management"),
            // G3-05 Available
            Triple("Imad Wasim", "XY-1027", "G3-06" to "Legal"),
            Triple("Shadab Khan", "XY-1028", "G3-07" to "Analytics"),
        )

        for ((name, empId, seatData) in initialEmployees) {
            val (seatNum, dept) = seatData
            val roomName = seatNum.substringBefore("-")
            val sId = "B-112_${roomName}_$seatNum"
            val seat = db.seatDao().getSeatById(sId)
            if (seat != null) {
                val emp = Employee(
                    id = "EMP_$empId",
                    employeeId = empId,
                    name = name,
                    department = dept,
                    designation = if (dept == "Leadership") "Director" else if (dept == "Finance") "Executive" else "Senior Specialist",
                    buildingId = "B-112",
                    floorName = "Ground Floor",
                    floorId = "B-112_GF",
                    roomName = roomName,
                    roomId = "B-112_$roomName",
                    seatNumber = seatNum,
                    seatId = sId,
                    status = "Active",
                    email = "${name.lowercase().replace(" ", ".")}@xynotech.com",
                    phone = "+92 300 1234567",
                    dateAdded = "01 October 2026",
                    lastLocationChange = "07 October 2026"
                )
                db.employeeDao().insertEmployee(emp)
                db.seatDao().updateSeat(seat.copy(isOccupied = true, currentEmployeeId = empId))
            }
        }

        seedRemainingEmployeesForRealism(db)
    }

    private suspend fun seedBuildingStructure(
        db: AppDatabase,
        buildingId: String,
        groundRooms: List<Pair<String, Int>>,
        firstRooms: List<Pair<String, Int>>,
        secondRooms: List<Pair<String, Int>>
    ) {
        val gf = Floor(id = "${buildingId}_GF", buildingId = buildingId, floorName = "Ground Floor", floorLevel = 0)
        db.floorDao().insertFloor(gf)
        for ((rName, cap) in groundRooms) {
            val rId = "${buildingId}_${rName}"
            val room = OfficeRoom(id = rId, floorId = gf.id, buildingId = buildingId, roomName = rName, capacity = cap)
            db.roomDao().insertRoom(room)
            val seats = (1..cap).map { i ->
                val sNum = String.format("%s-%02d", rName, i)
                Seat(id = "${rId}_$sNum", roomId = rId, floorId = gf.id, buildingId = buildingId, seatNumber = sNum)
            }
            db.seatDao().insertSeats(seats)
        }

        val ff = Floor(id = "${buildingId}_1F", buildingId = buildingId, floorName = "First Floor", floorLevel = 1)
        db.floorDao().insertFloor(ff)
        for ((rName, cap) in firstRooms) {
            val rId = "${buildingId}_${rName}"
            val room = OfficeRoom(id = rId, floorId = ff.id, buildingId = buildingId, roomName = rName, capacity = cap)
            db.roomDao().insertRoom(room)
            val seats = (1..cap).map { i ->
                val sNum = String.format("%s-%02d", rName, i)
                Seat(id = "${rId}_$sNum", roomId = rId, floorId = ff.id, buildingId = buildingId, seatNumber = sNum)
            }
            db.seatDao().insertSeats(seats)
        }

        val sf = Floor(id = "${buildingId}_2F", buildingId = buildingId, floorName = "Second Floor", floorLevel = 2)
        db.floorDao().insertFloor(sf)
        for ((rName, cap) in secondRooms) {
            val rId = "${buildingId}_${rName}"
            val room = OfficeRoom(id = rId, floorId = sf.id, buildingId = buildingId, roomName = rName, capacity = cap)
            db.roomDao().insertRoom(room)
            val seats = (1..cap).map { i ->
                val sNum = String.format("%s-%02d", rName, i)
                Seat(id = "${rId}_$sNum", roomId = rId, floorId = sf.id, buildingId = buildingId, seatNumber = sNum)
            }
            db.seatDao().insertSeats(seats)
        }
    }

    private suspend fun seedRemainingEmployeesForRealism(db: AppDatabase) {
        val departments = listOf("Engineering", "Product", "Quality Assurance", "Finance", "HR", "Sales", "Customer Success")
        val designations = listOf("Associate", "Software Engineer", "Lead Architect", "Manager", "Product Owner", "Executive")
        val namesList = listOf(
            "Danish Kaneria", "Shoaib Akhtar", "Misbah Ul Haq", "Mohammad Yousuf", "Younis Khan",
            "Inzamam Ul Haq", "Waqar Younis", "Wasim Akram", "Javed Miandad", "Zaheer Abbas",
            "Imran Khan", "Abdul Qadir", "Saeed Anwar", "Aamer Sohail", "Rashid Latif",
            "Moin Khan", "Saqlain Mushtaq", "Mushtaq Ahmed", "Shoaib Malik", "Mohammad Hafeez"
        )

        var empCounter = 1030
        var nameIdx = 0

        for (fNum in 1..5) {
            val rName = "F$fNum"
            val rId = "B-112_$rName"
            val room = db.roomDao().getRoomById(rId) ?: continue
            val seats = db.seatDao().getVacantSeatsInRoom(rId)
            val occupyCount = (seats.size * 0.8).toInt()
            for (i in 0 until occupyCount) {
                if (i >= seats.size || nameIdx >= namesList.size) break
                val seat = seats[i]
                val name = namesList[nameIdx]
                nameIdx = (nameIdx + 1) % namesList.size
                val empId = "XY-${empCounter++}"
                val dept = departments[empCounter % departments.size]
                val desig = designations[empCounter % designations.size]

                val emp = Employee(
                    id = "EMP_$empId",
                    employeeId = empId,
                    name = name,
                    department = dept,
                    designation = desig,
                    buildingId = "B-112",
                    floorName = "First Floor",
                    floorId = "B-112_1F",
                    roomName = rName,
                    roomId = rId,
                    seatNumber = seat.seatNumber,
                    seatId = seat.id,
                    status = "Active",
                    email = "${name.lowercase().replace(" ", ".")}@xynotech.com",
                    phone = "+92 300 ${1000000 + empCounter}",
                    dateAdded = "03 October 2026",
                    lastLocationChange = "05 October 2026"
                )
                db.employeeDao().insertEmployee(emp)
                db.seatDao().updateSeat(seat.copy(isOccupied = true, currentEmployeeId = empId))
            }
        }

        db.movementDao().insertMovement(
            EmployeeMovement(
                employeeId = "XY-1025",
                employeeName = "Ahmed Khan",
                fromBuilding = "B-112",
                fromFloor = "First Floor",
                fromRoom = "F2",
                fromSeat = "F2-07",
                toBuilding = "B-144",
                toFloor = "Ground Floor",
                toRoom = "G3",
                toSeat = "G3-05",
                timestamp = System.currentTimeMillis() - 86400000L,
                formattedDate = "07 October 2026",
                reason = "Executive Transfer"
            )
        )
    }
}
