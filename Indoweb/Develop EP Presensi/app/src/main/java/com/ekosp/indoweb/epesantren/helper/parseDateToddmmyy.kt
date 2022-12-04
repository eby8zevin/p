package com.ekosp.indoweb.epesantren.helper

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun parseDateToddMMyyyy(time: String?): String? {
    val indonesia = Locale("in", "ID")
    val inputPattern = "yyyy-MM-dd"
    val outputPattern = "dd MMM yyyy"
    val inputFormat = SimpleDateFormat(inputPattern, indonesia)
    val outputFormat = SimpleDateFormat(outputPattern, indonesia)
    val date: Date?
    var str: String? = null
    try {
        date = inputFormat.parse(time.toString())
        str = outputFormat.format(date!!)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return str
}