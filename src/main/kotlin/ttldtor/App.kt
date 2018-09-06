package ttldtor

import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors
import kotlin.math.roundToInt

data class ActivityRecord(val time: String, val coeff: Double)

data class SampleRecord(val kpi: String, val value1: Int, val value2: Int, val activity: Map<String, Double>?)

fun activity(hours: Double): Double {
    return (1.0 / (1.0 + Math.pow(9.0 - hours, 2.0))
            + 1.0 / (1.0 + Math.pow(12.0 - hours, 2.0))
            + 1.0 / (1.0 + Math.pow(17.0 - hours, 2.0))) * 0.877976
}

fun String.toMinutes(): Int {
    val parts = this.split(':')

    val h = parts[0].toInt(10)
    val m = parts[1].toInt(10)

    return h + 60 * m
}

fun String.toHours(): Double {
    val parts = this.split(':')

    val h = parts[0].toInt(10).toDouble()
    val m = parts[1].toInt(10).toDouble()

    return h + m / 60.0
}

fun List<ActivityRecord>.appendLeftBound(): List<ActivityRecord> {
    if (this.first().time == "00:00") {
        return this
    }

    return listOf(ActivityRecord("00:00", 0.0)) + this
}

fun List<ActivityRecord>.appendRightBound(): List<ActivityRecord> {
    if (this.first().time == "24:00") {
        return this
    }

    return this + ActivityRecord("24:00", 0.0)
}

fun activity(hours: Double, activityRecords: List<ActivityRecord>?): Double {
    if (activityRecords == null || activityRecords.isEmpty()) {
        return 1.0
    }

    val sorted = activityRecords.sortedWith(compareBy { it.time.toMinutes() }).appendLeftBound().appendRightBound()

    val idx = sorted.binarySearchBy(hours) { it.time.toHours() }

    if (idx >= 0) {
        return sorted[idx].coeff
    }

    val to = -(idx + 1)
    val from = to - 1

//    println("$hours $idx $from - $to")

    return (hours - sorted[from].time.toHours()) * (sorted[to].coeff - sorted[from].coeff) /
            (sorted[to].time.toHours() - sorted[from].time.toHours()) + sorted[from].coeff
}

fun LocalDateTime.hours(): Double {
    return hour + minute.toDouble() / 60.0 + second.toDouble() / 3600.0
}

fun printUsage() {
    println("Usage:")
    println("eg --help")
    println("eg --sample <sample file name>")
}

const val NUMBER_OF_MINUTES_IN_A_DAY = 1440
const val NUMBER_OF_PARTITIONS = NUMBER_OF_MINUTES_IN_A_DAY * 2

fun generateBySample(sampleFileName: String) {
    val random = Random(System.currentTimeMillis())
    val now = LocalDateTime.now()

    val f = File(sampleFileName)

    if (!f.exists()) {
        System.err.println("File '$sampleFileName' not found")

        return
    }

    if (f.isDirectory) {
        System.err.println("'$sampleFileName' - directory")

        return
    }

    val klaxon = Klaxon()

    val sampleRecordsArray = klaxon.parseArray<SampleRecord>(f.readText()) ?: return
    val hasActivityFields = sampleRecordsArray.stream().anyMatch { it.activity != null }

    print("""{"time": "${now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}", """)
    val kpis = sampleRecordsArray.stream().map { r: SampleRecord ->
        val newRandomValue = (r.value2 - r.value1) *
                random.nextInt(NUMBER_OF_PARTITIONS + 1).toDouble() / NUMBER_OF_PARTITIONS.toDouble()
        val activityCoeff = if (!hasActivityFields) activity(now.hours()) else
            activity(now.hours(), r.activity!!.entries.map { ActivityRecord(it.key, it.value) })
        val newValue = activityCoeff * newRandomValue + r.value1

        return@map """"${r.kpi}": ${newValue.roundToInt()}"""
    }.collect(Collectors.joining(", "))
    println("""$kpis}""")
}

fun main(args: Array<String>) {
    //println(args.toList())
    Locale.setDefault(Locale.US)

    if (args.isEmpty()) {
        printUsage()

        return
    }

    if (args[0] == "--help") {
        printUsage()

        return
    }

    if (args[0] == "--sample") {
        if (args.size != 2) {
            printUsage()

            return
        }

        generateBySample(args[1])

        return
    }

    printUsage()
}