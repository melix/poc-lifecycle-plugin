package org.gradle.plugins.lifecycle

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer

val LIFECYCLE_ATTRIBUTE = Attribute.of("org.gradle.lifecycle", Lifecycle::class.java)

const val ALIVE = "ALIVE"
const val DEPRECATED = "DEPRECATED"
const val BLACKLISTED = "BLACKLISTED"

internal
fun findLifecycleAttribute(attributes: AttributeContainer): String? {
    val lookup = attributes.keySet().find { it.name == LIFECYCLE_ATTRIBUTE.name }
    return if (lookup == null) { null } else {
        attributes.getAttribute(lookup) as String
    }
}

interface Lifecycle : Named {
}