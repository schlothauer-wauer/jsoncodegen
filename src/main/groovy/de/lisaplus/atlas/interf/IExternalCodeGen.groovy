package de.lisaplus.atlas.interf

import de.lisaplus.atlas.codegen.TemplateType
import de.lisaplus.atlas.model.Model

/**
 * Created by eiko on 02.06.17.
 */
interface IExternalCodeGen extends ICodeGen {
    /**
     * initialize the template file for the generator
     * @param templateFile
     * @param templateType
     */
    void initTemplateFromFile(String templateFile, TemplateType templateType)

    /**
     * initialize the template resource for the generator
     * @param templateResource
     * @param templateType
     */
    void initTemplateFromResource(String templateResource, TemplateType templateType)
}