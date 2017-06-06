package de.lisaplus.atlas.codegen.test.base

import de.lisaplus.atlas.codegen.TemplateType
import groovy.text.Template
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.junit.Assert.assertNotNull

/**
 * Tests for de.lisaplus.atlas.codegen.GeneratorBase
 * Created by eiko on 06.06.17.
 */
class GeneratorBase {
    @Test
    void testFromFile_XmlTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateFile = 'TODO';
        Template t = dg.createTemplateFromFile(templateFile, TemplateType.Xml)
        assertNotNull(t)
    }

    @Test
    void testFromFile_GStringTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateFile = 'TODO';
        Template t = dg.createTemplateFromFile(templateFile, TemplateType.GString)
        assertNotNull(t)

    }

    @Test
    void testFromFile_MarkupTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateFile = 'TODO';
        Template t = dg.createTemplateFromFile(templateFile, TemplateType.Markup)
        assertNotNull(t)
    }

    @Test
    void testFromResource_XmlTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateResource = 'TODO';
        Template t = dg.createTemplateFromFile(templateResource, TemplateType.Xml)
        assertNotNull(t)
    }

    @Test
    void testFromResource_GStringTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateResource = 'TODO';
        Template t = dg.createTemplateFromFile(templateResource, TemplateType.GString)
        assertNotNull(t)
    }

    @Test
    void testFromResource_MarkupTemplate() {
        DummyGenerator dg = new DummyGenerator()
        def templateResource = 'TODO';
        Template t = dg.createTemplateFromFile(templateResource, TemplateType.Markup)
        assertNotNull(t)
    }
}

class DummyGenerator extends de.lisaplus.atlas.codegen.GeneratorBase {
    private static final Logger log=LoggerFactory.getLogger(DummyGenerator.class)


    Logger getLogger() {
        return log
    }

}
