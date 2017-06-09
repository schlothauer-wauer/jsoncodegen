package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.DoCodeGen
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.text.XmlTemplateEngine
import groovy.text.markup.MarkupTemplateEngine
import org.slf4j.Logger

/**
 * it ignores the XML template engine because there is no XML input
 */
enum TemplateType {
    GString,
    Markup
}

/**
 * Created by eiko on 05.06.17.
 */
abstract class GeneratorBase {
    Template template

    void createDir(String dirName) {
        DoCodeGen.prepareOutputBaseDir(dirName)
    }

    private static TemplateEngine getTemplateEngine(TemplateType templateType) {
        switch(templateType) {
            case TemplateType.GString: return new GStringTemplateEngine()
            case TemplateType.Markup: return new MarkupTemplateEngine()
        }
    }

    /**
     * creates a template from a local file
     * @param templateFileName path to template file
     * @param templateType what kind of template is desired
     * @return new created template
     */
    Template createTemplateFromFile (String templateFileName, TemplateType templateType) {
        // load template
        File templateFile = new File(templateFileName);
        if (!templateFile.isFile()) {
            def errorMsg = "given template filename is not file: ${templateFileName}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
        if (logger.isInfoEnabled())
            logger.info("use file template: ${templateFileName}")
        TemplateEngine engine = getTemplateEngine(templateType)
        Reader reader = new FileReader(templateFile);
        return engine.createTemplate(reader);
    }

    /**
     *
     * @param templateResourceName resource in class path that contains the template
     * @param templateType what kind of template is desired
     * @return new created template
     */
    Template createTemplateFromResource (String templateResourceName, TemplateType templateType) {
        // load template
        TemplateEngine engine = getTemplateEngine(templateType)
        ClassLoader cl = engine.getClass().getClassLoader()
        InputStream inputStream = cl.getResourceAsStream(templateResourceName)
        if (inputStream==null) {
            def errorMsg = "given template resource not found: ${templateResourceName}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
        BufferedReader reader = new BufferedReader( new InputStreamReader (inputStream))
        return engine.createTemplate(reader);
    }

    abstract String getDestFileName(Model dataModel, Map<String,String> extraParameters)
    abstract String getDestDir(Model dataModel, Map<String,String> extraParameters)

    abstract Logger getLogger();
}
