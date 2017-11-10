package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.DoCodeGen
import de.lisaplus.atlas.codegen.helper.java.JavaTypeConvert
import de.lisaplus.atlas.codegen.helper.java.JsonTypeConvert
import de.lisaplus.atlas.codegen.helper.java.SwaggerTypeConvert
import de.lisaplus.atlas.model.InnerType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.Type
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.text.XmlTemplateEngine
import groovy.text.markup.MarkupTemplateEngine
import org.codehaus.groovy.runtime.StringBufferWriter
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
//        return s.replaceAll(/;\s*\n/,';\n\n')
        s = s.replaceAll(/\n\s*\n/,'\n')
        /*
        s = s.replaceAll(/:\s*\n\s*\n/,':\n')
        s = s.replaceAll(/;\s*\n\s*\n/,';\n')
        */
        return s;
    }

    /**
     * methon create a map object and initialize it with some basic stuff
     * @param model
     * @return
     */
    Map createTemplateDataMap(Model model) {
        return [
                model:model,
                DOLLAR:'$',
                toLowerCase: toLowerCase,
                toUpperCase: toUpperCase,
                firstLowerCase: firstLowerCase,
                firstUpperCase: firstUpperCase,
                isInnerType: isInnerType,
                typeToJava: JavaTypeConvert.convert,
                typeToSwagger: SwaggerTypeConvert.convert,
                typeToJson: JsonTypeConvert.convert,
                typeFormatToSwagger: SwaggerTypeConvert.format,
                typeFormatToJson: JsonTypeConvert.format,
                renderInnerTemplate: renderInnerTemplate,
                breakTxt: breakTxt,
                containsTag: containsTag,
                missingTag: missingTag,
                containsPropName: containsPropName,
                missingPropName: missingPropName
        ]
    }

    def isInnerType = { type ->
        return type && (type instanceof InnerType )
    }

    def containsTag = { obj, tag ->
        if (! tag ) return false
        if (! ((obj instanceof Type) || (obj instanceof Property))) {
            return false
        }
        if (!obj.tags) {
            return false
        }
        return obj.tags.contains(tag)
    }

    def containsPropName = { type, propName ->
        if (! type ) return false
        if (! propName ) return false
        if (! (type instanceof Type)) return false
        return type.properties.findIndexOf{
            it.name==propName
        } != -1
    }

    def missingPropName = { type, propName ->
        if (! type ) return false
        if (! propName ) return false
        if (! (type instanceof Type)) return false
        return type.properties.findIndexOf{
            it.name==propName
        } == -1
    }

    def missingTag = { obj, tag ->
        if (! tag ) return false
        if (! ((obj instanceof Type) || (obj instanceof Property))) {
            return false
        }
        if (!obj.tags) {
            return false
        }
        return ! obj.tags.contains(tag)
    }

    def breakTxt = { String txtToBreak,int charPerLine,String breakText='\n' ->
        if (!txtToBreak) return EMPTY
        StringBuilder sb = new StringBuilder()
        int txtLen = txtToBreak.length()
        int aktPos = 0
        while (aktPos < txtLen) {
            if ((aktPos + charPerLine) >= txtLen) {
                // The rest of the word is smaller than the desired char count per line
                sb.append(txtToBreak.substring(aktPos))
                break;
            } else {
                sb.append(txtToBreak.substring(aktPos, aktPos + charPerLine))
                aktPos += charPerLine
                if (txtToBreak.substring(aktPos, aktPos + 1) == ' ') {
                    sb.append(breakText)
                    aktPos++
                } else {
                    for (aktPos; aktPos < txtLen; aktPos++) {
                        String subStr = txtToBreak.substring(aktPos, aktPos + 1)
                        if (subStr == ' ') {
                            sb.append(breakText)
                            aktPos++
                            break
                        } else {
                            sb.append(subStr)
                        }
                    }
                }
            }
        }
        return sb.toString()
    }

    def toLowerCase = { str ->
        return str==null ? EMPTY : str.toLowerCase()
    }

    def toUpperCase = { str ->
        return str==null ? EMPTY : str.toUpperCase()
    }

    def renderInnerTemplate = { templateResource,actObj,indent ->
        def test = actObj.toString()
        def innerTemplate = createTemplateFromResource(templateResource,TemplateType.GString)
        def data = [
                actObj: actObj,
                indent: indent,
                printIndent: printIndent,
                DOLLAR:'$',
                toLowerCase: toLowerCase,
                toUpperCase: toUpperCase,
                firstLowerCase: firstLowerCase,
                firstUpperCase: firstUpperCase,
                isInnerType: isInnerType,
                typeToJava: JavaTypeConvert.convert,
                typeToSwagger: SwaggerTypeConvert.convert,
                typeToJson: JsonTypeConvert.convert,
                typeFormatToSwagger: SwaggerTypeConvert.format,
                typeFormatToJson: JsonTypeConvert.format,
                renderInnerTemplate: renderInnerTemplate,
                breakTxt: breakTxt
        ]

        return innerTemplate.make(data)
    }

    def printIndent = { indent ->
        def ret = ''
        for (def i=0;i<indent;i++) ret+=' '
        return ret
    }

    def firstLowerCase = { str ->
        if (!str) return EMPTY
        def first = str.substring(0,1)
        first = first.toLowerCase()
        if (str.length()>1) {
            def rest = str.substring(1)
            return first + rest
        }
        else {
            return first
        }
    }

    def firstUpperCase = { str ->
        if (!str) return EMPTY
        def first = str.substring(0,1)
        first = first.toUpperCase()
        if (str.length()>1) {
            def rest = str.substring(1)
            return first + rest
        }
        else {
            return first
        }
    }


    private final static String EMPTY=''

    abstract String getDestFileName(Model dataModel, Map<String,String> extraParameters,Type currentType=null)
    abstract String getDestDir(Model dataModel, String outputBasePath, Map<String,String> extraParameters,Type currentType=null)

    abstract Logger getLogger();
}
