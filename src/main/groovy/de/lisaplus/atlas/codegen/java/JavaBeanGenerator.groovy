package de.lisaplus.atlas.codegen.java

import de.lisaplus.atlas.codegen.MultiFileGenarator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 05.06.17.
 */
class JavaBeanGenerator extends MultiFileGenarator {
    private static final Logger log=LoggerFactory.getLogger(JavaBeanGenerator.class)


    Logger getLogger() {
        return log
    }
}
