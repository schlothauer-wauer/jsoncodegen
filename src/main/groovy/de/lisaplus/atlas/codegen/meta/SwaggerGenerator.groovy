package de.lisaplus.atlas.codegen.meta

import de.lisaplus.atlas.codegen.SingleFileGenarator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 05.06.17.
 */
class SwaggerGenerator extends SingleFileGenarator {
    private static final Logger log=LoggerFactory.getLogger(SwaggerGenerator.class)

    Logger getLogger() {
        return log
    }
}
