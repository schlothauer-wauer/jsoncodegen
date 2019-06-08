package de.lisaplus.atlas.builder.test.jsonschema

import de.lisaplus.atlas.ModelTestHelper
import de.lisaplus.atlas.builder.JsonSchemaBuilder
import de.lisaplus.atlas.codegen.test.base.FileHelper
import de.lisaplus.atlas.model.Property
import de.lisaplus.atlas.model.StringType
import org.junit.Test

import static org.junit.Assert.*

/**
 * Created by eiko on 02.06.17.
 */
class ArrayTests {
    @Test
    void test_initModel() {
        def modelFile = new File('src/test/resources/test_schemas/ds/array_test_simple.json')
        assertTrue(modelFile.isFile())
        def builder = new JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(model.description,'Test user model')
        assertEquals('User model',model.title)
        assertEquals(6,model.types.size())
    }

    @Test
    void testContainsAttrib() {
        def destDir = 'tmp/array_tests'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/array_test_simple.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = [modelFile]
        doCodeGen.generators.add('java_beans')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test2.jsoncodegen.impl')
        doCodeGen.run()
/*
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/Role.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/User.java').exists())
        assertTrue(new File('tmp/java_beans/de/test2/jsoncodegen/impl/UserLog.java').exists())
        new File('tmp/java_beans/de/test2/jsoncodegen/impl').listFiles(new FileFilter() {
            @Override
            boolean accept(File file) {
                return file.isFile()
            }
        }).size()==3
*/
    }


    void test_initModel2() {
        def modelFile = new File('src/test/resources/test_schemas/ds/array_test.json')
        assertTrue(modelFile.isFile())
        def builder = new JsonSchemaBuilder()
        def model = builder.buildModel(modelFile)
        assertEquals(model.description,'Test user model')
        assertEquals('User model',model.title)
        assertEquals(7,model.types.size())
    }
}
