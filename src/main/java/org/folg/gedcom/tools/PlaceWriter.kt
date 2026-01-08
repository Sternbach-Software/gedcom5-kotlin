package org.folg.gedcom.tools

import org.folg.gedcom.model.EventFact
import org.folg.gedcom.model.Visitor
import org.folg.gedcom.parser.ModelParser
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXParseException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

/**
 * User: dallan
 * Date: 1/7/12
 */
class PlaceWriter : Visitor() {
    @Option(name = "-i", required = true, usage = "file or directory containing gedcom files to process")
    private val gedcomIn: File? = null

    @Option(name = "-o", required = true, usage = "target file for places")
    private val placesOut: File? = null

    private val parser = ModelParser()
    private var out: PrintWriter? = null

    override fun visit(eventFact: EventFact): Boolean {
        val place = eventFact.place
        if (place != null && place?.isNotEmpty() == true) {
            out!!.println(place)
        }
        return true
    }

    @Throws(SAXParseException::class, IOException::class)
    fun processGedcom(file: File?) {
        val gedcom = parser.parseGedcom(file)
        gedcom!!.accept(this)
    }

    @Throws(IOException::class, SAXParseException::class)
    private fun doMain() {
        out = PrintWriter(FileWriter(placesOut))
        if (gedcomIn!!.isDirectory) {
            for (file in gedcomIn.listFiles()) {
                processGedcom(file)
            }
        } else if (gedcomIn.isFile) {
            processGedcom(gedcomIn)
        }
        out!!.close()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("org.folg.gedcom.tools")

        @Throws(IOException::class, SAXParseException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val self = PlaceWriter()
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
