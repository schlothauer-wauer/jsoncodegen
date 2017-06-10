package de.lisaplus.atlas.codegen.meta

import de.lisaplus.atlas.codegen.SingleFileGenarator
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 05.06.17.
 */
class SwaggerGenerator extends SingleFileGenarator {
    private static final Logger log=LoggerFactory.getLogger(SwaggerGenerator.class)

    @Override
    String getDestFileName(Model dataModel, Map<String, String> extraParameters,Type currentType=null) {
        return null // TODO
    }

    @Override
    String getDestDir(Model dataModel, String outputBasePath, Map<String, String> extraParameters, Type currentType=null) {
        return null // TODO
    }

    Logger getLogger() {
        return log
    }
}
