package de.lisaplus.atlas.builder.jsonschema

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import org.junit.Test
import static org.junit.Assert.*

/**
 * Created by eiko on 02.06.17.
 */
class MultiTypeSchema {
    @Test
    void test_initModel() {
        def modelFile = new File('src/test/resources/test_schemas/multiType.json')
        assertTrue(modelFile.isFile())
        def builder = new JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(model.description,'This is a multi type test schema :)')
        assertEquals(model.title,'JSON schema for multi type testing')
        assertEquals(2,model.types.size())
    }
}
