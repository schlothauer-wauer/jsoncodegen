package de.lisaplus.atlas.codegen.java

import de.lisaplus.atlas.codegen.TemplateType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 05.06.17.
 */
class JavaInterfaceGenerator extends JavaGeneratorBase {
    private static final Logger log=LoggerFactory.getLogger(JavaInterfaceGenerator.class)

    void initTemplate() {
        template = createTemplateFromResource('templates/java/java_interface.txt',TemplateType.GString)
    }

    Logger getLogger() {
        return log
    }
}
