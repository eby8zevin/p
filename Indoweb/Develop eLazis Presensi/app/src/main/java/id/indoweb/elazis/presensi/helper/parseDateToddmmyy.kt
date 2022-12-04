package id.indoweb.elazis.presensi.helper

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun parseDateToddMMyyyy(time: String?): String? {
    val localeID = Locale("in", "ID")
    val inputPattern = "yyyy-MM-dd"
    val outputPattern = "dd MMM yyyy"
    val inputFormat = SimpleDateFormat(inputPattern, localeID)
    val outputFormat = SimpleDateFormat(outputPattern, localeID)
    val date: Date?
    var result: String? = null
    try {
        date = inputFormat.parse(time.toString())
        result = outputFormat.format(date!!)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return result
}