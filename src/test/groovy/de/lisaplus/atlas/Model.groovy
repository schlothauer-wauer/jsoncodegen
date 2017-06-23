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
        def typeName='domain'
        model.types.find { it.name==typeName }.each { type ->
            assertEquals(3,type.refOwner.size())
        }
        typeName='application'
        model.types.find { it.name==typeName }.each { type ->
            assertEquals(1,type.refOwner.size())
        }
        typeName='app_module'
        model.types.find { it.name==typeName }.each { type ->
            assertEquals(1,type.refOwner.size())
        }
        typeName='role'
        model.types.find { it.name==typeName }.each { type ->
            assertEquals(1,type.refOwner.size())
        }
        typeName='user'
        model.types.find { it.name==typeName }.each { type ->
            assertEquals(1,type.refOwner.size())
        }
        typeName='user_log'
        model.types.find { it.name==typeName }.each { type ->
            assertEquals(0,type.refOwner.size())
        }
    }
}
