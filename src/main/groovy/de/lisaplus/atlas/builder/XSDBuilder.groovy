package de.lisaplus.atlas.builder

import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import org.apache.xmlbeans.SchemaTypeSystem
import org.apache.xmlbeans.XmlBeans
import org.apache.xmlbeans.XmlObject

import static de.lisaplus.atlas.builder.helper.BuildHelper.strFromMap
import static de.lisaplus.atlas.builder.helper.BuildHelper.strFromMap

class XSDBuilder implements IModelBuilder {
    @Override
    Model buildModel(File modelFile) {
        XmlObject object = XmlObject.Factory.parse(modelFile)
        List<XmlObject> objectList = []
        objectList.add(object)
        SchemaTypeSystem sts = XmlBeans.compileXsd((XmlObject[])objectList.toArray(), XmlBeans.getBuiltinTypeSystem(), null);
        println sts
        Model model = new Model()
        model.title = 'dummy'
        model.description = 'not implemented, yet'
        return model
    }
}
