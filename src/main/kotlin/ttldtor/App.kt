package ttldtor

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt
import kotlin.ranges.*

fun getHost(args: Array<String>, index: Int = 0, defaultValue: String = "localhost"): String {
    if (args.size < index + 1) {
        return defaultValue
    }

    return args[index]
}

fun getDateTime(args: Array<String>, index: Int = 1): LocalDateTime {
    val now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)

    if (args.size < index + 1) {
        return now
    }

    if (args[1] == "now") {
        return now
    }

    return try {
        LocalDateTime.of(LocalDate.parse(args[index], DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.MIN)
    } catch (e: RuntimeException) {
        now
    }
}

fun getInt(args: Array<String>, index: Int = 2, defaultValue: Int = 5): Int {
    if (args.size < index + 1) {
        return defaultValue
    }

    return args[index].toIntOrNull()?:defaultValue
}

fun getDouble(args: Array<String>, index: Int = 3, defaultValue: Double = 0.0): Double {
    if (args.size < index + 1) {
        return defaultValue
    }

    return args[index].toDoubleOrNull()?:defaultValue
}

fun getType(args: Array<String>, index: Int = 4, defaultValue: String = "double"): String {
    if (args.size < index + 1) {
        return defaultValue
    }

    val possibleValues = setOf("int", "double")

    val arg = args[index]

    if (possibleValues.contains(arg)) {
        return arg
    }

    return defaultValue
}

fun activity(hours: Double): Double {
    return (1.0 / (1.0 + Math.pow(9.0 - hours, 2.0))
            + 1.0 / (1.0 + Math.pow(12.0 - hours, 2.0))
            + 1.0 / (1.0 + Math.pow(17.0 - hours, 2.0)))*0.8777
}

fun LocalDateTime.hours(): Double {
    return hour + minute.toDouble() / 60.0 + second.toDouble() / 3600.0
}

fun printUsage() {
    println("Usage:")
    println("eg [[<host>] [<DateTime {yyyy-mm-dd}> or now] [<interval>] [<type: int or double>] [<minValue>] [<maxValue>]]")
    println("Examples:")
    println("eg localhost now 5 int 0 5")
    println("eg localhost 2018-08-11 1 double 500.0 2000.0")
}

fun main(args: Array<String>) {
    //println(args.toList())
    Locale.setDefault(Locale.US)

    if (args.isNotEmpty() && args[0] == "--help") {
        printUsage()

        return
    }

    val host = getHost(args, 0)
    val dateTime = getDateTime(args, 1)
    val interval = getInt(args, 2, 5)

    val type = getType(args, 3)

    val minValue = getDouble(args, 4, 0.0)
    val maxValue = getDouble(args, 5, 100.0)

    val r = Random(System.currentTimeMillis())
    println("host, timestamp, value")
    for (i in 0..1439 step interval) {
        val newDateTime = dateTime.plusMinutes(i.toLong())
        val newRandomValue = (maxValue - minValue) * r.nextInt(1001).toDouble() / 1000.0
        val newValue = activity(newDateTime.hours()) * newRandomValue + minValue

        if (type == "double") {
            println("%s, %s, %.2f".format(
                    host,
                    newDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    newValue))
        } else {
            println("%s, %s, %d".format(
                    host,
                    newDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    newValue.roundToInt()))
        }
    }
}