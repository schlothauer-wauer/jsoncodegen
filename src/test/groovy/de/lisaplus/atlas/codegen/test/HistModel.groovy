package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertTrue

/**
 * Tests the plantuml generator and template
 * Created by eiko on 19.06.17.
 */
class HistModel {
    private static void generateHistModel(String model, String destFile) {
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = [model]
        doCodeGen.generators.add('hist_model')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add("destFileName=${destFile}")
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File("tmp/${destFile}").exists())
    }

    private static void generatePlantUML(String model, String destFile) {
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[model]
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
        def histModel='user_hist.json'
        def plantUml='user_hist.puml'
        generateHistModel(model,histModel)
        generatePlantUML("tmp/${histModel}",plantUml)
    }

    @Test
    void createMapObjectModel() {
        def model='src/test/resources/test_schemas/ds/base_types/map_object.json'
        def histModel='map_hist.json'
        def plantUml='map_hist.puml'
        generateHistModel(model,histModel)
        generatePlantUML("tmp/${histModel}",plantUml)
    }

}
