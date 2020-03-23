package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue
import static org.junit.Assert.assertFalse

/**
 * Tests the plantuml generator and template
 * Created by eiko on 19.06.17.
 */
class PlantUml {
    @Test
    void createUserModel() {
        def destFile = 'tmp/user.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/user.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=user.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createIncidentModel() {
        def destFile = 'tmp/incident.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createLisaInesNetworkModel() {
        def destFileBase = 'lisa_ines_network_json.puml'
        def destFile = "tmp/$destFileBase"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/lisa-ines-network.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add("destFileName=$destFileBase")
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createIncidentModelAddTags() {
        def destFile = 'tmp/incident.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.typeAddTagList.add('Incident=main')
        doCodeGen.typeAddTagList.add('Incident=cool')
        doCodeGen.typeAddTagList.add('Domain=cool')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
        boolean incidentFound=false
        boolean domainFound=false
        doCodeGen.dataModel.types.each { type ->
            if (type.name=='Incident') {
                incidentFound=true
                assertEquals(5,type.tags.size())
                assertTrue(type.tags.contains('main'))
                assertTrue(type.tags.contains('cool'))
            }
            else if (type.name=='Domain') {
                domainFound=true
                assertEquals(1,type.tags.size())
                assertTrue(type.tags.contains('cool'))
            }
            else {
                assertFalse(type.tags.contains('main'))
                assertFalse(type.tags.contains('cool'))
            }
        }
        assertTrue(incidentFound)
        assertTrue(domainFound)
    }

    @Test
    void createIncidentModelRemoveTags() {
        def destFile = 'tmp/incident.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.typeRemoveTagList.add('Incident=mongodb')
        doCodeGen.typeRemoveTagList.add('Incident=rest')
        doCodeGen.typeRemoveTagList.add('Incident=joined')
        doCodeGen.typeRemoveTagList.add('IncidentTag=rest')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
        boolean incidentFound=false
        boolean incidentTagFound=false
        doCodeGen.dataModel.types.each { type ->
            if (type.name=='Incident') {
                incidentFound=true
                assertEquals(0,type.tags.size())
            }
            else if (type.name=='IncidentTag') {
                incidentTagFound=true
                assertEquals(1,type.tags.size())
                assertTrue(type.tags.contains('mongodb'))
            }
        }
        assertTrue(incidentFound)
        assertTrue(incidentTagFound)
    }

    @Test
    void createIncidentModelRemoveAllTags() {
        def destFile = 'tmp/incident.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('guidTypeColor=f9f6e0')
        doCodeGen.generator_parameters.add('ignoreUnRefTypes=true')
        doCodeGen.typeRemoveTagAllList.add('rest')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
        boolean incidentFound=false
        boolean incidentTagFound=false
        doCodeGen.dataModel.types.each { type ->
            assertFalse(type.tags.contains('mainType')) // could be implicit set with command line switch
            assertFalse(type.tags.contains('rest'))
            if (type.name=='Incident') {
                incidentFound=true
                assertEquals(2,type.tags.size())
            }
            else if (type.name=='IncidentTag') {
                incidentTagFound=true
                assertEquals(1,type.tags.size())
            }
        }
        assertTrue(incidentFound)
        assertTrue(incidentTagFound)
    }

    @Test
    void addMainTypeTags() {
        def destFile = 'tmp/incident_mainTypeTags.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident_mainTypeTags.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.tagMainTypes = true
        doCodeGen.run()
        assertTrue(new File(destFile).exists())

        int mainTypeCount=0
        doCodeGen.dataModel.types.each { type ->
            if (type.tags.contains('mainType')) {
                mainTypeCount++
            }
            if (type.name=='Incident') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentState') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentStateType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentComment') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentTag') {
                assertTrue(type.tags.contains('mainType'))
            }
        }
        assertEquals(6,mainTypeCount)
    }

    @Test
    void addMainTypeTagsMultipleModels() {
        def destFile = 'tmp/incident_mainTypeTags_multipleModels.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json',
                            'src/test/resources/test_schemas/ds/junction.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident_mainTypeTags.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.tagMainTypes = true
        doCodeGen.run()
        assertTrue(new File(destFile).exists())

        int mainTypeCount=0
        doCodeGen.dataModel.types.each { type ->
            if (type.tags.contains('mainType')) {
                mainTypeCount++
            }
            if (type.name=='Incident') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentState') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentStateType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentComment') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentTag') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='Junction') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='JunctionState') {
                assertTrue(type.tags.contains('mainType'))
            }
        }
        assertEquals(6,mainTypeCount)
    }

    @Test
    void addMainTypeTagsWithEnums() {
        def destFile = 'tmp/incident_mainTypeTags.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident_mainTypeTags.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.tagMainTypes = true
        doCodeGen.createEnumTypes = true
        doCodeGen.run()
        assertTrue(new File(destFile).exists())

        int mainTypeCount=0
        doCodeGen.dataModel.types.each { type ->
            if (type.tags.contains('mainType')) {
                mainTypeCount++
            }
            if (type.name=='Incident') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentState') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentStateType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentComment') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentTag') {
                assertTrue(type.tags.contains('mainType'))
            }
        }
        assertEquals(7,mainTypeCount)
    }

    @Test
    void addMainTypeTags_attrib() {
        def destFile = 'tmp/incident_mainTypeTags.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/incident.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=incident_mainTypeTags.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.tagMainTypes = true
        doCodeGen.mainTypeAttrib = 'guid'
        doCodeGen.run()
        assertTrue(new File(destFile).exists())

        int mainTypeCount=0
        doCodeGen.dataModel.types.each { type ->
            if (type.tags.contains('mainType')) {
                mainTypeCount++
            }
            if (type.name=='Incident') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentStateType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentComment') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentType') {
                assertTrue(type.tags.contains('mainType'))
            }
            if (type.name=='IncidentTag') {
                assertTrue(type.tags.contains('mainType'))
            }
        }
        assertEquals(5,mainTypeCount)
    }

    @Test
    void createLicenseModel() {
        def destFile = 'tmp/license.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/license.json']
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/plantuml.txt')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('destFileName=license.puml')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createUserModel_BuiltIn() {
        def destFile = 'tmp/userModel.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/user.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createJunctionModel_BuiltIn() {
        def destFile = 'tmp/junctionModel.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/junction2.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }
    // TODO remove - start
    @Test
    void test_Micha1() {
        def destFile = 'tmp/KnotenDaten.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/KnotenDaten.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('destFileName=KnotenDaten.puml')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }
    // TODO remove - end


    @Test
    void createFlorian_BuiltIn() {
        def destFile = 'tmp/device.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/Device.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void testHeavyReferenced() {
        def destFile = 'tmp/heavy_referenced.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/referenced_multi_types.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('destFileName=heavy_referenced.puml')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void testHeavyReferencedWithoutBaseTypes() {
        def destFile = 'tmp/heavy_referenced2.puml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/referenced_multi_types.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('destFileName=heavy_referenced2.puml')
        doCodeGen.generator_parameters.add('ignoreBaseTypes=true')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createUserModel_Markdown() {
        def destFile = 'tmp/user_puml.md'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ['src/test/resources/test_schemas/ds/user.json']
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('markdown=true')
        doCodeGen.generator_parameters.add('destFileName=user_puml.md')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    static void test_v4_base(def outputFileBase) {
        def destFile = "tmp/${outputFileBase}.puml"
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = ["src/test/resources/schemas/${outputFileBase}.json"]
        doCodeGen.generators.add('plantuml')
        doCodeGen.outputBaseDir = 'tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add("destFileName=${outputFileBase}.puml".toString())
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }


    @Test
    void test_v4() {
        test_v4_base("test_4")

        test_v4_base("CnResponseType")
        test_v4_base("CycCollectionType")
        test_v4_base("DetCollectionType")
        test_v4_base("GeoCollectionType")
        test_v4_base("LocCollectionType")
        test_v4_base("ProcessDataEvent")
        test_v4_base("StdCollectionType")
        test_v4_base("TimCollectionType")
        test_v4_base("Container")
    }

    @Test
    void test_v6() {
        test_v4_base("ramwa.schema")
        test_v4_base("notify")
    }

}