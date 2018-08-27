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
        def destFile = 'tmp/dummy.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model = 'src/test/resources/xsd/simpleTest.xsd'
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void plantuml_2() {
        def destFile = 'tmp/dummy.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model = 'src/test/resources/xsd/complex/DSRC.xsd'
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void plantuml_3() {
        def destFile = 'tmp/dummy.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model = 'src/test/resources/xsd/arrayTest.xsd'
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

}
