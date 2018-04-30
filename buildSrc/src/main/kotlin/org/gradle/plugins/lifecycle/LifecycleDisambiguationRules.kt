package org.gradle.plugins.lifecycle

import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.MultipleCandidatesDetails

class LifecycleDisambiguationRules(private val alive: Lifecycle,
                                   private val deprecated: Lifecycle,
                                   private val blacklisted: Lifecycle) : AttributeDisambiguationRule<Lifecycle> {

    private val aliveAndDeprecated = setOf(alive, deprecated)

    override
    fun execute(details: MultipleCandidatesDetails<Lifecycle>) = details.run {
        when (consumerValue) {
            alive -> if (candidateValues == aliveAndDeprecated) {
                closestMatch(alive)
            }
            deprecated -> if (candidateValues == aliveAndDeprecated) {
                closestMatch(deprecated)
            }
        }
    }

}