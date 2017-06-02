package de.lisaplus.atlas.builder.jsonschema

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import org.junit.Test
import static org.junit.Assert.*

/**
 * Created by eiko on 02.06.17.
 */
class SingleTypeSchema {
    @Test
    void test_initModel() {
        def modelFile = new File('src/test/resources/test_schemas/singleType.json')
        assertTrue(modelFile.isFile())
        def builder = new JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(model.description,'JSON Schema for process data of a traffic controller.')
        assertNull(model.title)
        assertEquals(1,model.types.size())
        assertEquals(9,model.types[0].properties.size())
    }
}
