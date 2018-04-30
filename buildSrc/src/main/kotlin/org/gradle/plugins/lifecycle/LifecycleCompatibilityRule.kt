package org.gradle.plugins.lifecycle

import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.CompatibilityCheckDetails

class LifecycleCompatibilityRule: AttributeCompatibilityRule<Lifecycle> {
    override
    fun execute(details: CompatibilityCheckDetails<Lifecycle>) = details.run {
        val consumerValue = consumerValue!!.name
        val producerValue = producerValue?.name

        when (consumerValue) {
            ALIVE -> when (producerValue) {
                ALIVE -> compatible()
                else -> incompatible()
            }
            DEPRECATED -> when (producerValue) {
                ALIVE -> compatible()
                DEPRECATED -> compatible()
                else -> incompatible()
            }
            BLACKLISTED -> when (producerValue) {
                BLACKLISTED -> compatible()
                else -> incompatible()
            }

        }
    }
}