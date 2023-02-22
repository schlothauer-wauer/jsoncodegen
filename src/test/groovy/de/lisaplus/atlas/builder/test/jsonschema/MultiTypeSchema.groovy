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
        assertEquals(1,model.version)
        assertEquals(5,model.types.size())
        def typeName = 'Action'
        ModelTestHelper.checkPropertySize(model,typeName,3)
        ModelTestHelper.compareProperty (new Property(
                name: 'defaultTitle',
                description: 'Tooltip for the main toolbar icon.',
                type: new StringType()
        ),model,typeName)
        ModelTestHelper.compareProperty (new Property(
                name: 'defaultPopup',
                description: 'The popup appears when the user clicks the icon.',
                type: new RefType(typeName: 'Icon')
        ),model,typeName)
        ModelTestHelper.compareProperty (new Property(
                name: 'defaultIcon',
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
                name: 'suggestedKey',
                description: null,
                type: new UnsupportedType()
        ),model,typeName)
    }

    /**
     * Additional tests for lisa server data models ... runs only if the related project is available
     */
    @Test
    void testComplexIncludes() {
        File f1 = new File('.')
        def s = f1.getAbsolutePath()

        File f = new File('../../gitlab/lisa-server_models/model/junction.json')
        if (f.exists()) {
            def builder = new JsonSchemaBuilder()
            def model = builder.buildModel(f)
            assertNotNull(model)
        }
    }
}
