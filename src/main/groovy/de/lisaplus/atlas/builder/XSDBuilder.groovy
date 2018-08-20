package de.lisaplus.atlas.builder

import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import org.apache.xmlbeans.SchemaProperty
import org.apache.xmlbeans.SchemaType
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
        def globalTypes = sts.globalTypes()
        for (SchemaType type: globalTypes) {
            // local name of the type
            def localGlobalName =  type.getName().localPart
            def baseType = type.getBaseType().getName().localPart
            if (baseType) {
                println "type: $localGlobalName extends $baseType"
            }
            else {
                println "type: $localGlobalName"
            }
            def properties = type.getProperties()
            for (SchemaProperty prop: properties) {
                // how will we define type attributes as element or as XML attribute

                def propName = prop.getName().localPart
                def propType = prop.getType()
                println "    $propName: $propType"
            }
        }
        Model model = new Model()
        model.title = 'dummy'
        model.description = 'not implemented, yet'
        return model
    }
}
