package de.lisaplus.atlas.de.lisaplus.atlas.builder.test.xsd

import de.lisaplus.atlas.builder.XSDBuilder
import org.junit.Test

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
    void comlplex() {
        def modelFile = new File('src/test/resources/xsd/complex/DSRC.xsd')
        assertTrue(modelFile.isFile())
        def builder = new XSDBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

}
