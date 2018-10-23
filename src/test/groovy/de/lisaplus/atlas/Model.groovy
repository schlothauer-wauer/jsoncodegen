package de.lisaplus.atlas

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * Meta model tests
 * Created by eiko on 23.06.17.
 */
class Model {
    @Test
    void testInitRefOwner() {
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def typeName='Domain'
        boolean found=false
        model.types.find { it.name==typeName }.each { type ->
            found=true
            assertEquals(0,type.refOwner.size())
        }
        assertTrue(found)
        typeName='Application'
        found=false
        model.types.find { it.name==typeName }.each { type ->
            found=true
            assertEquals(0,type.refOwner.size())
        }
        assertTrue(found)
        typeName='AppModule'
        found=false
        model.types.find { it.name==typeName }.each { type ->
            found=true
            // because inner type are ignored
            assertEquals(0,type.refOwner.size())
        }
        assertTrue(found)
        typeName='Role'
        found=false
        model.types.find { it.name==typeName }.each { type ->
            found=true
            assertEquals(1,type.refOwner.size())
        }
        assertTrue(found)
        typeName='User'
        found=false
        model.types.find { it.name==typeName }.each { type ->
            found=true
            assertEquals(0,type.refOwner.size())
        }
        assertTrue(found)
        typeName='UserLog'
        found=false
        model.types.find { it.name==typeName }.each { type ->
            found=true
            assertEquals(0,type.refOwner.size())
        }
        assertTrue(found)
    }
}
