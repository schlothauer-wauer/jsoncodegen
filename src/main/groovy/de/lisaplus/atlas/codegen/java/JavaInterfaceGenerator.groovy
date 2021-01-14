package de.lisaplus.atlas.codegen.java

import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by eiko on 05.06.17.
 */
class JavaInterfaceGenerator extends JavaGeneratorBase {

    private static final Logger log = LoggerFactory.getLogger(JavaInterfaceGenerator.class)

    @Override
    String getDestFileName(Model dataModel, Map<String, String> extraParameters, Type currentType=null) {
        String fileNameBase = firstUpperCase(currentType.name)
        if (currentType.isEnum) {
            return "${ fileNameBase }.java"
        } else {
            return "I${fileNameBase}.java"
        }
    }

    void initTemplate() {
        template = createTemplateFromResource('templates/java/interface.txt', TemplateType.GString)
    }

    Logger getLogger() {
        return log
    }
}
