package de.lisaplus.atlas.codegen.java

import de.lisaplus.atlas.codegen.MultiFileGenarator
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 05.06.17.
 */
abstract class JavaGeneratorBase extends MultiFileGenarator {
    @Override
    String getDestFileName(Model dataModel, Map<String, String> extraParameters, Type currentType=null) {
        String fileNameBase = firstUpperCamelCase(currentType.name)
        return "${fileNameBase}.java"
    }

    @Override
    String getDestDir(Model dataModel, String outputBasePath, Map<String, String> extraParameters, Type currentType=null) {
        String destDirBase = extraParameters.outputDirExt ? outputBasePath + File.separator + extraParameters.outputDirExt : outputBasePath
        // Force linux/unix file separator, JVM can handle that on all plattforms!
        String packageStr = extraParameters.packageName ? extraParameters.packageName.replaceAll('\\.','/') : ''
        String destDirStr = "${destDirBase}${File.separator}${packageStr}"
        File destDir = new File(destDirStr)
        if (!destDir.exists()) destDir.mkdirs()
        return destDirStr
    }

    abstract void initTemplate()
}
