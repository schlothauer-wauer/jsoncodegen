package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertTrue

/**
 * Tests the plantuml generator and template
 * Created by eiko on 19.06.17.
 */
class Xsd {
    private static void generateXsd(String model, String destFile) {
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model=model
        doCodeGen.generators.add('xsd')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add("destFileName=${destFile}")
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File("tmp/${destFile}").exists())
    }

    private static void generatePlantUML(String model, String destFile) {
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model=model
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add("destFileName=${destFile}")
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File("tmp/${destFile}").exists())
    }

    @Test
    void createUserModel() {
        def model='src/test/resources/test_schemas/ds/user.json'
        def xsdModel='user.xsd'
        def plantUmlJson='user_xsd.puml'
        def plantUmlXsd='user_xsd.puml'
        generatePlantUML(model,plantUmlJson)
        generateXsd(model,xsdModel)
        generatePlantUML("tmp/${xsdModel}",plantUmlXsd)
    }
}
