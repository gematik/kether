import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ConvertAbiTask : DefaultTask() {
    @InputFiles
    val inputDir = project.file("src/main/kotlin")

    @TaskAction
    fun convert() {
        val abiFiles = collectAbiFiles(inputDir)
        abiFiles.forEach {
            val outfile = File(it.absolutePath.dropLast(4) + ".kt")
            outfile.writeText("Contract goes here!")
        }
    }

}

private fun collectAbiFiles(file: File) : List<File> {
    val list = mutableListOf<File>()
    list.addAbiFiles(file)
    return list
}

@OptIn(ExperimentalStdlibApi::class)
private fun MutableList<File>.addAbiFiles(file: File){
    file.listFiles()?.forEach {
        when {
            it.isDirectory -> addAbiFiles(it)
            else -> if (it.name.lowercase().endsWith(".abi")) add(it)
        }
    }
}
