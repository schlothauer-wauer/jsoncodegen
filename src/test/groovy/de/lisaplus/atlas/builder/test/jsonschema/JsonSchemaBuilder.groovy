package de.lisaplus.atlas.builder.test.jsonschema

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * Created by eiko on 18.06.17.
 */
class JsonSchemaBuilder {
    @Test
    void testExternalReferences_1() {
        def modelFile = new File('src/test/resources/test_schemas/ds/license.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(4,model.types.size())
    }

    @Test
    void testExternalReferences_2() {
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(6,model.types.size())
    }


    @Test
    void testGetSchemaBasePath() {
        File f = new File ('src/test/resources/test_schemas/multiType.json')
        /*
        println "canPath: ${f.getCanonicalPath()}"
        println "name: ${f.getName()}"
        */
        def s = de.lisaplus.atlas.builder.JsonSchemaBuilder.getBasePathFromModelFile(f)
        assertEquals('src/test/resources/test_schemas/',s)
    }
}
