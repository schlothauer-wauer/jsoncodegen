package de.lisaplus.atlas.interf

import de.lisaplus.atlas.model.Model

/**
 * Created by eiko on 02.06.17.
 */
interface ICodeGen {
    void doCodeGen(Model model, Map<String,String> extraParams)
}