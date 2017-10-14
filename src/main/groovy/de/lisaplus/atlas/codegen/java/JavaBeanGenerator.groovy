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
class JavaBeanGenerator extends JavaGeneratorBase {
    private static final Logger log=LoggerFactory.getLogger(JavaBeanGenerator.class)

    void initTemplate() {
        template = createTemplateFromResource('templates/java/bean.txt',TemplateType.GString)
    }

    Logger getLogger() {
        return log
    }
}
