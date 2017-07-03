package de.lisaplus.atlas.codegen.test

import junit.framework.Assert
import org.junit.Test

import static junit.framework.Assert.assertTrue
import static junit.framework.Assert.assertEquals

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

    @Test
    void test_extraProduces_1() {
        def destFile='tmp/user1.swagger'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/user.json'
        doCodeGen.generators.add('swagger')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('destFileName=user1.swagger')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('host=api.lisaplus.de')
        doCodeGen.generator_parameters.add('extraProduces=application/xml')
        doCodeGen.run()
        def createdFile = new File(destFile)
        assertTrue(createdFile.exists())
        def testResult = createdFile.text.indexOf('produces:\n  - application/json\n' +
                '  - application/xml\n'+
                'paths:')
        assertTrue(testResult!=-1)
    }

    @Test
    void test_extraProduces_2() {
        def destFile='tmp/user2.swagger'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.model='src/test/resources/test_schemas/ds/user.json'
        doCodeGen.generators.add('swagger')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('destFileName=user2.swagger')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('host=api.lisaplus.de')
        doCodeGen.generator_parameters.add('extraProduces=application/xml, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
        doCodeGen.run()
        def createdFile = new File(destFile)
        assertTrue(createdFile.exists())
        def testResult = createdFile.text.indexOf('produces:\n  - application/json\n' +
                '  - application/xml\n'+
                '  - application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n'+
                'paths:')
        assertTrue(testResult!=-1)
    }
}
