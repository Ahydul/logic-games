package com.example.tfg

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

suspend fun longRunningFunction() {
    // Simulate a long-running task
    delay(2000) // This represents a task that takes 2 seconds to complete
    println("Function completed")
}

fun main() {
    runBlocking {
        try {
            withTimeout(1000L) {
                longRunningFunction()
            }
        } catch (e: TimeoutCancellationException) {
            println("Function timed out and was cancelled")
        }
    }
}
