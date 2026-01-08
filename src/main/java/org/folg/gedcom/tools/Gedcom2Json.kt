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
package org.folg.gedcom.tools

import org.folg.gedcom.parser.JsonParser
import org.folg.gedcom.parser.ModelParser
import org.folg.gedcom.parser.TreeParser
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.xml.sax.SAXParseException
import java.io.File
import java.io.IOException
import java.io.PrintWriter

/**
 * User: Dallan
 * Date: 1/1/12
 */
class Gedcom2Json {
    @Option(name = "-i", required = true, usage = "gedcom file in")
    private val gedcomIn: File? = null

    @Option(name = "-o", required = false, usage = "json file out")
    private val jsonOut: File? = null

    @Option(name = "-t", required = false, usage = "use tree parser (use model parser by default)")
    private val useTreeParser = false

    @Throws(SAXParseException::class, IOException::class)
    private fun doMain() {
        val json: String
        val jsonParser = JsonParser()
        if (useTreeParser) {
            val treeParser = TreeParser()
            val gedcomTags = treeParser.parseGedcom(gedcomIn)
            json = jsonParser.toJson(gedcomTags)
        } else {
            val modelParser = ModelParser()
            val gedcom = modelParser.parseGedcom(gedcomIn)
            json = jsonParser.toJson(gedcom)
        }
        if (jsonOut != null) {
            val writer = PrintWriter(jsonOut)
            writer.println(json)
            writer.close()
        } else {
            println(json)
        }
    }

    companion object {
        @Throws(SAXParseException::class, IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val self = Gedcom2Json()
            val parser = CmdLineParser(self)
            try {
                parser.parseArgument(*args)
                self.doMain()
            } catch (e: CmdLineException) {
                System.err.println(e.message)
                parser.printUsage(System.err)
            }
        }
    }
}
