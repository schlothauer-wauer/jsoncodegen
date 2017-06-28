package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.interf.ICodeGen
import de.lisaplus.atlas.model.Model
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is the base for code generators that only create one file
 * Created by eiko on 05.06.17.
 */
abstract class SingleFileGenarator extends GeneratorBase implements ICodeGen {
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
        if (extraParams) {
            data.extraParam = extraParams
        }
        else {
            data.extraParam = [:]
        }

        def ergebnis = template.make(data)

        def destFileName = getDestFileName(model,extraParams)
        def destDir = getDestDir(model,outputBasePath,extraParams)

        def shouldRemoveEmptyLines = extraParams['removeEmptyLines']

        def resultString = shouldRemoveEmptyLines ? removeEmptyLines (ergebnis.toString()) :
                ergebnis.toString()
        File file=new File("${destDir}/${destFileName}")
        file.write( resultString )
    }
}
