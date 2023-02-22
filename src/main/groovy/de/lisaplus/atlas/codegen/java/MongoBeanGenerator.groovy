package de.lisaplus.atlas.codegen.java

import de.lisaplus.atlas.codegen.TemplateType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 05.06.17.
 */
class MongoBeanGenerator extends JavaGeneratorBase {
    private static final Logger log=LoggerFactory.getLogger(MongoBeanGenerator.class)

    void initTemplate() {
        template = createTemplateFromResource('templates/java/mongo_bean.txt',TemplateType.GString)
    }

    Logger getLogger() {
        return log
    }
}
