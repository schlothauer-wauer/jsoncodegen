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
        doCodeGen.models=['src/test/resources/test_schemas/ds/user.json']
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
        def destFile='tmp/userModel.swagger'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=['src/test/resources/test_schemas/ds/user.json']
        doCodeGen.generators.add('swagger')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('host=api.lisaplus.de')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createLicenseModel_BuiltIn() {
        def destFile='tmp/licenseModel.swagger'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=['src/test/resources/test_schemas/ds/license.json']
        doCodeGen.generators.add('swagger')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('host=api.lisaplus.de')
        doCodeGen.run()
        assertTrue(new File(destFile).exists())
    }

    @Test
    void createNotifyModel_BuiltIn() {
        def destFile='tmp/notify_swagger.yaml'
        de.lisaplus.atlas.DoCodeGen doCodeGen = new de.lisaplus.atlas.DoCodeGen()
        doCodeGen.models=['src/test/resources/schemas/notify.json']
        doCodeGen.generators.add('swagger')
        doCodeGen.outputBaseDir='tmp'
        doCodeGen.generator_parameters.add('destFileName=notify_swagger.yaml')
        doCodeGen.generator_parameters.add('containsAttrib=message')
        doCodeGen.generator_parameters.add('removeEmptyLines=true')
        doCodeGen.generator_parameters.add('host=notify.swarco.com')
        doCodeGen.run()
        assertTrue(destFile, new File(destFile).exists())
    }

}
