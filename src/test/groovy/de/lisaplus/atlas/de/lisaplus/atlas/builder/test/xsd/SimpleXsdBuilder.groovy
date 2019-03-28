package de.lisaplus.atlas.de.lisaplus.atlas.builder.test.xsd

import de.lisaplus.atlas.builder.XSDBuilder
import org.junit.Test

import static junit.framework.Assert.assertTrue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class SimpleXsdBuilder {
    @Test
    void simple() {
        def modelFile = new File('src/test/resources/xsd/simpleTest.xsd')
        assertTrue(modelFile.isFile())
        def builder = new XSDBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

    @Test
    void arrays() {
        def modelFile = new File('src/test/resources/xsd/arrayTest.xsd')
        assertTrue(modelFile.isFile())
        def builder = new XSDBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

    @Test
    void comlplex() {
        def modelFile = new File('src/test/resources/xsd/complex/DSRC.xsd')
        assertTrue(modelFile.isFile())
        def builder = new XSDBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

    @Test
    void plantuml_1() {
        def fileName = 'plantuml_from_xsd1.puml'
        def destFile = "tmp/$fileName"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/xsd/simpleTest.xsd']
        doCodeGen.generators.add('plantuml')
        doCodeGen.generator_parameters.add("destFileName=$fileName")
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void plantuml_2() {
        def fileName = 'plantuml_from_xsd2.puml'
        def destFile = "tmp/$fileName"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/xsd/complex/DSRC.xsd']
        doCodeGen.generators.add('plantuml')
        doCodeGen.generator_parameters.add("destFileName=$fileName")
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void plantuml_3() {
        def fileName = 'plantuml_from_xsd3.puml'
        def destFile = "tmp/$fileName"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/xsd/arrayTest.xsd']
        doCodeGen.generators.add('plantuml')
        doCodeGen.generator_parameters.add("destFileName=$fileName")
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void plantuml_4() {
        def fileName = 'tlc.puml'
        def destFile = "tmp/$fileName"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/xsd/ui-tlc.xsd']
        doCodeGen.generators.add('plantuml')
        doCodeGen.generator_parameters.add("destFileName=$fileName")
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void plantuml_6() {
        def fileName = 'plantuml6.puml'
        def destFile = "tmp/$fileName"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/xsd/LSA_Versorgung_OMTC.xsd']
        doCodeGen.generators.add('plantuml')
        doCodeGen.generator_parameters.add("destFileName=$fileName")
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void simple2() {
        def modelFile = new File('src/test/resources/xsd/ui-tlc.xsd')
        assertTrue(modelFile.isFile())
        def builder = new XSDBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

}
