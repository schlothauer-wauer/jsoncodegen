package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertTrue

/**
 * Tests the plantuml generator and template
 * Created by eiko on 19.06.17.
 */
class JsonSchema {
    private static void generateHistModel(String model, String destFile) {
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[model]
        doCodeGen.generators.add('json_schema')
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
        def histModel='user_schema.json'
        def plantUml='user_schema.puml'
        generateHistModel(model,histModel)
        generatePlantUML("tmp/${histModel}",plantUml)
    }

    @Test
    void createMapObjectModel() {
        def model='src/test/resources/test_schemas/ds/base_types/map_object.json'
        def histModel='map_schema.json'
        def plantUml='map_schema.puml'
        generateHistModel(model,histModel)
        generatePlantUML("tmp/${histModel}",plantUml)
    }

    @Test
    void createMapObjectModel2() {
        def model='src/test/resources/test_schemas/ds/base_types/map_object_multi_type.json'
        def histModel='map_schema2.json'
        def plantUml='map_schema2.puml'
        generateHistModel(model,histModel)
        generatePlantUML("tmp/${histModel}",plantUml)
    }

    @Test
    void createRecursiveModel() {
        def model='src/test/resources/test_schemas/ds/shared/options_response.json'
        def histModel='options_response.json'
        def plantUml='options_response.puml'
        generateHistModel(model,histModel)
        generatePlantUML("tmp/${histModel}",plantUml)
    }

}
