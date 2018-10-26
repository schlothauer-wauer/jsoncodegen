package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.interf.ICodeGen
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base class for generators that creates more than one file ... for instance JavaBeanGenerator
 * Created by eiko on 05.06.17.
 */
abstract class MultiFileGenarator extends GeneratorBase implements ICodeGen {
    /**
     * This funkction is called to start the code generation process
     * @param model model that is the base for the code generation
     * @param outputBasePath under this path the output is generated. A generator can add a needed sub path if needed (for instance for packeges)
     * @param extraParams additional parameters to initialize the generator
     */
    void doCodeGen(Model model, String outputBasePath, Map<String,String> extraParams) {
        if (!template) {
            def errorMsg = "template not initialized"
            getLogger().error(errorMsg)
            throw new Exception(errorMsg)
        }
        def data = createTemplateDataMap(model)
        this.extraParams = extraParams
        if (extraParams) {
            data.extraParam = extraParams
        }
        else {
            data.extraParam = [:]
        }

        def blackListed=data.extraParam['blackListed']
        def whiteListed=data.extraParam['whiteListed']

        def shouldRemoveEmptyLines = extraParams['removeEmptyLines']

        def neededAttrib = extraParams['containsAttrib']
        def missingAttrib = extraParams['missingAttrib']
        def neededTag = extraParams['neededTag']
        def neededTagList = splitValueToArray(neededTag)

        def ignoredTag = extraParams['ignoreTag']
        def ignoredTagList = splitValueToArray(ignoredTag)
        model.types*.each { type ->
            boolean handleNeeded = neededAttrib ? type.properties.find { prop ->
                return prop.name==neededAttrib
            } != null : true
            boolean handleMissing = missingAttrib ? type.properties.find { prop ->
                return prop.name==missingAttrib
            } == null : true

            boolean handleType=true;
            if (whiteListed && (!whiteListed.contains(type.name))) {
                handleType = false
                println "ingnored by white-list: ${type.name}"
            }
            else if (blackListed && blackListed.contains(type.name)) {
                handleType = false
                println "ingnored by black-list: ${type.name}"
            }

            boolean handleTag = ignoredTagList ? type.tags.find { tag ->
                return ignoredTagList.contains(tag)
            } == null : true

            if (handleTag && neededTagList) {
                handleTag = type.tags.find { tag ->
                    return neededTagList.contains(tag)
                } != null
            }

            if (handleType && handleNeeded && handleMissing && handleTag) {
                data.put('currentType', type)
                def ergebnis = template.make(data)
                def destFileName = getDestFileName(model, extraParams, type)
                def destDir = getDestDir(model, outputBasePath, extraParams, type)
                def pathToFile = "${destDir}/${destFileName}"
                File file = new File(pathToFile)
                def resultString = shouldRemoveEmptyLines ? removeEmptyLines(ergebnis.toString()) :
                        ergebnis.toString()
                file.write(resultString)
                println ("written: $pathToFile")
            }
        }
    }

    private List<String> splitValueToArray(String value) {
        if (!value) return []
        return value.indexOf(',')!=-1 ? value.split(',') : value.split(':')
    }
}
