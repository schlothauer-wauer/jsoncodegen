package de.lisaplus.atlas

import de.lisaplus.atlas.model.RefType

import static org.junit.Assert.*

/**
 * Created by eiko on 02.06.17.
 */
class ModelTestHelper {
    static void checkPropertySize (def model, def typeName,def expectedSize) {
        def found = false
        model.types.find { it.name==typeName }.each {
            found = true
            assertEquals(expectedSize,it.properties.size())
        }
        assertTrue(found)
    }

    static void compareProperty (def expectedProperty,def model,def typeName) {
        def propName = expectedProperty.name
        def found = false
        model.types.find { it.name==typeName }.properties.find { it.name==propName }.each {
            found = true
            assertEquals(expectedProperty.name,it.name)
            assertEquals(expectedProperty.description,it.description)
            assertEquals(expectedProperty.type.class,it.type.class)
            assertEquals(expectedProperty.type.isArray,it.type.isArray)
            if (expectedProperty.type instanceof RefType ) {
                assertEquals(expectedProperty.type.typeName,it.type.typeName)
            }
        }
        assertTrue(found)
    }
}
