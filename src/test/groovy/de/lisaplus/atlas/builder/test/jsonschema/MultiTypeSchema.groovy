package de.lisaplus.atlas.builder.test.jsonschema

import de.lisaplus.atlas.ModelTestHelper
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.model.ComplexType
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.RefType
import de.lisaplus.atlas.model.StringType
import de.lisaplus.atlas.model.UnsupportedType
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
        def typeName = 'Action'
        ModelTestHelper.checkPropertySize(model,typeName,3)
        ModelTestHelper.compareProperty (new Property(
                name: 'default_title',
                description: 'Tooltip for the main toolbar icon.',
                type: new StringType()
        ),model,typeName)
        ModelTestHelper.compareProperty (new Property(
                name: 'default_popup',
                description: 'The popup appears when the user clicks the icon.',
                type: new RefType()
        ),model,typeName)
        ModelTestHelper.compareProperty (new Property(
                name: 'default_icon',
                description: 'Icon for the main toolbar.',
                type: new ComplexType()
        ),model,typeName)

        typeName = 'Command'
        ModelTestHelper.checkPropertySize(model,typeName,2)
        ModelTestHelper.compareProperty (new Property(
                name: 'description',
                description: null,
                type: new StringType()
        ),model,typeName)
        ModelTestHelper.compareProperty (new Property(
                name: 'suggested_key',
                description: null,
                type: new UnsupportedType()
        ),model,typeName)
    }
}
