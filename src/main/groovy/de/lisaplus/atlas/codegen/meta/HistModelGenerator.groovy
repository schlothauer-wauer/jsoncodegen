package de.lisaplus.atlas.codegen.meta

import de.lisaplus.atlas.codegen.SingleFileGenarator
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static de.lisaplus.atlas.builder.helper.BuildHelper.string2Name

/**
 * Created by eiko on 05.06.17.
 */
class HistModelGenerator extends SingleFileGenarator {
    private static final Logger log=LoggerFactory.getLogger(HistModelGenerator.class)

    void initTemplate() {
        template = createTemplateFromResource('templates/meta/hist_model_schema.txt',TemplateType.GString)
    }

    @Override
    String getDestFileName(Model dataModel, Map<String, String> extraParameters, Type currentType=null) {
        if (extraParameters.destFileName) {
            return extraParameters.destFileName
        }
        else {
            return "${string2Name(dataModel.title,false)}_hist.json"
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
        return log
    }
}
