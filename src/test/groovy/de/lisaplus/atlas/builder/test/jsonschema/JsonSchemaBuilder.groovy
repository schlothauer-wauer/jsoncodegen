package de.lisaplus.atlas.builder.test.jsonschema

import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.external.ExtSingleFileGenarator
import de.lisaplus.atlas.model.AggregationType
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
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
        assertEquals(8,model.types.size())
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
        def phases_idProp = timingPlanType.properties.find { it.name=='phases_id'}
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
        def mapObject = model.types.find { it.name=='Map_object' }
        assertNotNull(mapObject)
    }

    @Test
    void testGisObj() {
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/gis_object.json')
        assertTrue(modelFile.isFile())
        def builder = new de.lisaplus.atlas.builder.JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        def mapObject = model.types.find { it.name=='GIS_object' }
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
        def mapObject = model.types.find { it.name=='Map_object' }
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
        def mapObject = model.types.find { it.name=='Map_object' }
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
                    assertTrue (it.name.equals('grant') || it.name.equals('module_grants'))
                }
            }
        }
        assertEquals(2,propsWithTags)
    }
}
