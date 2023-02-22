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
            return ret.substring(0,1).toUpperCase()+makeCamelCase(ret.substring(1))
        }
        else {
            return ret.substring(0,1).toLowerCase()+makeCamelCase(ret.substring(1))
        }
    }

    static String makeCamelCase(String s) {
        if (!s) return s
        int i = s.indexOf('_')
        int txtLen = s.length()
        while (i!=-1) {
            if (i<txtLen) {
                String nextChar = s.substring(i+1,i+2)
                nextChar = nextChar.toUpperCase()
                String tmp = s.substring(0,i)
                tmp += nextChar
                if (i<txtLen-1) {
                    tmp += s.substring(i+2)
                }
                s = tmp
                i = s.indexOf('_')
            }
            else
                break
        }
        return s
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
