package com.example.hpcl.utils

interface DataAdditionListener {
    fun onProgressUpdated(progress: Int)
    fun onInsertionComplete()
}