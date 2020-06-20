package fudge

import net.fabricmc.loom.LoomGradleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class FFLoomPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        check(project.pluginManager.hasPlugin("fabric-loom")) {
            "ForgedFlowerLoom must be placed AFTER fabric-loom in the plugins{} block!"
        }
//        project.repositories.jcenter()
        val loom = project.extensions.getByType(LoomGradleExtension::class.java)
        loom.addDecompiler(ForgedFlowerDecompiler(project))
    }
}