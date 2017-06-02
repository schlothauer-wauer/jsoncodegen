package de.lisaplus.atlas.builder.helper

/**
 * Created by eiko on 01.06.17.
 */
class BuildHelper {
    static String strFromMap(def map,String key,String defValue=null) {
        def v = map[key]
        if (!v) v = defValue
        return v
    }

    static String string2Name(String s,boolean firstUpper=true) {
        def ret = s.replaceAll('[^a-zA-Z0-9]','_')
        if (firstUpper) {
            return ret.substring(0,1).toUpperCase()+ret.substring(1)
        }
        else
            return ret.substring(0,1).toLowerCase()+ret.substring(1)
    }

    static List listFromMap(def map,String key) {
        def listObj = map[key]
        if (!listObj) {
            return []
        }
        else {
            return listObj
        }

    }


}
