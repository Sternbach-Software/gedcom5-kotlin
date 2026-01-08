package org

import org.folg.gedcom.model.Family
import org.folg.gedcom.model.Gedcom

fun main(args: Array<String>) {
    val family = Family()
    family.id = ""
    val gedcom = Gedcom()
    gedcom.addFamily(family)
    println(gedcom)
}