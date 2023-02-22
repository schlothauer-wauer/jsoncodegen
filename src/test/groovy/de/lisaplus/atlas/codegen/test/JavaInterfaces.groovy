package de.lisaplus.atlas.codegen.test

import de.lisaplus.atlas.codegen.test.base.FileHelper
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Tests the java interface generator and template
 * Created by eiko on 19.06.17.
 */
class JavaInterfaces {
    @Test
    void createFromUserModel() {
        def destDir = 'tmp/java_interfaces'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/user.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = [modelFile]
        doCodeGen.generators.add('java_interfaces')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test.jsoncodegen.impl')
        doCodeGen.run()
    }

    @Test
    void testMapObjMulti() {
        def destDir = 'tmp/java_interfaces'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/map_object_multi_type.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = [modelFile]
        doCodeGen.generators.add('java_interfaces')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test2.jsoncodegen.impl')
        doCodeGen.run()
    }

    @Test
    void testSimpleMapObj() {
        def destDir = 'tmp/java_interfaces'
        FileHelper.removeDirectoryIfExists(destDir)
        def modelFile = new File('src/test/resources/test_schemas/ds/base_types/simple_map_object.json')
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models = [modelFile]
        doCodeGen.generators.add('java_interfaces')
        doCodeGen.outputBaseDir = destDir
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('packageName=de.test3.jsoncodegen.impl')
        doCodeGen.run()
    }

}
