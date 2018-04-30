/**
 * This file uses lots of internal APIs, for the sake of a DEMO. Don't do this at home!
 */
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.ModuleMetadataParser
import org.gradle.api.internal.model.NamedObjectInstantiator
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier
import org.gradle.internal.component.external.model.DefaultMutableMavenModuleResolveMetadata
import org.gradle.internal.resource.local.LocalFileStandInExternalResource
import org.gradle.kotlin.dsl.support.serviceOf

project.run {
    val modules = mutableListOf<DefaultMutableMavenModuleResolveMetadata>()
    file("repo").walkTopDown().forEach {
        if (it.name.endsWith(".module")) {
            val metadataParser = ModuleMetadataParser(serviceOf(), serviceOf(), NamedObjectInstantiator.INSTANCE)
            val id = DefaultModuleVersionIdentifier.newId("com.acme", it.parentFile.parentFile.name, it.parentFile.name)
            val metadata = DefaultMutableMavenModuleResolveMetadata(
                    id,
                    DefaultModuleComponentIdentifier.newId(id),
                    mutableListOf(),
                    serviceOf(),
                    NamedObjectInstantiator.INSTANCE,
                    true
            )
            metadataParser.parse(LocalFileStandInExternalResource(it, serviceOf()), metadata)
            modules.add(metadata)
        }
    }
    println("Module versions")
    modules.groupBy { it.id.module }.forEach { module, versions ->
        println("  - $module")
        versions.sortedByDescending { it.id.version }.forEach {
            println("    - Version ${it.id.version} has attributes ${it.attributes}")
        }
    }
}