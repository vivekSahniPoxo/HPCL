package com.example.hpcl.utils

import java.util.concurrent.BlockingQueue

class SequentialTaskExecutor(private val taskQueue: BlockingQueue<Runnable>) : Thread() {
    override fun run() {
        while (!isInterrupted) {
            val task = taskQueue.take() // Retrieve the next task from the queue (blocks if the queue is empty)
            task.run() // Execute the task
        }
    }

    fun addTask(task: Runnable) {
        taskQueue.put(task) // Add a new task to the queue
    }

    fun shutdown() {
        interrupt() // Stop the executor thread
    }
}