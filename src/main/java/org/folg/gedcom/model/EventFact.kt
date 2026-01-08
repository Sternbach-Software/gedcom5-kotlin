/*
 * Copyright 2011 Foundation for On-Line Genealogy, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.folg.gedcom.model

import java.util.*

/**
 * User: Dallan
 * Date: 12/25/11
 *
 * omit: Phone, Age, Agency, Cause (except for death events)
 * add: Rin, Uid
 */
open class EventFact : SourceCitationContainer() {
    var value: String? = null
    var tag: String? = null
    var type: String? = null
    var date: String? = null
    var place: String? = null
    var address: Address? = null
    var phone: String? = null
    var fax: String? = null
    var rin: String? = null
    var cause: String? = null
    var uid: String? = null
    var uidTag: String? = null
    var email: String? = null
    var emailTag: String? = null
    var www: String? = null
    var wwwTag: String? = null

    val displayType: String
        /**
         * Return human-friendly event type
         * @return human-friendly event type
         */
        get() {
            val resourceBundle = ResourceBundle.getBundle("EventFact", Locale.getDefault())
            if (tag != null) {
                val key = DISPLAY_TYPE[tag!!.uppercase(Locale.getDefault())]
                if (key != null) {
                    return resourceBundle.getString(key)
                }
            }
            return resourceBundle.getString("other")
        }

    override fun visitContainedObjects(visitor: Visitor) {
        if (address != null) {
            address!!.accept(visitor)
        }
        super.visitContainedObjects(visitor)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            this.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }

