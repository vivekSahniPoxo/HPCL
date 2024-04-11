package com.example.hpcl.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*



class RfidLogger(private val context: Context) {

    private val fileName = "rfid_log.txt"

    fun logRfidNumber(rfidNumber: String) {
        val currentTime = getCurrentTime()
        val logMessage = "$rfidNumber - $currentTime\n"

        // Append the log to the file
        appendLogToFile(logMessage)
    }

    private fun appendLogToFile(logMessage: String) {
        try {
            // Use the app's private files directory
            val file = context.getFileStreamPath(fileName)
            val fileOutputStream = FileOutputStream(file, true)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)

            outputStreamWriter.append(logMessage)
            outputStreamWriter.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }
}
