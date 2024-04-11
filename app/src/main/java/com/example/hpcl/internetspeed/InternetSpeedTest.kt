package com.example.hpcl.internetspeed
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class SpeedTester(private val context: Context) {

    interface SpeedTestListener {
        fun onDownloadSpeedChanged(speedMbps: Double)
    }

    fun measureSpeed(listener: SpeedTestListener) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse("https://speedtest.netgaadi.in/50MB.bin")
        val request = DownloadManager.Request(downloadUri)

        request.setAllowedOverMetered(true)
        request.setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val bytesDownloaded = getBytesDownloaded(downloadManager, downloadId)
            val speedKbps = (bytesDownloaded * 8) / 1000.0
            val speedMbps = speedKbps / 1000.0
            listener.onDownloadSpeedChanged(speedMbps)
        }, 5000)
    }

    @SuppressLint("Range")
    private fun getBytesDownloaded(downloadManager: DownloadManager, downloadId: Long): Long {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        var bytesDownloaded = 0L

        var cursor: Cursor? = null
        try {
            cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    bytesDownloaded =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                }
            }
        } finally {
            cursor?.close()
        }

        return bytesDownloaded
    }
}
