package de.lisaplus.atlas.builder

import de.lisaplus.atlas.interf.IModelBuilder
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
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
        Model model = new Model()
        model.title = 'dummy'
        model.description = 'not implemented, yet'
        for (SchemaType type: globalTypes) {
            if (type.contentType==SchemaType.DT_NOT_DERIVED) {
//                println "ignored type: ${type.getName().localPart}, baseType: ${type.getBaseType().name.localPart}"
                def baseName = type.getBaseType().name.localPart
                switch(baseName) {
                    case 'token':
                        // TODO is basically a string
                        break;
                    case 'string':
                        // TODO
                        break
                    case 'long':
                        // TODO
                        break
                    case 'int':
                        // TODO
                        break
                    case 'integer':
                        // TODO
                        break
                    case 'decimal':
                        // TODO
                        break
                    case 'float':
                        // TODO
                        break
                    case 'double':
                        // TODO
                        break
                    case 'dateTime':
                        // TODO
                        break
                    case 'date':
                        // TODO
                        break
                    case 'time':
                        // TODO
                        break
                    default:
                        println "unknown restriction type: $baseName"
                }
            }
            else {
                Type newType = new Type()
                newType.name = type.getName().localPart
                if (model.types.contains(newType)) {
                    continue;
                }
                newType.description = getDescription(type)
                newType.baseTypes.add (type.baseType.getName().localPart)

                // local name of the type
                /*
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
                */
                model.types.add(newType)
                //println "added type: ${newType.name}, contentType: ${type.contentType}"
            }
        }
        return model
    }

    private String getDescription(elem) {
        def docuTxt = null
        def annotation = elem.getAnnotation()
        if (annotation!=null) {
            def userInfos = annotation.userInformation
            for (XmlObject userInfo: userInfos ) {
                if (userInfo.getDomNode().localName=='documentation') {
                    if (docuTxt!=null) {
                        docuTxt+=' '
                    }
                    else {
                        docuTxt=''
                    }
                    docuTxt += userInfo.getDomNode().getFirstChild().getNodeValue().replaceAll('\\W+',' ').trim()
                }
            }
        }
        return docuTxt
    }
}
