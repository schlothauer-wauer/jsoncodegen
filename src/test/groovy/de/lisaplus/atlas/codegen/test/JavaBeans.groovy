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
        doCodeGen.model = modelFile
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
        doCodeGen.model = modelFile
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
        doCodeGen.model = modelFile
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
        doCodeGen.model = modelFile
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('containsAttrib=domain_id')
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
        doCodeGen.model = modelFile
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
}
