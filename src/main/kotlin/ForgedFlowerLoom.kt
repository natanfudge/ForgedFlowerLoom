import fudge.ForgedFlowerTinyJavadocProvider
import fudge.forgedflower.api.IFabricJavadocProvider
import fudge.forgedflower.main.DecompilerContext
import fudge.forgedflower.main.Fernflower
import fudge.forgedflower.main.extern.IBytecodeProvider
import fudge.forgedflower.main.extern.IFernflowerLogger
import fudge.forgedflower.main.extern.IFernflowerPreferences
import fudge.forgedflower.main.extern.IResultSaver
import net.fabricmc.loom.decompilers.fernflower.*
import org.gradle.api.Project
import java.io.File
import java.util.jar.Manifest


class ForgedFlowerDecompiler(project: Project) : AbstractFernFlowerDecompiler(project) {
    override fun fernFlowerExecutor(): Class<ForgedFlowerForkedFFExecutor> = ForgedFlowerForkedFFExecutor::class.java
    override fun name(): String = "ForgedFlower"
}


class ForgedFlowerForkedFFExecutor : AbstractForkedFFExecutor() {
    override fun runFF(
            options: MutableMap<String, Any>,
            libraries: List<File>,
            input: File,
            output: File,
            lineMap: File,
            mappings: File
    ) {
        options[IFabricJavadocProvider.PROPERTY_NAME] =
                ForgedFlowerTinyJavadocProvider(mappings)

        val saver = ThreadSafeResultSaver({ output }) { lineMap }
        val logger = ThreadIDFFLogger()
        val ff = Fernflower(
                IBytecodeProvider { ext, int -> FernFlowerUtils.getBytecode(ext, int) },
                ForgedFlowerThreadSafeResultSaver(saver), options,
                ForgedFlowerThreadIDFFLogger(logger)
        )

        for (library in libraries) {
            ff.structContext.addSpace(library, false)
        }

        ff.structContext.addSpace(input, true)
        ff.decompileContext()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            decompile(args, ForgedFlowerForkedFFExecutor())
        }
    }
}

/**
 * The fabric ThreadSafeResultSaver implements FabricFlower's IResultSaver, and not ForgedFlower's,
 * so we need to convert between the two.
 */
private class ForgedFlowerThreadSafeResultSaver(private val fabricSaver: ThreadSafeResultSaver) : IResultSaver {
    override fun saveFolder(p0: String?) = fabricSaver.saveFolder(p0)
    override fun closeArchive(p0: String?, p1: String?) = fabricSaver.closeArchive(p0, p1)
    override fun copyFile(p0: String?, p1: String?, p2: String?) = fabricSaver.copyFile(p0, p1, p2)
    override fun copyEntry(p0: String?, p1: String?, p2: String?, p3: String?) = fabricSaver.copyEntry(p0, p1, p2, p3)
    override fun saveClassEntry(path: String?, archiveName: String?, qualifiedName: String?, entryName: String?, content: String?) {
        val mapping = if (qualifiedName != null && DecompilerContext.getOption(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING)) {
            DecompilerContext.getBytecodeSourceMapper().originalLinesMapping
        } else null

        fabricSaver.saveClassEntry(path, archiveName, qualifiedName, entryName, content, mapping)
    }


    override fun createArchive(p0: String?, p1: String?, p2: Manifest?) = fabricSaver.createArchive(p0, p1, p2)
    override fun saveClassFile(p0: String?, p1: String?, p2: String?, p3: String?, p4: IntArray?) =
            fabricSaver.saveClassFile(p0, p1, p2, p3, p4)

    override fun saveDirEntry(p0: String?, p1: String?, p2: String?) = fabricSaver.saveDirEntry(p0, p1, p2)


}

private typealias ForgedSeverity = fudge.forgedflower.main.extern.IFernflowerLogger.Severity
private typealias FabricSeverity = org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity

private val ForgedSeverity.fabric: FabricSeverity
    get() = when (this) {
        ForgedSeverity.TRACE -> FabricSeverity.TRACE
        ForgedSeverity.INFO -> FabricSeverity.INFO
        ForgedSeverity.WARN -> FabricSeverity.WARN
        ForgedSeverity.ERROR -> FabricSeverity.ERROR
    }

/**
 * Same thing
 */
private class ForgedFlowerThreadIDFFLogger(private val fabricLogger: ThreadIDFFLogger) : IFernflowerLogger() {
    override fun writeMessage(p0: String?, p1: Severity) = fabricLogger.writeMessage(p0, p1.fabric)
    override fun writeMessage(p0: String?, p1: Severity, p2: Throwable?) = fabricLogger.writeMessage(p0, p1.fabric, p2)
    override fun writeMessage(message: String?, t: Throwable?) = fabricLogger.writeMessage(message, t)
    override fun startWriteClass(className: String?) = fabricLogger.startWriteClass(className)
    override fun setSeverity(severity: Severity) = fabricLogger.setSeverity(severity.fabric)
    override fun endClass() = fabricLogger.endClass()
    override fun endMethod() = fabricLogger.endMethod()
    override fun startMethod(methodName: String?) = fabricLogger.startMethod(methodName)
    override fun endWriteClass() = fabricLogger.endWriteClass()
    override fun startClass(className: String?) = fabricLogger.startClass(className)
    override fun startReadingClass(className: String?) = fabricLogger.startReadingClass(className)
    override fun accepts(severity: Severity): Boolean = fabricLogger.accepts(severity.fabric)
    override fun endReadingClass() = fabricLogger.endReadingClass()
}

