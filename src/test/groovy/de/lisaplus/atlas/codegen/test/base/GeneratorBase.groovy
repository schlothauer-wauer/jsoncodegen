package de.lisaplus.atlas.codegen.test.base

import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.model.Model
import groovy.text.Template
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static junit.framework.Assert.fail
import static junit.framework.TestCase.assertNull
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * Tests for de.lisaplus.atlas.codegen.GeneratorBase
 * Created by eiko on 06.06.17.
 */
class GeneratorBase {
    @Test
    void testFromFile_GStringTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateFile = 'src/test/resources/templates/HelloWorld_GString.tmpl';
        Template t = dg.createTemplateFromFile(templateFile, TemplateType.GString)
        assertNotNull(t)

    }

    @Test
    void testFromFile_MarkupTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateFile = 'src/test/resources/templates/HelloWorld_Markup.tmpl';
        Template t = dg.createTemplateFromFile(templateFile, TemplateType.Markup)
        assertNotNull(t)
    }

    @Test
    void testFromFile_Fail() {
        DummyGenerator dg = new DummyGenerator()
        def templateFile = 'src/test/resources/templates/xxxx.tmpl';
        try {
            Template t = dg.createTemplateFromFile(templateFile, TemplateType.Markup)
            fail('no exception while load missing file')
        }
        catch(Exception e) {
            assertEquals('given template filename is not file: src/test/resources/templates/xxxx.tmpl',e.message)
        }
    }

    @Test
    void testFromResource_GStringTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateResource = 'templates/HelloWorld_GString.tmpl';
        Template t = dg.createTemplateFromResource(templateResource, TemplateType.GString)
        assertNotNull(t)
    }

    @Test
    void testFromResource_MarkupTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateResource = 'templates/HelloWorld_Markup.tmpl';
        Template t = dg.createTemplateFromResource(templateResource, TemplateType.Markup)
        assertNotNull(t)
    }

    @Test
    void testFromResource_Fail() {
        DummyGenerator dg = new DummyGenerator()
        def templateResource = 'templates/xxxx.tmpl';
        try {
            Template t = dg.createTemplateFromResource(templateResource, TemplateType.Markup)
            fail('no exception while load missing file')
        }
        catch(Exception e) {
            assertEquals('given template resource not found: templates/xxxx.tmpl',e.message)
        }
    }

}

class DummyGenerator extends de.lisaplus.atlas.codegen.GeneratorBase {
    private static final Logger log=LoggerFactory.getLogger(DummyGenerator.class)

    @Override
    String getDestFileName(Model dataModel, Map<String, String> extraParameters) {
        return null
    }

    @Override
    String getDestDir(Model dataModel, String outputBasePath, Map<String, String> extraParameters) {
        return null
    }

    Logger getLogger() {
        return log
    }

}
