package de.lisaplus.atlas.builder.test.jsonschema

import de.lisaplus.atlas.ModelTestHelper
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.StringType
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
        assertEquals(6,model.types.size())

        // check property count
        def typeName = 'SingleType'
        ModelTestHelper.checkPropertySize(model,typeName,10)
        ModelTestHelper.compareProperty (new Property(
                                                name: 'domainUUID',
                                                description: 'RFC 4122 compliant universally unique identifier (UUID) for identifying the domain of the traffic controller.',
                                                type: new StringType()
                                            ),model,typeName)
        ModelTestHelper.compareProperty (new Property(
                name: 'permissions',
                type: new StringType(isArray: true),
        ),model,typeName)

    }
}
