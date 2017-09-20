package de.lisaplus.atlas.codegen.meta

import de.lisaplus.atlas.codegen.SingleFileGenarator
import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static de.lisaplus.atlas.builder.helper.BuildHelper.string2Name

/**
 * Created by eiko on 05.06.17.
 */
class SwaggerGeneratorExt extends SwaggerGenerator {
    private static final Logger log=LoggerFactory.getLogger(SwaggerGeneratorExt.class)

    @Override
    void initTemplate() {
        template = createTemplateFromResource('templates/meta/swagger_ext.txt',TemplateType.GString)
    }
}
