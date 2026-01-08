package org.gedml

import org.xml.sax.*
import org.xml.sax.helpers.AttributesImpl
import java.io.*
import java.net.URL
import java.util.*

/**
 * This class is designed to look like a SAX2-compliant XML parser; however,
 * it takes GEDCOM as its input rather than XML.
 * The events sent to the ContentHandler reflect the GEDCOM input "as is";
 * there is no validation or conversion of tags.
 *
 * @author mhkay@iclway.co.uk
 * @version 22 March 2006 - revised by lmonson.com to support string inlining, a few sax feature settings and additional encodings
 * Also revised in 2007 by Nathan Powell and revised in 2011 by Dallan Quass
 */
class GedcomParser : XMLReader, Locator {
    private var contentHandler: ContentHandler? = null
    private var errorHandler: ErrorHandler? = null
    private val emptyAttList = AttributesImpl()
    private val attList = AttributesImpl()
    private var entityResolver: EntityResolver? = null
    private var systemId: String? = null
    private var lineNr = 0

    /**
     * Set the ContentHandler
     *
     * @param handler User-supplied content handler
     */
    override fun setContentHandler(handler: ContentHandler) {
        contentHandler = handler
    }

    /**
     * Get the ContentHandler
     */
    override fun getContentHandler(): ContentHandler {
        return contentHandler!!
    }

    /**
     * Set the entityResolver.
     * This call has no effect, because entities are not used in GEDCOM files.
     */
    override fun setEntityResolver(er: EntityResolver) {
        entityResolver = er
    }

    /**
     * Get the entityResolver
     */
    override fun getEntityResolver(): EntityResolver {
        return entityResolver!!
    }

    /**
     * Set the DTDHandler
     * This call has no effect, because DTDs are not used in GEDCOM files.
     */
    override fun setDTDHandler(dh: DTDHandler) {
    }

    /**
     * Get the DTDHandler
     */
    override fun getDTDHandler(): DTDHandler? {
        return null
    }

    /**
     * Set the error handler
     *
     * @param eh A user-supplied error handler
     */
    override fun setErrorHandler(eh: ErrorHandler) {
        errorHandler = eh
    }

    /**
     * Get the error handler
     */
    override fun getErrorHandler(): ErrorHandler {
        return errorHandler!!
    }

    @Throws(IOException::class)
    private fun getBufferedReader(`in`: InputStream): BufferedReader {
        var `in` = `in`
        if (!`in`.markSupported()) {
            `in` = BufferedInputStream(`in`)
        }
        `in`.mark(Int.MAX_VALUE)

        var charEncoding = readCorrectedCharsetName(`in`)
        `in`.reset()

        if (charEncoding.length == 0) {
            // Let's try again with a UTF-16 reader.
            val br = BufferedReader(InputStreamReader(`in`, "UTF-16"))
            charEncoding = readCorrectedCharsetName(br)
            `in`.reset()

            if (charEncoding == "UTF-16") {
                // skip over junk at the beginning of the file
                var reader = InputStreamReader(`in`, "UTF-16")
                var cnt = 0
                var c: Int
                while ((reader.read().also { c = it }) != '0'.code && c != -1) {
                    cnt++
                }

                `in`.reset()
                reader = InputStreamReader(`in`, "UTF-16")
                for (i in 0 until cnt) {
                    reader.read()
                }
                return BufferedReader(reader)
            }
        }

        if (charEncoding.length == 0) {
            charEncoding = "ANSEL" // default
        }

        // skip over junk at the beginning of the file
        `in`.reset()
        var cnt = 0
        var c: Int
        while ((`in`.read().also { c = it }) != '0'.code && c != -1) {
            cnt++
        }

        `in`.reset()
        for (i in 0 until cnt) {
            `in`.read()
        }

        val reader: InputStreamReader
        reader = if (charEncoding == "ANSEL") {
            AnselInputStreamReader(`in`)
        } else {
            InputStreamReader(`in`, charEncoding)
        }

        return BufferedReader(reader)
    }

    /**
     * Parse input from the supplied InputSource.  Per {InputSource}, any character stream provided will be used
     * first, then any byte stream, and lastly the system Id.
     */
    @Throws(IOException::class, SAXParseException::class)
    override fun parse(source: InputSource) {
        this.systemId = source.systemId

        if (source.characterStream != null) {
            parse(source.characterStream)
        } else if (source.byteStream != null) {
            parse(source.byteStream)
        } else {
            parse(this.systemId!!)
        }
    }

    /**
     * Parse input from the supplied systemId
     */
    @Throws(IOException::class, SAXParseException::class)
    override fun parse(systemId: String) {
        this.systemId = systemId
        val `is` = URL(systemId).openStream()
        parse(`is`)
    }

    /**
     * Parse input from the supplied InputStream
     */
    @Throws(IOException::class, SAXParseException::class)
    fun parse(`is`: InputStream) {
        val reader = getBufferedReader(`is`)
        parse(reader)
    }

