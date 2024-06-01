package com.example.tfg.common.utils

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher

class CustomTestWatcher : TestWatcher {

    override fun testSuccessful(context: ExtensionContext?) {
        context?.let {
            val testDisplayName = it.displayName
            println("Test $testDisplayName was successful.")
        }
    }
}