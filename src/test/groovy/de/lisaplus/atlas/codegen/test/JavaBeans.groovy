package de.lisaplus.atlas.codegen.test

import de.lisaplus.atlas.codegen.test.base.FileHelper
import org.junit.Test

import static junit.framework.Assert.assertTrue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

/**
 * Tests the java bean generator and template
 * Created by eiko on 19.06.17.
 */
class JavaBeans {
    @Test
    void createFromUserModel() {
        def destDir = 'tmp/java_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test.jsoncodegen.impl')
        doCodeGen.run()
    }

    @Test
    void createFromXsd() {
        def destDir = 'tmp/xsd_java_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/xsd/ui-tlc.xsd')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test.jsoncodegen.impl')
        doCodeGen.run()
    }

    @Test
    void testMapObjMulti() {
        def destDir = 'tmp/java_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/map_object_multi_type.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test2.jsoncodegen.impl')
        doCodeGen.run()
    }

    @Test
    void testSimpleMapObj() {
        def destDir = 'tmp/java_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/simple_map_object.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test3.jsoncodegen.impl')
        doCodeGen.run()
    }

    @Test
    void testContainsAttrib() {
        def destDir = 'tmp/java_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('containsAttrib=domainId')
        doCodeGen.generator_parameters.add('packageName=de.test2.jsoncodegen.impl')
        doCodeGen.run()
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/Role.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/User.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/UserLog.java').exists())
        new File('tmp/java_beans/de/test2/jsoncodegen/impl').listFiles(new FileFilter() {
            @Override
            boolean accept(File file) {
                return file.isFile()
            }
        }).size()==3
    }

    @Test
    void testMissingAttrib() {
        def destDir = 'tmp/java_beans'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('missingAttrib=domain_id')
        doCodeGen.generator_parameters.add('packageName=de.test2.jsoncodegen.impl')
        doCodeGen.run()
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/Domain.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/AppModule.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/Application.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/RoleDataGrantsItem.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/RoleModuleGrantsItem.java').exists())

        new File('tmp/java_beans/de/test2/jsoncodegen/impl').listFiles(new FileFilter() {
            @Override
            boolean accept(File file) {
                return file.isFile()
            }
        }).size()==5
    }

    @Test
    void testIgnoreTag() {
        def destDir = 'tmp/java_beans2'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/incident.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('versionConst=true')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('ignoreTag=rest')
        doCodeGen.generator_parameters.add('packageName=de.test3')
        doCodeGen.run()

        assertFalse(new File('tmp/java_beans2/de/test3/Incident.java').exists())
    }

    @Test
    void testNeededTag() {
        def destDir = 'tmp/java_beans3'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/incident.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.tagMainTypes = true
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('neededTag=selList')
        doCodeGen.generator_parameters.add('packageName=de.test3')
        doCodeGen.run()

        assertFalse (new File('tmp/java_beans3/de/test3/Incident.java').exists())
        assertFalse (new File('tmp/java_beans3/de/test3/IncidentComment.java').exists())
        assertFalse (new File('tmp/java_beans3/de/test3/IncidentTag.java').exists())
        assertTrue (new File('tmp/java_beans3/de/test3/IncidentType.java').exists())

        assertFalse(new File('tmp/java_beans3/de/test3/Comment.java').exists())
        assertFalse(new File('tmp/java_beans3/de/test3/Domain.java').exists())
        assertFalse(new File('tmp/java_beans3/de/test3/Tag.java').exists())
    }

    @Test
    void testNeededTag2() {
        def destDir = 'tmp/java_beans3'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/incident.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.tagMainTypes = true
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('neededTag=mainType:rest')
        doCodeGen.generator_parameters.add('packageName=de.test3')
        doCodeGen.run()

        assertTrue (new File('tmp/java_beans3/de/test3/Incident.java').exists())
        assertTrue (new File('tmp/java_beans3/de/test3/IncidentComment.java').exists())
        assertTrue (new File('tmp/java_beans3/de/test3/IncidentTag.java').exists())
        assertTrue (new File('tmp/java_beans3/de/test3/IncidentType.java').exists())

        assertFalse (new File('tmp/java_beans3/de/test3/Comment.java').exists())
        assertFalse (new File('tmp/java_beans3/de/test3/Domain.java').exists())
        assertFalse (new File('tmp/java_beans3/de/test3/Tag.java').exists())
    }

    @Test
    void testBlackList() {
        def destDir = 'tmp/java_beans_black_list'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.blackListed = ['Domain','Application','App_module','Role']
        doCodeGen.generator_parameters.add('packageName=de.test')
        doCodeGen.run()
        assertTrue(new File('tmp/java_beans_black_list/de/test/RoleDataGrantsItem.java').exists())
        assertTrue(new File('tmp/java_beans_black_list/de/test/RoleModuleGrantsItem.java').exists())
        assertTrue(new File('tmp/java_beans_black_list/de/test/User.java').exists())
        assertTrue(new File('tmp/java_beans_black_list/de/test/UserLog.java').exists())
        new File('tmp/java_beans_black_list/de/test').listFiles(new FileFilter() {
            @Override
            boolean accept(File file) {
                return file.isFile()
            }
        }).size()==4
    }

    @Test
    void testWhiteList() {
        def destDir = 'tmp/java_beans_white_list'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=[modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.whiteListed = ['User','Role']
        doCodeGen.generator_parameters.add('packageName=de.test')
        doCodeGen.run()
        assertTrue(new File('tmp/java_beans_white_list/de/test/Role.java').exists())
        assertTrue(new File('tmp/java_beans_white_list/de/test/User.java').exists())
        new File('tmp/java_beans_white_list/de').listFiles(new FileFilter() {
            @Override
            boolean accept(File file) {
                return file.isFile()
            }
        }).size()==2
    }

}
