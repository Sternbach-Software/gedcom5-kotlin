package org.folg.gedcom.tools

import org.folg.gedcom.model.GedcomTag
import org.folg.gedcom.parser.*
import org.folg.gedcom.visitors.GedcomWriter
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXParseException
import java.io.*

/**
 * Convert a GEDCOM file or directory to the model and back again
 */
class CompareGedcom2Gedcom : ErrorHandler {
    @Option(name = "-i", required = true, usage = "file or directory containing gedcom files to convert")
    private val gedcomIn: File? = null

    @Option(name = "-o", required = false, usage = "temporary directory to store generated gedcom in")
    private val gedcomOut: File? = null


    private var totalGedcoms = 0
    private var equalsCount = 0
    private var expectedNotEqualsCount = 0
    private var unexpectedNotEqualsCount = 0
    private var hasError = false
    private var logWarningsErrors = false
    private var currentFilename: String? = null

    override fun warning(message: String?, lineNumber: Int) {
        if (logWarningsErrors) {
            logger.warn("$message @ $lineNumber")
        }
    }

    override fun error(message: String?, lineNumber: Int) {
        hasError = true
        if (logWarningsErrors) {
            logger.error("$message @ $lineNumber => $currentFilename")
        }
    }

    override fun fatalError(message: String?, lineNumber: Int) {
        // handled below
    }

    fun convertAndCompareGedcom(file: File?) {
        val parser = ModelParser()
        parser.setErrorHandler(this)
        val treeParser = TreeParser()
        treeParser.setErrorHandler(this)
        val writer = GedcomWriter()

        try {
            hasError = false
            logWarningsErrors = false
            val gedcom = parser.parseGedcom(file)

            val tempFile = (if (gedcomOut != null) File(gedcomOut, file!!.name) else File.createTempFile(
                file!!.name, "tmp"
            ))
            val out: OutputStream = FileOutputStream(tempFile)

            writer.write(gedcom, out)
            if (out != null) {
                out.close()
            }

            totalGedcoms++

            val gedcomTags = treeParser.parseGedcom(file)
            logWarningsErrors = true
            currentFilename = tempFile.name
            val compareGedcomTags = treeParser.parseGedcom(tempFile)

            if (equals(gedcomTags, compareGedcomTags)) {
                equalsCount++
            } else if (hasError) {
                expectedNotEqualsCount++
            } else {
                logger.warn("Unexpected not equal: " + file.name)
                unexpectedNotEqualsCount++
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

    @Throws(SAXParseException::class, IOException::class)
    private fun equals(gedcomTags: MutableList<GedcomTag?>?, compareGedcomTags: MutableList<GedcomTag?>?): Boolean {
        // merge CONC tags
        mergeContConcTags(gedcomTags)
        mergeContConcTags(compareGedcomTags)

        // remove empty tags
        removeEmptyTags(gedcomTags)
        removeEmptyTags(compareGedcomTags)

        // convert multiple TEXT tags into CONT tags
        convertMultipleTextTags(gedcomTags)

        // sort tags
       gedcomTags?.sortBy { it }
       compareGedcomTags?.sortBy { it }

        // check whether the lists of tags are equal
        if (gedcomTags!!.size != compareGedcomTags!!.size) {
            return false
        }
        for (indx in gedcomTags.indices) {
            val gedcomTagOne = gedcomTags[indx]
            val gedcomTagTwo = compareGedcomTags[indx]

            if (gedcomTagOne != gedcomTagTwo) {
                logger.info("file=" + currentFilename + " !tag=" + gedcomTagOne.toString() + "<=====>" + gedcomTagTwo.toString())
                return false
            }
        }

        return true
    }

    private fun mergeContConcTags(gedcomTags: List<GedcomTag?>?) {
        for (tag in gedcomTags!!) {
            mergeContConcTags(tag)
        }
    }

    private fun mergeContConcTags(tag: GedcomTag?) {
        val children = tag!!._children
        if (children!!.size > 0) {
            var i = 0
            while (i < children.size) {
                val child = children[i]
                mergeContConcTags(child)
                if ("CONC" == child?.tag || "CONT" == child?.tag) {
                    var concValue = if (child.value == null) "" else child.value
                    if ("CONT" == child.tag) {
                        concValue = """
                            
                            $concValue
                            """.trimIndent()
                    }
                    val target = (if (i == 0) tag else children[i - 1])
                    val value = if (target?.value == null) "" else target.value
                    target?.value = value + concValue
                    for (tagToMove in child!!._children ?: emptyList()) {
                        target!!.addChild(tagToMove)
                    }
                    children.removeAt(i)
                    i--
                }
                i++
            }
            if (tag._children?.size == 0) {
                tag._children = null
            }
        }
    }

    private fun removeEmptyTags(gedcomTags: MutableList<GedcomTag?>?) {
        val iter = gedcomTags!!.iterator()
        while (iter.hasNext()) {
            val tag = iter.next()
            removeEmptyTags(tag!!._children)
            if (tag.isEmpty) {
                iter.remove()
            }
        }
    }

    // convert geni's 3 TEXT ... 3 TEXT ... sequence into 3 TEXT ... 4 CONT ... which should be considered equivalent
    private fun convertMultipleTextTags(gedcomTags: MutableList<GedcomTag?>?) {
        val iter = gedcomTags!!.iterator()
        var prevTag: GedcomTag? = null
        while (iter.hasNext()) {
            val tag = iter.next()
            if ("TEXT" == tag?.tag) {
                if (prevTag != null && "TEXT" == prevTag.tag && prevTag._children?.size == 0) {
                    iter.remove()
                    tag.tag = "CONT"
                    prevTag.addChild(tag)
                }
            }
            prevTag = tag
            convertMultipleTextTags(tag!!._children)
        }
    }

    @Throws(FileNotFoundException::class)
    private fun doMain() {
        if (gedcomIn!!.isDirectory) {
            var cnt = 0
            for (file in gedcomIn.listFiles()) {
                convertAndCompareGedcom(file)
                if (++cnt % 100 == 0) {
                    print(".")
                }
            }
        } else if (gedcomIn.isFile) {
            convertAndCompareGedcom(gedcomIn)
        }

        println("Total Number of Gedcoms = $totalGedcoms")
        println("Number of Gedcoms that are Equal = $equalsCount")
        println("Expected number of Gedcoms that are NOT Equal = $expectedNotEqualsCount")
        println("UNEXPECTED number of Gedcoms that are NOT Equal = $unexpectedNotEqualsCount")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("org.folg.gedcom.tools")

        @Throws(FileNotFoundException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val self = CompareGedcom2Gedcom()
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
