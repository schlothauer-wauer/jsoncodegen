package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertTrue

/**
 * Tests the plantuml generator and template
 * Created by eiko on 19.06.17.
 */
class PlantUml {
    @Test
    void createUserModel() {
        def destFile='tmp/user.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/user.json'
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('destFileName=user.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createLicenseModel() {
        def destFile='tmp/license.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/license.json'
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('destFileName=license.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createUserModel_BuiltIn() {
        def destFile='tmp/user_model.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/user.json'
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createFlorian_BuiltIn() {
        def destFile='tmp/device.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/Device.json'
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void testHeavyReferenced() {
        def destFile='tmp/heavy_referenced.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/referenced_multi_types.json'
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('destFileName=heavy_referenced.puml')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }


    @Test
    void createUserModel_Markdown() {
        def destFile='tmp/user_puml.md'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/user.json'
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('markdown=true')
        doCodeGen.generator_parameters.add('destFileName=user_puml.md')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    static void test_v4_base(def outputFileBase) {
        def destFile="tmp/${outputFileBase}.puml"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model="src/test/resources/schemas/${outputFileBase}.json"
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add("destFileName=${outputFileBase}.puml".toString())
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }


    @Test
    void test_v4() {
        test_v4_base("test_4")

        test_v4_base("CnResponseType")
        test_v4_base("CycCollectionType")
        test_v4_base("DetCollectionType")
        test_v4_base("GeoCollectionType")
        test_v4_base("LocCollectionType")
        test_v4_base("ProcessDataEvent")
        test_v4_base("StdCollectionType")
        test_v4_base("TimCollectionType")
        test_v4_base("Container")
    }

}
