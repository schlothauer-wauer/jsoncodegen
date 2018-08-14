package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.DoCodeGen
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.text.markup.MarkupTemplateEngine
import org.slf4j.Logger


/**
 * Created by eiko on 05.06.17.
 */
abstract class GeneratorBase extends TypeStringManipulation {
    Template template

    static void createDir(String dirName) {
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
        return createTemplatePreprocessing (new FileInputStream(templateFile), engine)
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
        return createTemplatePreprocessing (inputStream, engine)
    }

    Template createTemplatePreprocessing (InputStream inputStream, TemplateEngine engine) {
        BufferedReader reader = new BufferedReader( new InputStreamReader (inputStream))
        // start preprocessing of the template
        StringBuffer strBuffer = new StringBuffer()
        String s
        def replaceMap=[:]
        while ((s = reader.readLine()) != null) {
            if (!s) {
                strBuffer.append('\n')
                continue
            }
            if (getDefinedMacro(replaceMap,s)) continue // found macro
            String processed=s.replaceAll('\\s*////.*$','')
            processed = replaceMacros(replaceMap,processed)
            if (!processed) continue
            strBuffer.append(processed)
            strBuffer.append('\n')
        }
        // end preprocessing of the template
        StringReader strReader = new StringReader(strBuffer.toString())
        return engine.createTemplate(strReader);
    }


    boolean getDefinedMacro(Map replaceMap,String line) {
        if (!line) return false
        def s = line.trim()
        if (s.startsWith('//define')) {
            s = s.replaceAll('//define\\s*','')
            def i = s.indexOf('=')
            def key = s.substring(0,i).trim()
            def value = s.substring(i+1).trim()
            replaceMap.put(key,value)
            return true
        }
        else
            return false
    }

    static String replaceMacros(Map replaceMap,String line) {
        if (!line) return line
        def keys = replaceMap.keySet()
        def iterator = keys.iterator()
        while (iterator.hasNext()) {
            def key = iterator.next()
            line = line.replace(key,replaceMap[key])
        }
        return line
    }

    static String removeEmptyLines (String genResult) {
        String s=genResult.replaceAll(/\n\s*\n\s*\n/,'\n')
        s = s.replaceAll(/\n\s*\n/,'\n')
        s = s.replaceAll(/}\s*\n(\s*[a-zA-Z])/,'}\n\n$1')
        /*
        s = s.replaceAll(/:\s*\n\s*\n/,':\n')
        s = s.replaceAll(/;\s*\n\s*\n/,';\n')
        */
        return s;
    }

    /**
     * method create a map object and initialize it with some basic string manipulation stuff
     * needed for working with the types and their properties in the templates.
     * @return
     */
    /*
    Map getClosures() {
        return new TypeStringManipulation().getClosures()
    }
    */

    /**
     * method create a map object and initialize it with some basic stuff
     * @param model
     * @return
     */
    Map createTemplateDataMap(Model model) {
        Map map = getClosures()
        map.model = model
        map.renderInnerTemplate = renderInnerTemplate
        return map
    }

    def renderInnerTemplate = { templateResource,actObj,indent ->
        def test = actObj.toString()
        def innerTemplate = createTemplateFromResource(templateResource,TemplateType.GString)
        def data = getClosures()
        data.actObj = actObj
        data.indent = indent
        data.renderInnerTemplate = renderInnerTemplate
        return innerTemplate.make(data)
    }

    abstract String getDestFileName(Model dataModel, Map<String,String> extraParameters,Type currentType=null)
    abstract String getDestDir(Model dataModel, String outputBasePath, Map<String,String> extraParameters,Type currentType=null)

    abstract Logger getLogger()
}
