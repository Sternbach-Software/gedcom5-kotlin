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

import org.folg.gedcom.parser.*
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXParseException
import java.io.*

/**
 * User: dallan
 * Date: 1/2/12
 */
class GedcomAnalyzer : ErrorHandler {
    @Option(name = "-i", required = true, usage = "file or directory containing gedcom files to analyze")
    private val gedcomIn: File? = null

    @Option(name = "-w", required = false, usage = "warnings file out")
    private val warningsOut: File? = null

    @Option(name = "-e", required = false, usage = "errors file out")
    private val errorsOut: File? = null

    private val parser = ModelParser()
    private var warnings: MutableSet<String?>? = null
    private var errors: MutableSet<String?>? = null
    private val ccWarnings: CountsCollector
    private val ccErrors: CountsCollector
    private var cntWarnings: Int
    private var cntErrors: Int
    private val cntTotal: Int
    private var file: File? = null

    init {
        parser.setErrorHandler(this)
        ccWarnings = CountsCollector()
        ccErrors = CountsCollector()
        cntTotal = 0
        cntWarnings = 0
        cntErrors = 0
    }

    override fun warning(message: String?, lineNumber: Int) {
        logger.info("$message @ $lineNumber")
        warnings!!.add(message)
    }

    override fun error(message: String?, lineNumber: Int) {
        logger.warn("$message @ $lineNumber")
        errors!!.add(message)
    }

    override fun fatalError(message: String?, lineNumber: Int) {
        // we'll log this below
    }

    fun analyzeGedcom(gedcomFile: File?) {
        warnings = HashSet()
        errors = HashSet()
        this.file = gedcomFile

        try {
            parser.parseGedcom(gedcomFile)
            if (warnings!!.size > 0 || errors!!.size > 0) {
                cntWarnings++
            }
            if (errors!!.size > 0) {
                cntErrors++
            }
            for (warning in warnings!!) {
                ccWarnings.add(warning)
            }
            for (error in errors!!) {
                ccErrors.add(error)
            }
        } catch (e: SAXParseException) {
            logger.error("SaxParseException for file: " + file!!.name + " " + e.message + " @ " + e.lineNumber)
        } catch (e: IOException) {
            logger.error("IOException for file: " + file!!.name + " " + e.message)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            logger.error("Exception for file: " + file!!.name + " " + e.message)
        }
    }

    fun getWarnings(): CountsCollector = ccWarnings

    fun getErrors(): CountsCollector = ccErrors

    @Throws(FileNotFoundException::class)
    private fun doMain() {
        if (gedcomIn!!.isDirectory) {
            for (file in gedcomIn.listFiles()) {
                analyzeGedcom(file)
            }
        } else if (gedcomIn.isFile) {
            analyzeGedcom(gedcomIn)
        }

        println("Total=$cntTotal with warnings=$cntWarnings with errors=$cntErrors")
        getWarnings().writeSorted(
            false,
            1,
            if (warningsOut != null) PrintWriter(warningsOut) else PrintWriter(System.out)
        )
        getErrors().writeSorted(false, 1, if (errorsOut != null) PrintWriter(errorsOut) else PrintWriter(System.out))
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("org.folg.gedcom.tools")

        @Throws(FileNotFoundException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val self = GedcomAnalyzer()
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
