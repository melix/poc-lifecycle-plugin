package org.gradle.plugins.lifecycle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class LifecyclePlugin @Inject constructor(private val objectFactory: ObjectFactory) : Plugin<Project> {

    override
    fun apply(project: Project): Unit = project.run {
        val extension = extensions.create("lifecycle", LifecyleExtension::class.java, true)

        configureSchema()
        configureGraphValidation(extension)
        configureBlacklistedUpgrade(extension)
    }

    private fun Project.configureBlacklistedUpgrade(extension: LifecyleExtension) {
        val blacklistedModules = mutableSetOf<String>()

        dependencies.components.all {
            if (!extension.failOnBlacklisted) {
                val id = id
                findLifecycleAttribute(attributes)?.let {
                    if (it == BLACKLISTED) {
                        blacklistedModules.add(id.toString())
                    }
                }
            }
        }
        configurations.all {
            resolutionStrategy.componentSelection.all {
                if (!extension.failOnBlacklisted) {
                    if (blacklistedModules.contains(candidate.toString())) {
                        reject("Blacklisted module")
                    }
                }
            }
        }
    }

    private
    fun Project.configureSchema() {
        dependencies.attributesSchema.attribute(LIFECYCLE_ATTRIBUTE) {
            compatibilityRules.add(LifecycleCompatibilityRule::class.java)
            disambiguationRules.add(LifecycleDisambiguationRules::class.java) {
                params(lifecycle(ALIVE))
                params(lifecycle(DEPRECATED))
                params(lifecycle(BLACKLISTED))
            }
        }
    }

    private
    fun Project.configureGraphValidation(extension: LifecyleExtension) {
        configurations.all {
            val configName = name
            incoming.afterResolve {
                resolutionResult.allComponents {
                    findLifecycleAttribute(variant.attributes)?.let {
                        if (extension.failOnBlacklisted && it == BLACKLISTED) {
                            throw RuntimeException("Configuration $configName resolved a blacklisted module: $id")
                        }
                        if (it == DEPRECATED) {
                            logger.warn("Configuration $configName resolved a deprecated module: $id")
                        }
                    }
                }
            }
        }
    }

    private
    fun lifecycle(name: String) = objectFactory.named(Lifecycle::class.java, name)

}