package org.folg.gedcom.tools

import org.folg.gedcom.parser.ModelParser
import org.folg.gedcom.visitors.GedcomWriter
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXParseException
import java.io.*

/**
 * User: dallan
 * Date: 1/7/12
 *
 * Convert a GEDCOM file or directory to the model and back again
 */
class Gedcom2Gedcom {
    @Option(name = "-i", required = true, usage = "file or directory containing gedcom files to convert")
    private val gedcomIn: File? = null

    @Option(name = "-o", required = false, usage = "target directory")
    private val gedcomOut: File? = null

    private val parser = ModelParser()
    private val writer = GedcomWriter()

    fun convertGedcom(file: File?) {
        try {
            val gedcom = parser.parseGedcom(file)
            val out =
                (if (gedcomOut != null) FileOutputStream(File(gedcomOut, file!!.name)) else ByteArrayOutputStream())
            writer.write(gedcom, out)
            if (gedcomOut != null) {
                out.close()
            } else {
                println(out.toString())
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

    @Throws(FileNotFoundException::class)
    private fun doMain() {
        if (gedcomIn!!.isDirectory) {
            for (file in gedcomIn.listFiles()) {
                convertGedcom(file)
            }
        } else if (gedcomIn.isFile) {
            convertGedcom(gedcomIn)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("org.folg.gedcom.tools")

        @Throws(FileNotFoundException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val self = Gedcom2Gedcom()
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
