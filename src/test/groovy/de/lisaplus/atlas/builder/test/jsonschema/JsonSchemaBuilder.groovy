package de.lisaplus.atlas.builder.test.jsonschema

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.external.ExtSingleFileGenarator
import de.lisaplus.atlas.codegen.test.DoCodeGen
import de.lisaplus.atlas.model.AggregationType
import de.lisaplus.atlas.model.ByteType
import de.lisaplus.atlas.model.EnumType
import de.lisaplus.atlas.model.IntType
import de.lisaplus.atlas.model.LongType
import de.lisaplus.atlas.model.RefType
import org.junit.Test

import static de.lisaplus.atlas.codegen.test.DoCodeGen.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

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
    void testExt() {
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(8,model.types.size())
        de.lisaplus.atlas.DoCodeGen.sortTypesAndProperties(model)
        println (model)
        def userLogType = model.types.find { it.name=='UserLog' }
        assertNotNull(userLogType)
        def domainsProp = userLogType.properties.find { it.name=='domains' }
        assertNotNull(domainsProp)
        assertFalse(domainsProp.isRefTypeOrComplexType())
    }

    @Test
    void testExtWithEnums() {
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        builder.createEnumTypes = true
        def model = builder.buildModel(modelFile)
        assertEquals(10,model.types.size())

        def grantsEnumType = model.types.find { it.name == 'GrantsEnum'}
        assertNotNull(grantsEnumType)
        List<String> expected1 = ['read','write','commit']
        assertEquals(expected1.size(),((EnumType)grantsEnumType).allowedValues.size())
        for (int i=0; i<expected1.size();i++) {
            assertEquals(expected1[i],((EnumType)grantsEnumType).allowedValues[i])
        }

        def userLogType = model.types.find { it.name == 'UserLogType'}
        assertNotNull(userLogType)
        def userLog = model.types.find { it.name == 'UserLog'}
        assertNotNull(userLog)
        def userLogTypeProp = userLog.properties.find { it.name == 'type' }
        assertNotNull(userLogTypeProp)
        assertTrue ( userLogTypeProp.type instanceof RefType )
        assertEquals('UserLogType', userLogTypeProp.type.type.name )
        List<String> expected2 = ['login','logout']
        assertEquals(expected2.size(),((EnumType)userLogType).allowedValues.size())
        for (int i=0; i<expected2.size();i++) {
            assertEquals(expected2[i],((EnumType)userLogType).allowedValues[i])
        }
    }

    @Test
    void testExtWithEnumsWithDiffrentAllowedValues() {
        try {
            def modelFile = new File('src/test/resources/test_schemas/ds/user_different_enum_values.json')
            assertTrue(modelFile.isFile())
            def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
            builder.createEnumTypes = true
            def model = builder.buildModel(modelFile)
            fail()
        }
        catch (Exception e) {
            assertEquals('expect an enum type but there already exists an non-enum type with the same name: GrantsEnum',e.message)
        }
    }

    @Test
    void testGetSchemaBasePath() {
        File f = new File ('src/test/resources/test_schemas/multiType.json')
        /*
        println "canPath: ${f.getCanonicalPath()}"
        println "name: ${f.getName()}"
        */
        def s = de.lisaplus.atlas.builder.JsonSchemaBuilder.getBasePathFromModelFile(f)
        def s2 = s.replaceAll('\\\\','/') // convert windows file separator to linux/unix file separator 
        assertEquals('src/test/resources/test_schemas/',s2)
    }

    @Test
    void testAggregationType () {
        def modelFile = new File('src/test/resources/schemas/Container.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def timingPlanType = model.types.find { it.name=='TimingPlanType' }
        assertNotNull(timingPlanType)
        def phases_idProp = timingPlanType.properties.find { it.name=='phasesId'}
        assertNotNull(phases_idProp)
        assertEquals(AggregationType.aggregation,phases_idProp.aggregationType)

        def timingPlan = model.types.find { it.name=='TimingPlan' }
        assertNotNull(timingPlan)
        def phasesProp = timingPlan.properties.find { it.name=='phases'}
        assertNotNull(phasesProp)
        assertEquals(AggregationType.composition,phasesProp.aggregationType)

        def cycleType = model.types.find { it.name=='CycleType' }
        assertNotNull(cycleType)
        def std = cycleType.properties.find { it.name=='std'}
        assertNotNull(std)
        assertEquals(AggregationType.composition,phasesProp.aggregationType)
    }

    @Test
    void testAllOfFeature() {
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/map_object.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def mapObject = model.types.find { it.name=='MapObject' }
        assertNotNull(mapObject)
    }

    @Test
    void testGisObj() {
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/gis_object.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def mapObject = model.types.find { it.name=='GISObject' }
        assertNotNull(mapObject)
        assertEquals(1,mapObject.properties.size())
        assertEquals('gis',mapObject.properties[0].name)
    }

    @Test
    void testMapObjMulti() {
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/map_object_multi_type.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def mapObject = model.types.find { it.name=='MapObject' }
        assertNotNull(mapObject)
        assertEquals(2,mapObject.properties.size())
        assertEquals('gis',mapObject.properties[0].name)
        assertEquals('display',mapObject.properties[1].name)
    }

    @Test
    void testSimpleMapObj() {
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/simple_map_object.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def mapObject = model.types.find { it.name=='MapObject' }
        assertNotNull(mapObject)
        assertEquals(2,mapObject.properties.size())
        assertEquals('gis',mapObject.properties[0].name)
        assertEquals('display',mapObject.properties[1].name)
    }

    @Test
    void testImplicitRef() {
        def modelFile = new File('src/test/resources/test_schemas/ds/implicit_ref.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def docObject = model.types.find { it.name=='Document' }
        assertNotNull(docObject)
        def tagsProp = docObject.properties.find { it.name=='tags' }
        assertNotNull(tagsProp)
        assertNotNull(tagsProp.implicitRef)
    }

    @Test
    void testXXXX() {
        def modelFile = new File('src/test/resources/test_schemas/ds/incident.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
        def de.lisaplus.atlas.codegen.GeneratorBase generator = new ExtSingleFileGenarator()
        model.types.findAll {
            return (!it.isInnerType()) && (generator.containsPropName(it, 'gid')) &&
                    it.name != 'Person' && it.name != 'Address' && it.name != 'Comment' && it.name != 'Document' && it.name != 'SelectionEntry'
        }.each { type ->
            println(type.name)
        }
        // test for tags in attributes
        def foundReferences=0
        def foundInnerReferences=false
        model.types.find {
            return (it.name == 'Incident')
        }.each { type ->
            type.properties.findAll { it.name=='references' || it.name=='innerReferences' }.each { prop ->
                    assertEquals(1,prop.tags.size())
                    assertEquals('recursion',prop.tags.get(0))
                    if (prop.name=='references') {
                        assertTrue(prop.selfReference)
                    }
                    foundReferences++
            }
        }
        model.types.find {
            return (it.name == 'IncidentInnerReferencesItem')
        }.each { type ->
            assertEquals(1,type.tags.size())
            assertEquals('recursion',type.tags.get(0))
            foundInnerReferences=true
        }
        assertTrue(foundInnerReferences)
        assertEquals(2,foundReferences)
    }


    @Test
    void testRecursion_1() {
        def modelFile = new File('src/test/resources/test_schemas/ds/shared/options_response.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

    @Test
    void testRecursion_2() {
        def modelFile = new File('src/test/resources/test_schemas/ds/shared/options_response2.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

    @Test
    void testTags() {
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(8,model.types.size())
        def typesWithTags = 0
        model.types.find{
            if (it.tags) {
                assertTrue (it.name.equals('RoleModuleGrantsItem') || it.name.equals('User'))
                typesWithTags++
            }
        }
        assertEquals(2,typesWithTags)
        def propsWithTags = 0
        model.types.each {
            it.properties.find {
                if (it.tags) {
                    propsWithTags++
                    assertTrue (it.name.equals('grant') || it.name.equals('moduleGrants'))
                }
            }
            if (it.name=="Role") {
                assertEquals(2,it.version)
            }
            else if (it.name=="Domain") {
                assertEquals(4,it.version)
            }
            else {
                assertEquals(0,it.version)
            }
        }
        assertEquals(2,propsWithTags)
        assertEquals(3,model.version)
    }

    @Test
    void testTags2() {
        def modelFile = new File('src/test/resources/test_schemas/ds/junction.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
        def tagCount=0;
        model.types.find{
            if (it.name == 'Junction') {
                return true
            }
            return false
        }.each { type ->
            tagCount = type.tags.size();
        }
        // junction has 3 tags
        assertEquals(3,tagCount)
        tagCount=0
        model.types.find{
            if (it.name == 'JunctionBase') {
                return true
            }
            return false
        }.each { type ->
            tagCount = type.tags.size();
        }
        // junction has 3 tags
        assertEquals(2,tagCount)

    }

    @Test
    void testSchemaPath() {
        def modelFile = new File('src/test/resources/test_schemas/ds/junction.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
        model.types.each { type ->
            assertNotNull(type.schemaPath)
            assertNotNull(type.schemaFileName)
            println "schema-path: ${type.schemaPath}, filename: ${type.schemaFileName}"
        }
    }

    @Test
    void testMainTypes() {
        def modelFile = new File('src/test/resources/test_schemas/ds/junction.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
        model.types.each { type ->
            println "type-name: ${type.name}"
            if (type.name=='Junction') {
                assertTrue (type.isMainType('junction'))
            }
            else if (type.name=='JunctionDocument') {
                assertTrue (type.isMainType('junction'))
            }
            else if (type.name=='JunctionComment') {
                assertTrue (type.isMainType('junction'))
            }
            else if (type.name=='JunctionState') {
                assertTrue (type.isMainType('junction'))
            }
            else if (type.name=='JunctionType') {
                assertTrue (type.isMainType('junction'))
            }
            else {
                assertFalse (type.isMainType('junction'))
            }
            if (type.name=="Domain") {
                assertEquals(4,type.version)
            }
            else {
                assertEquals(0,type.version)
            }
        }
    }

    @Test
    void testExtraTypes() {
        def modelFile = new File('src/test/resources/test_schemas/ds/extra_types.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
        model.types.find{ it.name=='ExtraType' }.each { type ->
            assertTrue (type.properties.find { it.name=='intAttrib' }.type instanceof IntType)
            assertTrue (type.properties.find { it.name=='intAttrib2' }.type instanceof IntType)
            assertTrue (type.properties.find { it.name=='noIntAttrib' }.type instanceof LongType)
            assertTrue (type.properties.find { it.name=='byteAttrib' }.type instanceof ByteType)
        }
    }

    @Test
    void lisaInesTypes() {
        def modelFile = new File('src/test/resources/test_schemas/ds/lisa-ines-network.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

    @Test
    void lisaInesTypes_externalRef() {
        def modelFile = new File('src/test/resources/test_schemas/ds/lisa-ines-network_new_references.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertNotNull(model)
    }

}
