package org.gradle.plugins.lifecycle

open class LifecyleExtension(var failOnBlacklisted: Boolean) {
    fun skipBlacklisted() {
        failOnBlacklisted = false
    }
}