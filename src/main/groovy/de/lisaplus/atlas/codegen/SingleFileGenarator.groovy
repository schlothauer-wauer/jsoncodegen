package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.interf.ICodeGen
import de.lisaplus.atlas.model.Model

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
    }

}
