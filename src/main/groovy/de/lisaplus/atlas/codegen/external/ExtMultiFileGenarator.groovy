package de.lisaplus.atlas.codegen.external

import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.codegen.MultiFileGenarator
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.interf.ICodeGen
import de.lisaplus.atlas.interf.IExternalCodeGen
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base class for generators that creates more than one file ... for instance JavaBeanGenerator
 * Created by eiko on 05.06.17.
 */
class ExtMultiFileGenarator extends MultiFileGenarator implements IExternalCodeGen {
    @Override
    void initTemplateFromFile(String templateFile, TemplateType templateType) {
        template = createTemplateFromFile(templateFile,templateType)
    }

    @Override
    void initTemplateFromResource(String templateResource, TemplateType templateType) {
        template = createTemplateFromResource(templateFile,templateType)
    }

    @Override
    String getDestFileName(Model dataModel, Map<String, String> extraParameters,Type currentType=null) {
        def destFileNameExt = extraParameters['destFileNameExt']
        def destFileNamePre = extraParameters['destFileNamePre']
        def destFileCamelCaseName = extraParameters['destFileCamelCaseName']
        def destName = destFileCamelCaseName ? firstUpperCamelCase(currentType.name) : firstUpperCase(currentType.name)
        if (destFileNameExt) {
            destName = destName + "." + destFileNameExt
        }
        if (destFileNamePre) {
            destName = destFileNamePre + destName;
        }
        return destName
    }

    @Override
    String getDestDir(Model dataModel, String outputBasePath, Map<String, String> extraParameters, Type currentType=null) {
        return outputBasePath
    }

    Logger getLogger() {
        return l
    }
    private static final Logger l=LoggerFactory.getLogger(ExtMultiFileGenarator.class)
}
