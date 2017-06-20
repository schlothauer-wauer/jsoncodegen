package de.lisaplus.atlas.codegen.helper.java

import de.lisaplus.atlas.model.Type

/**
 * Created by eiko on 20.06.17.
 */
class TypeToColor {
    static def colors = ['#3a5d0c', '#5d0c58', '#5d320c','#0c535d','#0c295d',
                         '#5d0c5c','#d81309','#b97c1c','#b3b91c','#228a22',
                         '#228a86','#22508a','#6b228a','#dc3a45','#dca53a',
                         '#1a796a','#2b1acb','#89038b','#9f9f9e','#28692c'
    ]

    static int akt = 0;

    static void setColor (Type type) {
        if (type.color=='#000000') {
            type.color = colors[akt % colors.size()]
            akt ++
        }
    }
}