    /**
     * Parse input from the supplied Reader.  WARNING: It is assumed that the given Reader has been initialized
     * with the appropriate character encoding for its underlying input stream.
     */
    @Throws(IOException::class, SAXParseException::class)
    fun parse(reader: Reader?) {
        val br = BufferedReader(reader)
        parse(br)
    }

    @Throws(IOException::class, SAXParseException::class)
    private fun parse(reader: BufferedReader?) {
        var line: String
        var thisLevel: Int
        var prevLevel = -1
        var iden: String
        var tag: String
        var xref: String
        var valu: String
        lineNr = 0
        val stack = Stack<String>()
        stack.push("GED")
        val buf = StringBuilder()

        try {
            contentHandler!!.setDocumentLocator(this)
            contentHandler!!.startDocument()
            contentHandler!!.startElement("", "GED", "GED", emptyAttList)
            var goodLine = false // Indicates whether we have found a good line so far in the file.
            val lineParser = GedcomLineParser()
            while ((reader!!.readLine().also { line = it }) != null) {
                lineNr++

                // remove control chars
                buf.setLength(0)
                for (j in 0 until line.length) {
                    val c = line[j]
                    if (c.code >= 32 || c.code == 9) {
                        buf.append(c)
                    }
                }
                line = buf.toString()

                if (line?.isNotEmpty() == true) {
                    // parse the GEDCOM line into five fields: level, iden, tag, xref, value
                    if (!lineParser.parse(line)) {
                        if (goodLine) {
                            errorHandler!!.error(
                                SAXParseException(
                                    "Line does not appear to be standard @ " +
                                            this.lineNumber + " appending content to the last tag started." + line, this
                                )
                            )
                            contentHandler!!.characters(line.toCharArray(), 0, line.length)
                        } // if we haven't found a good line yet, just skip it

                        if (lineNr > 20 && !goodLine) {
                            break
                        }
                    } else {
                        thisLevel = lineParser.level.toInt()
                        tag = lineParser.tag

                        // if level is > prevlevel+1, ignore it until it comes back down
                        if (thisLevel > prevLevel + 1) {
                            errorHandler!!.error(SAXParseException("Level > prevLevel+1 @ " + this.lineNumber, this))
                        } else if (thisLevel < 0) {
                            errorHandler!!.error(SAXParseException("Level < 0 @ " + this.lineNumber, this))
                        } else if (tag == null || tag.length == 0) {
                            errorHandler!!.error(SAXParseException("Tag not found @ " + this.lineNumber, this))
                        } else {
                            iden = lineParser.iD
                            xref = lineParser.xRef
                            valu = lineParser.value

                            // insert any necessary closing tags
                            while (thisLevel <= prevLevel) {
                                val endtag = stack.pop()
                                contentHandler!!.endElement("", endtag, endtag)
                                prevLevel--
                            }
                            attList.clear()
                            if (iden != null && iden?.isNotEmpty() == true) attList.addAttribute("", "ID", "ID", "ID", iden)
                            if (xref != null && xref?.isNotEmpty() == true) attList.addAttribute("", "REF", "REF", "IDREF", xref)
                            contentHandler!!.startElement("", tag, tag, attList)
                            goodLine = true
                            stack.push(tag)
                            prevLevel = thisLevel
                            if (valu != null && valu?.isNotEmpty() == true) {
                                contentHandler!!.characters(valu.toCharArray(), 0, valu.length)
                            }
                        }
                    }
                }
            }

            if (!goodLine) {
                throw SAXParseException("no good lines found in the first 20 lines ", this)
            }
            contentHandler!!.endElement("", "GED", "GED")
            contentHandler!!.endDocument()
        } catch (e: SAXException) {
            val err = SAXParseException("SAXException: " + e.message, this)
            try {
                errorHandler!!.fatalError(err)
            } catch (e1: SAXException) {
                // ignore
            }
            throw err
        } catch (e: EmptyStackException) {
            val err = SAXParseException("EmptyStack: " + e.message, this)
            try {
                errorHandler!!.fatalError(err)
            } catch (e1: SAXException) {
                // ignore
            }
            throw err
        } finally {
            reader?.close()
        }
    }

    /**
     * Set a feature
     */
    @Throws(SAXNotRecognizedException::class)
    override fun setFeature(s: String, b: Boolean) {
        if (!b || !ACCEPTED_TRUE_SAX_FEATURES.contains(s)) throw SAXNotRecognizedException(
            "Gedcom Parser does not recognize the feature '$s'"
        )
    }

    /**
     * Get a feature
     */
    @Throws(SAXNotRecognizedException::class)
    override fun getFeature(s: String): Boolean {
        if (s == "http://xml.org/sax/features/namespaces") return true
        if (s == "http://xml.org/sax/features/namespace-prefixes") return false
        throw SAXNotRecognizedException("Gedcom Parser does not recognize any features")
    }

