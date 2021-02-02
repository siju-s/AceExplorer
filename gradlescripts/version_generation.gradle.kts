import java.io.FileInputStream
import java.util.*

// Auto generate VERSION_CODE & VERSION_NAME
val versionPropsFile = file("${projectDir}/version.properties")
var code = 1
val versionMajor = 3
val versionMinor = 0
val versionPatch = 0
var version = ""

val devReleaseTask = "assembledevRelease"
val prodReleaseTask = "assembleprodRelease"
val prodReleaseTaskStudio = "assembleProdRelease"
val prodReleaseTaskBundle = "bundleProdRelease"
val prodReleaseTaskBundle2 = "bundleprodRelease"
val devReleaseTaskStudio = "assembleDevRelease"
val genericRelease = "assembleRelease"

if (versionPropsFile.canRead()) {
    val versionProps = Properties()
    versionProps.load(FileInputStream(versionPropsFile))
    val runTasks = gradle.startParameter.taskNames
    var isRelease = false
    for (item in runTasks) {
        if (item.contains(devReleaseTask) || item.contains(prodReleaseTask) || item.contains(genericRelease) ||
                item.contains(devReleaseTaskStudio) || item.contains(prodReleaseTaskStudio) ||
                item.contains(prodReleaseTaskBundle) || item.contains(prodReleaseTaskBundle2)) {
            isRelease = true
            break
        }
    }
    code = versionProps.getProperty("VERSION_CODE").toInt()
    var versionBuild = versionProps.getProperty("VERSION_BUILD").toInt()
    version = versionProps.getProperty("VERSION_NAME")
    if (isRelease) {
        code++
        versionBuild++
        versionProps.setProperty("VERSION_CODE", code.toString())
        versionProps.setProperty("VERSION_BUILD", versionBuild.toString())
        version = StringBuilder().append(versionMajor).append(".").append(versionMinor).append(".").append(versionPatch).append(".").append(versionBuild).toString()
        versionProps.setProperty("VERSION_NAME", version)
        versionProps.store(versionPropsFile.writer(), null)
    }
} else {
    throw GradleException("Could not read version.properties!")
}

extra.apply {
    set("versionCode", code)
    set("versionName", version)
}