    companion object {
        val PERSONAL_EVENT_FACT_TAGS: Set<String> = HashSet(
            mutableListOf(
                "ADOP",
                "ADOPTION",
                "ADULT_CHRISTNG",
                "AFN",
                "ARRI",
                "ARVL",
                "ARRIVAL",
                "_ATTR",
                "BAP",
                "BAPM",
                "BAPT",
                "BAPTISM",
                "BARM",
                "BAR_MITZVAH",
                "BASM",
                "BAS_MITZVAH",
                "BATM",
                "BAT_MITZVAH",
                "BIRT",
                "BIRTH",
                "BLES",
                "BLESS",
                "BLESSING",
                "BLSL",
                "BURI",
                "BURIAL",
                "CAST",
                "CASTE",
                "CAUS",
                "CENS",
                "CENSUS",
                "CHILDREN_COUNT",
                "CHR",
                "CHRA",
                "CHRISTENING",
                "CIRC",
                "CITN",
                "_COLOR",
                "CONF",
                "CONFIRMATION",
                "CREM",
                "CREMATION",
                "_DCAUSE",
                "DEAT",
                "DEATH",
                "_DEATH_OF_SPOUSE",
                "DEED",
                "_DEG",
                "_DEGREE",
                "DEPA",
                "DPRT",
                "DSCR",
                "DWEL",
                "EDUC",
                "EDUCATION",
                "_ELEC",
                "EMAIL",
                "EMIG",
                "EMIGRATION",
                "EMPL",
                "_EMPLOY",
                "ENGA",
                "ENLIST",
                "EVEN",
                "EVENT",
                "_EXCM",
                "EXCO",
                "EYES",
                "FACT",
                "FCOM",
                "FIRST_COMMUNION",
                "_FNRL",
                "_FUN",
                "_FA1",
                "_FA2",
                "_FA3",
                "_FA4",
                "_FA5",
                "_FA6",
                "_FA7",
                "_FA8",
                "_FA9",
                "_FA10",
                "_FA11",
                "_FA12",
                "_FA13",
                "GRAD",
                "GRADUATION",
                "HAIR",
                "HEIG",
                "_HEIG",
                "_HEIGHT",
                "IDNO",
                "IDENT_NUMBER",
                "_INTE",
                "ILL",
                "ILLN",
                "IMMI",
                "IMMIGRATION",
                "LVG",
                "LVNG",
                "MARR",
                "MARRIAGE_COUNT",
                "_MDCL",
                "_MEDICAL",
                "MIL",
                "_MIL",
                "MILA",
                "MILD",
                "MILI",
                "_MILI",
                "MILT",
                "_MILT",
                "_MILTID",
                "MISE",
                "_MISE",
                "_MILITARY_SERVICE",
                "MISN ",
                "_MISN",
                "MOVE",
                "_NAMS",
                "NATI",
                "NATIONALITY",
                "NATU",
                "NATURALIZATION",
                "NCHI",
                "NMR",
                "OCCU",
                "OCCUPATION",
                "ORDI",
                "ORDL",
                "ORDN",
                "ORDINATION",
                "PHON",
                "PHY_DESCRIPTION",
                "PROB",
                "PROBATE",
                "PROP",
                "PROPERTY",
                "RACE",
                "RELI",
                "RELIGION",
                "RESI",
                "RESIR",
                "RESIDENCE",
                "RETI",
                "RETIREMENT",
                "SEX",
                "SOC_SEC_NUMBER",
                "SSN",
                "STIL",
                "STLB",
                "TITL",
                "TITLE",
                "WEIG",
                "_WEIG",
                "_WEIGHT",
                "WILL"
            )
        )
        val FAMILY_EVENT_FACT_TAGS: Set<String> = HashSet(
            mutableListOf(
                "ANUL", "ANNULMENT",
                "CENS", "CLAW",
                "_DEATH_OF_SPOUSE", "DIV", "DIVF", "DIVORCE", "_DIV",
                "EMIG", "ENGA", "EVEN", "EVENT",
                "IMMI",
                "MARB", "MARC", "MARL", "MARR", "MARRIAGE", "MARS", "_MBON",
                "NCHI",
                "RESI",
                "SEPA", "_SEPR", "_SEPARATED"
            )
        )

            val DISPLAY_TYPE: Map<String, String>

        init {
            val m: MutableMap<String, String> = HashMap()
            m["ADOP"] = "adop"
            m["ADOPTION"] = "adop"
            m["AFN"] = "afn"
            m["ANUL"] = "anul"
            m["ANNULMENT"] = "anul"
            m["ARRIVAL"] = "arvl"
            m["ARRI"] = "arvl"
            m["ARVL"] = "arvl"
            m["_ATTR"] = "attr"
            m["BAP"] = "bapm"
            m["BAPM"] = "bapm"
            m["BAPT"] = "bapm"
            m["BAPTISM"] = "bapm"
            m["BARM"] = "barm"
            m["BAR_MITZVAH"] = "barm"
            m["BATM"] = "batm"
            m["BIRT"] = "birt"
            m["BIRTH"] = "birt"
            m["BLES"] = "bles"
            m["BURI"] = "buri"
            m["BURIAL"] = "buri"
            m["CAST"] = "cast"
            m["CAUS"] = "caus"
            m["CAUSE"] = "caus"
            m["CENS"] = "cens"
            m["CHR"] = "chr"
            m["CHRISTENING"] = "chr"
            m["CLAW"] = "claw"
            m["_COLOR"] = "color"
            m["CONF"] = "conf"
            m["CREM"] = "crem"
            m["_DCAUSE"] = "caus"
            m["DEAT"] = "deat"
            m["DEATH"] = "deat"
            m["_DEATH_OF_SPOUSE"] = "death_of_spouse"
            m["DEED"] = "deed"
            m["_DEG"] = "deg"
            m["_DEGREE"] = "deg"
            m["DEPA"] = "dprt"
            m["DPRT"] = "dprt"
            m["DIV"] = "div"
            m["DIVF"] = "divf"
            m["DIVORCE"] = "div"
            m["_DIV"] = "div"
            m["DSCR"] = "dscr"
            m["EDUC"] = "educ"
            m["EDUCATION"] = "educ"
            m["_ELEC"] = "elec"
            m["EMAIL"] = "email"
            m["EMIG"] = "emig"
            m["EMIGRATION"] = "emig"
            m["EMPL"] = "empl"
            m["_EMPLOY"] = "empl"
            m["ENGA"] = "enga"
            m["ENLIST"] = "milt"
            m["EVEN"] = "even"
            m["EVENT"] = "even"
            m["_EXCM"] = "excm"
            m["EYES"] = "eyes"
            m["FCOM"] = "fcom"
            m["_FNRL"] = "fnrl"
            m["_FUN"] = "fnrl"
            m["GRAD"] = "grad"
            m["GRADUATION"] = "grad"
            m["HAIR"] = "hair"
            m["HEIG"] = "heig"
            m["_HEIG"] = "heig"
            m["_HEIGHT"] = "heig"
            m["ILL"] = "ill"
            m["IMMI"] = "immi"
            m["IMMIGRATION"] = "immi"
            m["MARB"] = "marb"
            m["MARC"] = "marc"
            m["MARL"] = "marl"
            m["MARR"] = "marr"
            m["MARRIAGE"] = "marr"
            m["MARS"] = "mars"
            m["_MBON"] = "marb"
            m["_MDCL"] = "mdcl"
            m["_MEDICAL"] = "mdcl"
            m["MIL"] = "milt"
            m["_MIL"] = "milt"
            m["MILI"] = "milt"
            m["_MILI"] = "milt"
            m["_MILT"] = "milt"
            m["_MILTID"] = "milt"
            m["_MILITARY_SERVICE"] = "milt"
            m["MISE"] = "milt"
            m["_MISN"] = "misn"
            m["_NAMS"] = "nams"
            m["NATI"] = "nati"
            m["NATU"] = "natu"
            m["NATURALIZATION"] = "natu"
            m["NCHI"] = "nchi"
            m["OCCU"] = "occu"
            m["OCCUPATION"] = "occu"
            m["ORDI"] = "ordn"
            m["ORDN"] = "ordn"
            m["PHON"] = "phon"
            m["PROB"] = "prob"
            m["PROP"] = "prop"
            m["RELI"] = "reli"
            m["RELIGION"] = "reli"
            m["RESI"] = "resi"
            m["RESIDENCE"] = "resi"
            m["RETI"] = "reti"
            m["SEPA"] = "sepa"
            m["_SEPARATED"] = "sepa"
            m["_SEPR"] = "sepa"
            m["SEX"] = "sex"
            m["SSN"] = "ssn"
            m["SOC_SEC_NUMBER"] = "ssn"
            m["TITL"] = "titl"
            m["TITLE"] = "titl"
            m["_WEIG"] = "weig"
            m["_WEIGHT"] = "weig"
            m["WILL"] = "will"
            DISPLAY_TYPE = Collections.unmodifiableMap(m)
        }

        @Deprecated("")
        val OTHER_TYPE: String = "Other"
    }
}
