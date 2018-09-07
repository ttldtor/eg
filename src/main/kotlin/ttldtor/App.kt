package ttldtor

import arrow.core.Either
import arrow.core.flatMap
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors
import kotlin.math.roundToInt

data class ActivityRecord(val time: String, val coeff: Double)

data class SampleRecord(val kpi: String, val value1: Int, val value2: Int, val activity: Map<String, Double>?)

typealias ErrorString = String
typealias JsonString = String

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

fun calculateActivityCoefficientByTwoRecordsLinearly(currentHours: Double, fromRecord: ActivityRecord, toRecord: ActivityRecord): Double {
    return (currentHours - fromRecord.time.toHours()) * (toRecord.coeff - fromRecord.coeff) /
            (toRecord.time.toHours() - fromRecord.time.toHours()) + fromRecord.coeff
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

    return calculateActivityCoefficientByTwoRecordsLinearly(hours, sorted[from], sorted[to])
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

fun loadSample(sampleFileName: String): Either<ErrorString, String> {
    val sampleFile = File(sampleFileName)

    if (!sampleFile.exists()) {
        return Either.left("File '$sampleFileName' not found")
    }

    if (sampleFile.isDirectory) {
        return Either.left("'$sampleFileName' - directory")
    }

    return Either.right(sampleFile.readText())
}

fun generateBySample(sampleFileName: String): JsonString {
    val random = Random(System.currentTimeMillis())
    val now = LocalDateTime.now()
    val template = """{"time": "${now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}", %s}"""
    val result = loadSample(sampleFileName).flatMap { s ->
        val klaxon = Klaxon()
        val sampleRecordsArray = klaxon.parseArray<SampleRecord>(s)
                ?: return@flatMap Either.left("Can't parse the '$sampleFileName' file")
        val hasActivityFields = sampleRecordsArray.stream().anyMatch { it.activity != null }

        val kpis = sampleRecordsArray.stream().map map2@{ sampleRecord ->
            val newRandomValue = (sampleRecord.value2 - sampleRecord.value1) *
                    random.nextInt(NUMBER_OF_PARTITIONS + 1).toDouble() / NUMBER_OF_PARTITIONS.toDouble()
            val activityCoeff = if (!hasActivityFields) activity(now.hours()) else
                activity(now.hours(), sampleRecord.activity!!.entries.map { ActivityRecord(it.key, it.value) })
            val newValue = activityCoeff * newRandomValue + sampleRecord.value1

            return@map2 """"${sampleRecord.kpi}": ${newValue.roundToInt()}"""
        }.collect(Collectors.joining(", "))

        return@flatMap Either.right(kpis)
    }

    return when (result) {
        is Either.Left -> template.format(""""error": "${result.a}"""")
        is Either.Right -> template.format(result.b)
    }
}

fun generateArrayBySample(sampleFileName: String, interval: Int = 5): JsonString {
    val random = Random(System.currentTimeMillis())
    val now = LocalDateTime.now()
    val start = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
    val template = """{"time": "${now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}", "%s"}"""
    val result = loadSample(sampleFileName).flatMap { s ->
        val klaxon = Klaxon()
        val sampleRecordsArray = klaxon.parseArray<SampleRecord>(s)
                ?: return@flatMap Either.left("Can't parse the '$sampleFileName' file")
        val hasActivityFields = sampleRecordsArray.stream().anyMatch { it.activity != null }
        val v = mutableListOf<JsonString>()

        for (minutes in 0..1440 step interval) {
            val time = start.plusMinutes(minutes.toLong())
            val kpis = sampleRecordsArray.stream().map map2@{ sampleRecord ->
                val newRandomValue = (sampleRecord.value2 - sampleRecord.value1) *
                        random.nextInt(NUMBER_OF_PARTITIONS + 1).toDouble() / NUMBER_OF_PARTITIONS.toDouble()
                val activityCoeff = if (!hasActivityFields) activity(time.hours()) else
                    activity(time.hours(), sampleRecord.activity!!.entries.map { ActivityRecord(it.key, it.value) })
                val newValue = activityCoeff * newRandomValue + sampleRecord.value1

                return@map2 """"${sampleRecord.kpi}": ${newValue.roundToInt()}"""
            }.collect(Collectors.joining(", "))

            v.add("""{"time": "${time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}", $kpis}""")
        }

        val kpiArray = v.joinToString(", ")

        return@flatMap Either.right("[$kpiArray]")
    }

    return when (result) {
        is Either.Left -> template.format("""{"time": "${now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}", "error": "${result.a}"}""")
        is Either.Right -> result.b
    }
}

fun main(args: Array<String>) {
    //println(args.toList())
    Locale.setDefault(Locale.US)

    if (args.isEmpty()) {
        printUsage()

        return
    }

    when (args[0]) {
        "--help" -> printUsage()
        "--sample" -> {
            if (args.size != 2) {
                printUsage()

                return
            }

            println((Parser().parse(StringBuilder(generateBySample(args[1]))) as JsonObject).toJsonString(true))
        }
        "--arrayBySample" -> {
            if (args.size != 2) {
                printUsage()

                return
            }

            println((Parser().parse(StringBuilder(generateArrayBySample(args[1], 5))) as JsonArray<*>).toJsonString(true))
        }
        else -> printUsage()
    }
}