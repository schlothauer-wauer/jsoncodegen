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

    static String string2Name(String s) {
        return s.replaceAll('[^a-zA-Z0-9]','_')
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
