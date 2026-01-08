package org.gedml

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * User: npowell
 * Date: May 2, 2007
 */
class GedcomLineParser {
    private var m: Matcher? = null

    fun parse(line: String?): Boolean {
        m = pGedcomLine.matcher(line)
        return m!!.find()
    }

    val level: String
        get() = m!!.group(LEVEL_GROUP)

    val xRef: String
        get() = m!!.group(XREF_GROUP)

    val iD: String
        get() = m!!.group(ID_GROUP)
    // TODO - necessary?
//      String returnValue = m.group(AFTER_ID_GROUP);
//      if (returnValue == null || returnValue.length() == 0) {
//         return returnValue;
//      } else {
//         return returnValue.replaceAll("[|#/]+", "");
//      }

    val tag: String
        get() = m!!.group(TAG_GROUP)

    val value: String
        get() = m!!.group(VALUE_GROUP)

    companion object {
        private val pGedcomLine: Pattern = Pattern.compile(
            "^\\s*(\\d)\\s+(@([^@ ]+)@\\s+)?([a-zA-Z_0-9.]+)(\\s+@([^@ ]+)@)?(\\s(.*))?$", Pattern.DOTALL
        ) // DOTALL for unicode line separator

        private const val LEVEL_GROUP = 1
        private const val ID_GROUP = 3
        private const val TAG_GROUP = 4
        private const val XREF_GROUP = 6
        private const val VALUE_GROUP = 8
    }
}
