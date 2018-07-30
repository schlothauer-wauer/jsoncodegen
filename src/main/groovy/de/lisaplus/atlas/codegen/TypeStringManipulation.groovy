package de.lisaplus.atlas.codegen.meta;

import de.lisaplus.atlas.codegen.helper.java.JavaTypeConvert
import de.lisaplus.atlas.codegen.helper.java.JsonTypeConvert
import de.lisaplus.atlas.codegen.helper.java.SwaggerTypeConvert
import de.lisaplus.atlas.model.ComplexType
import de.lisaplus.atlas.model.InnerType
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.RefType
import de.lisaplus.atlas.model.Type

class TypeStringManipulation {

    /**
     * method create a map object and initialize it with some basic string manipulation stuff
     * needed for working with the types and their properties in the templates.
     * @return
     */
    Map getClosures() {
        return [
            DOLLAR:'$',
            printIndent: printIndent,
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

    def printIndent = { indent ->
        def ret = ''
        for (def i=0;i<indent;i++) ret+=' '
        return ret
    }

    def toLowerCase = { str ->
        return str==null ? EMPTY : str.toLowerCase()
    }

    def toUpperCase = { str ->
        return str==null ? EMPTY : str.toUpperCase()
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

    // Closure<List<Property>> filterProps = { Type type, Map params ->
    def filterProps = { type, params ->
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
        if (params.withoutTag != null) props = props.findAll { prop -> !prop.hasTag(params.withoutTag) }
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

    // Vararg definition is mandatory, call example: printLines3.call( [level:2, by: '.-'], new StringWriter(), list, list2 )
    // Closure<Void> printLines3 = { Writer out, Map params,  List... lists ->
    def printLines = { out, params,List... lists ->
        def level = params.level?:0
        def by = params.by?:'    '
        def prefix = ''
        level.times { prefix += by}
        lists.flatten().each { out << "$prefix$it\n" }
    }

    private final static String EMPTY=''
}
