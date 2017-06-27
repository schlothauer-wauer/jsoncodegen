package de.lisaplus.atlas.codegen.test

import org.junit.Test

import static junit.framework.Assert.assertTrue

/**
 * Tests the swagger generator and the swagger template
 * Created by eiko on 21.06.17.
 */
class Swagger {
    @Test
    void createUserModel_SingleFile() {
        def destFile='tmp/user.swagger'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/user.json'
        doCodeGen.generators.add('singlefile=src/main/resources/templates/meta/swagger_file.txt')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('destFileName=user.swagger')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('host=api.lisaplus.de')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createUserModel_BuiltIn() {
        def destFile='tmp/user_model.swagger'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/user.json'
        doCodeGen.generators.add('swagger')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('host=api.lisaplus.de')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

}
