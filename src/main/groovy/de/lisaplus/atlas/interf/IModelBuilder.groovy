package de.lisaplus.atlas.interf

import de.lisaplus.atlas.model.Model

/**
 * Created by eiko on 02.06.17.
 */
interface IModelBuilder {
    Model buildModel(File modelFile)
}