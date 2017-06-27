package de.lisaplus.atlas.codegen.external

import de.lisaplus.atlas.codegen.GeneratorBase
import de.lisaplus.atlas.codegen.SingleFileGenarator
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.interf.ICodeGen
import de.lisaplus.atlas.interf.IExternalCodeGen
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static de.lisaplus.atlas.builder.helper.BuildHelper.string2Name

/**
 * This is the published version of SingleFileGenerator. With external template it can be used for own generation
 * tasks
 * Created by eiko on 05.06.17.
 */
class ExtSingleFileGenarator extends SingleFileGenarator implements IExternalCodeGen {
    @Override
    void initTemplateFromFile(String templateFile, TemplateType templateType) {
        template = createTemplateFromFile(templateFile,templateType)
    }

    @Override
    void initTemplateFromResource(String templateResource, TemplateType templateType) {
        template = createTemplateFromResource(templateFile,templateType)
    }

    @Override
    String getDestFileName(Model dataModel, Map<String, String> extraParameters, Type currentType=null) {
        if (extraParameters.destFileName) {
            return extraParameters.destFileName
        }
        else {
            return string2Name("${dataModel.title}.swagger",false)
        }
    }

    @Override
    String getDestDir(Model dataModel, String outputBasePath, Map<String, String> extraParameters,Type currentType=null) {
        if (extraParameters.outputDirExt) {
            return outputBasePath + "/" + extraParameters.outputDirExt
        }
        else {
            return outputBasePath
        }
    }


    Logger getLogger() {
        return l
    }
    private static final Logger l=LoggerFactory.getLogger(ExtSingleFileGenarator.class)
}
