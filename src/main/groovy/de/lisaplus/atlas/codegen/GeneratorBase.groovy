package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.model.Type
import groovy.text.GStringTemplateEngine
import groovy.text.TemplateEngine
import groovy.text.XmlTemplateEngine
import groovy.text.markup.MarkupTemplateEngine

enum TemplateType {
    GString,
    Xml,
    Markup
}

/**
 * Created by eiko on 05.06.17.
 */
abstract class GeneratorBase {
    File createDir() {
        // TODO
    }

    void genForSingleType(String templateName, TemplateType templateType, Type type) {
        // load template
        TemplateEngine engine
        switch(templateType) {
            case TemplateType.Xml: engine = new XmlTemplateEngine()
                break
            case TemplateType.GString: engine = new GStringTemplateEngine()
                break
            case TemplateType.Markup: engine = new MarkupTemplateEngine()
        }
        // TODO - create Template
    }

}