    /**
     * Set a property
     */
    @Throws(SAXNotRecognizedException::class)
    override fun setProperty(s: String, b: Any) {
        throw SAXNotRecognizedException("Gedcom Parser does not recognize any properties")
    }

    /**
     * Get a property
     */
    @Throws(SAXNotRecognizedException::class)
    override fun getProperty(s: String): Any {
        throw SAXNotRecognizedException("Gedcom Parser does not recognize any properties")
    }

    /**
     * Get the publicId: always null
     */
    override fun getPublicId(): String? {
        return null
    }

    /**
     * Get the system ID
     */
    override fun getSystemId(): String {
        return systemId!!
    }

    /**
     * Get the line number
     */
    override fun getLineNumber(): Int {
        return lineNr
    }

    /**
     * Get the column number: always -1
     */
    override fun getColumnNumber(): Int {
        return -1
    }

    companion object {
        private val ACCEPTED_TRUE_SAX_FEATURES: List<String> = mutableListOf(
            "http://xml.org/sax/features/namespace-prefixes",  // OK to support, since non are produced from gedcom
            "http://xml.org/sax/features/external-general-entities",  // OK to support, since non are produced from gedcom
            "http://xml.org/sax/features/external-parameter-entities",  // OK to support, since non are produced from gedcom
            "http://xml.org/sax/features/string-interning"
        )

        @Throws(IOException::class)
        private fun readCorrectedCharsetName(`is`: InputStream): String {
            val `in` = BufferedReader(InputStreamReader(`is`))
            return readCorrectedCharsetName(`in`)
        }

        @Throws(IOException::class)
        private fun readCorrectedCharsetName(`in`: BufferedReader): String {
            // We will only try to read the first 100 lines of
            // the file attempting to get the char encoding.
            var line: String?
            var generatorName: String? = null
            var encoding: String? = null
            var version: String? = null
            for (i in 0..99) {
                line = `in`.readLine()
                if (line != null) {
                    var split = line.trim { it <= ' ' }.split("\\s+".toRegex(), limit = 3).toTypedArray()
                    if (split.size == 3) {
                        if (generatorName == null && split[0] == "1" && split[1] == "SOUR") {
                            generatorName = split[2]
                        } else if (split[0] == "1" &&
                            (split[1] == "CHAR" || split[1] == "CHARACTER")
                        ) {
                            // get encoding
                            encoding = split[2].uppercase(Locale.getDefault())
                            // look for version
                            line = `in`.readLine()
                            if (line != null) {
                                split = line.trim { it <= ' ' }.split("\\s+".toRegex(), limit = 3).toTypedArray()
                                if (split.size == 3 && split[0] == "2" && split[1] == "VERS") {
                                    version = split[2]
                                }
                            }
                        }
                    }
                }
                if (generatorName != null && encoding != null) {
                    break // got what we need
                }
            }

            return getCorrectedCharsetName(generatorName, encoding, version)
        }

        fun getCorrectedCharsetName(generatorName: String?, encoding: String?, version: String?): String {
            // correct incorrectly-assigned encoding values
            var encoding = encoding
            if ("GeneWeb" == generatorName && "ASCII" == encoding) {
                // GeneWeb ASCII -> Cp1252 (ANSI)
                encoding = "Cp1252"
            } else if ("Geni.com" == generatorName && "UNICODE" == encoding) {
                // Geni.com UNICODE -> UTF-8
                encoding = "UTF-8"
            } else if ("Geni.com" == generatorName && "ANSEL" == encoding) {
                // Geni.com ANSEL -> UTF-8
                encoding = "UTF-8"
            } else if ("GENJ" == generatorName && "UNICODE" == encoding) {
                // GENJ UNICODE -> UTF-8
                encoding = "UTF-8"
            } else if ("ASCII" == encoding) { // ASCII followed by VERS MacOS Roman is MACINTOSH
                if ("MacOS Roman" == version) {
                    encoding = "x-MacRoman"
                }
            } else if ("ATARIST_ASCII" == encoding) {
                encoding = "ASCII"
            } else if ("MACROMAN" == encoding || "MACINTOSH" == encoding) {
                encoding = "x-MacRoman"
            } else if ("ANSI" == encoding || "IBM WINDOWS" == encoding) {
                encoding = "Cp1252"
            } else if ("WINDOWS-874" == encoding) {
                encoding = "Cp874"
            } else if ("WINDOWS-1251" == encoding) {
                encoding = "Cp1251"
            } else if ("WINDOWS-1254" == encoding) {
                encoding = "Cp1254"
            } else if ("IBMPC" == encoding || "IBM DOS" == encoding) {
                encoding = "Cp850"
            } else if ("UNICODE" == encoding) {
                encoding = "UTF-16"
            } else if ("UTF-16BE" == encoding) {
                encoding = "UnicodeBigUnmarked"
            } else if (encoding == null) {
                encoding = "" // not found
            }

            return encoding
        }
    }
}
