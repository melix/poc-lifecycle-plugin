import org.gradle.plugins.lifecycle.LIFECYCLE_ATTRIBUTE
import org.gradle.plugins.lifecycle.Lifecycle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

plugins {
    `build-scan`
    `java-library`
    id("org.gradle.plugins.lifecycle")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    publishAlways()
}

val all by cliOption()
val skip by cliOption()
val forceA by cliOption()
val forceB by cliOption()

lifecycle {
    skip?.let {
        skipBlacklisted()
    }
}

configurations.compileClasspath.attributes {
    all?.let {
        attribute(LIFECYCLE_ATTRIBUTE, objects.named(Lifecycle::class.java, it))
    }
}

dependencies {
    implementation("com.acme:testA:[1,)") {
        forceA?.let { status ->
            attributes {
                attribute(LIFECYCLE_ATTRIBUTE, objects.named(Lifecycle::class.java, status))
            }
        }
    }
    implementation("com.acme:testB:[1,)") {
        forceB?.let { status ->
            attributes {
                attribute(LIFECYCLE_ATTRIBUTE, objects.named(Lifecycle::class.java, status))
            }
        }
    }
}

// Below is just helpers for the sake of the demo

tasks.create("resolveDependencies") {
    group = "Dependency resolution features"
    doLast {
        println("Asking for modules with lifecycle=${all ?: "any"}")
        if (skip == null) {
            println("Blacklisted versions will fail resolution")
        } else {
            println("Blacklisted versions will be rejected")
        }
        configurations.compileClasspath.incoming.resolutionResult.run {
            allComponents {
                if (id is ModuleComponentIdentifier) {
                    println("Resolved variant $this with attributes ${variant.attributes}")
                }
            }
            allDependencies {
                if (this is UnresolvedDependencyResult) {
                    println("Unresolved dependency : $this")
                }
            }
        }
    }
}

inline fun <reified T> attribute(name: String): Attribute<T> = Attribute.of(name, T::class.java)

fun cliOption(): ReadOnlyProperty<Project, String?> = object : ReadOnlyProperty<Project, String?> {
    override
    fun getValue(thisRef: Project, property: KProperty<*>): String? =
            thisRef.findProperty(property.name) as String?
}

defaultTasks("resolveDependencies")

apply {
    from("repositories.gradle.kts")
    from("list-versions.gradle.kts")
}

pluginManager.withPlugin("idea") {
    lifecycle {
        skipBlacklisted() // make sure IDE import doesn't fail
    }
}
