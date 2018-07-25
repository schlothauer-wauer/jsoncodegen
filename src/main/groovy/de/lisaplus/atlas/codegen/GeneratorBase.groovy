package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.DoCodeGen
import de.lisaplus.atlas.codegen.helper.java.JavaTypeConvert
import de.lisaplus.atlas.codegen.helper.java.JsonTypeConvert
import de.lisaplus.atlas.codegen.helper.java.SwaggerTypeConvert
import de.lisaplus.atlas.model.ComplexType
import de.lisaplus.atlas.model.InnerType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.RefType
import de.lisaplus.atlas.model.Type
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.text.XmlTemplateEngine
import groovy.text.markup.MarkupTemplateEngine
import org.codehaus.groovy.runtime.StringBufferWriter
import org.slf4j.Logger


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
                lowerCamelCase: firstLowerCamelCase,
                upperCamelCase: firstUpperCamelCase,
                isInnerType: isInnerType,
                isPropComplexType: isPropComplexType,
                typeToJava: JavaTypeConvert.convert,
                typeToSwagger: SwaggerTypeConvert.convert,
                typeToJson: JsonTypeConvert.convert,
                typeToMeta: JsonTypeConvert.meta,
                typeFormatToSwagger: SwaggerTypeConvert.format,
                typeFormatToJson: JsonTypeConvert.format,
                renderInnerTemplate: renderInnerTemplate,
                breakTxt: breakTxt,
                containsTag: containsTag,
                missingTag: missingTag,
                containsPropName: containsPropName,
                missingPropName: missingPropName,
                propsContainsTag: propsContainsTag,
                filterProps: filterProps,
                filterPropsPerform: filterPropsPerform,
                printLines: printLines
        ]
    }

    Closure<List<Property>> filterProps = { Type type, Map params ->
    // def filterProps = { type, params ->
        List props = type.properties
        if (params.filterCls!= null) props = props.findAll { params.get('filterCls') }
        if (params.name != null) props = props.findAll { prop -> prop.name == params.name }
        if (params.namePattern != null) props = props.findAll { prop -> prop.name =~ params.namePattern }
        if (params.complex != null) props = props.findAll { prop -> prop.isComplexType() == params.complex }
        if (params.refComplex != null) props = props.findAll { prop ->
            // println "refComplex: param=${params.refComplex} propName=${prop.name} propValue=${prop.isRefTypeOrComplexType()}"
            prop.isRefTypeOrComplexType() == params.refComplex }
        if (params.array != null) props = props.findAll { prop -> prop.type.isArray == params.array }
        if (params.join != null) props = props.findAll { prop ->
            // def evaled = prop.hasTag('join') == params.join
            // println "join: param=${params.join} propName=${prop.name} propValue=${prop.hasTag('join')} evaled=${evaled}"
            prop.hasTag('join') == params.join }
        if (params.aggregation != null) props = props.findAll { prop -> prop.isAggregation() == params.aggregation }
        if (params.implRefIsRef != null) props = props.findAll { prop -> prop.implicitRefIsRefType() == params.implRefIsRef }
        if (params.implRefIsComp != null) props = props.findAll { prop -> prop.implicitRefIsComplexType() == params.implRefIsComp }
        if (params.typeName!= null) props = props.findAll { prop -> prop.type.NAME == params.typeName }
        if (params.typeNameNot!= null) props = props.findAll { prop -> prop.type.NAME != params.typeNameNot }
        // Alternative: use pattern, e.g. typeNamePattern:'^(?!DATE$)' to get all types with names other than 'DATE'
        if (params.typeNamePattern!= null) props = props.findAll { prop -> prop.type.NAME =~ params.typeNamePattern }
        if (params.hasTag != null) props = props.findAll { prop -> prop.hasTag(params.hasTag) }
        return props
    }

    // Closure<List<String>> filterPropsPerform = {Type type,  Map params, Closure<Property> toLines ->
    def filterPropsPerform = { type, params, toLines ->
        def props = filterProps.call(type, params)
        def lines = []
        if (props.size() > 0 && params.comment != null) {
            lines += params.comment
        }
        // props.each { prop -> lines = lines + toLines.call(prop) }
        props.each { prop -> lines += toLines.call(prop) }
        if (props.size() > 0 && true == params.newLine) {
            lines += ''
        }
        return lines
    }


    // Closure<Void> printLines3 = { Writer out, Map params,  List... lists ->
    // Vararg definition is mandatory, call example: printLines3.call( [level:2, by: '.-'], new StringWriter(), list, list2 )
    def printLines = { out, params,List... lists ->
        def level = params.level?:0
        def by = params.by?:'    '
        def prefix = ''
        level.times { prefix += by}
        lists.flatten().each { out << "$prefix$it\n" }
    }

    def isInnerType = { type ->
        return type && (type instanceof InnerType )
    }

    def isPropComplexType = { prop ->
        return prop && prop.type && (prop.type instanceof ComplexType || prop.type instanceof RefType)
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

    def propsContainsTag = { type, name ->
        if (! type ) return false
        if (! name ) return false
        if (! (type instanceof Type)) {
            return false
        }
        def result = type.properties.find { it.tags.contains(name) }
        if (result) {
            return true
        }
        else {
            return false
        }
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
                lowerCamelCase: firstLowerCamelCase,
                upperCamelCase: firstUpperCamelCase,
                isInnerType: isInnerType,
                typeToJava: JavaTypeConvert.convert,
                typeToSwagger: SwaggerTypeConvert.convert,
                typeToJson: JsonTypeConvert.convert,
                typeToMeta: JsonTypeConvert.meta,
                typeFormatToSwagger: SwaggerTypeConvert.format,
                typeFormatToJson: JsonTypeConvert.format,
                renderInnerTemplate: renderInnerTemplate,
                breakTxt: breakTxt,
                filterProps: filterProps,
                filterPropsPerform: filterPropsPerform,
                printLines: printLines
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

    def firstUpperCamelCase = { str ->
        if (!str) return EMPTY
        def firstUpper = firstUpperCase(str)
        return convertAllUnderLinesToCamelCase(firstUpper)
    }

    def firstLowerCamelCase = { str ->
        if (!str) return EMPTY
        def firstLower = firstLowerCase(str)
        return convertAllUnderLinesToCamelCase(firstLower)
    }

    def convertAllUnderLinesToCamelCase = { String str ->
        if (!str) return EMPTY
        def i_ = str.indexOf('_')
        while (i_!=-1) {
            def stopLen = str.length()-1
            if (i_<stopLen) {
                def nextChar = new String(str.charAt(i_+1))
                if (nextChar=='_') {
                    str = str.replace('__','_')
                }
                else {
                    def nextCharUpper = nextChar.toUpperCase()
                    str = str.replace('_'+nextChar,new String(nextCharUpper))
                }
            }
            else
                break
            i_ = str.indexOf('_',i_)
        }
        return str
    }

    private final static String EMPTY=''

    abstract String getDestFileName(Model dataModel, Map<String,String> extraParameters,Type currentType=null)
    abstract String getDestDir(Model dataModel, String outputBasePath, Map<String,String> extraParameters,Type currentType=null)

    abstract Logger getLogger()
}
