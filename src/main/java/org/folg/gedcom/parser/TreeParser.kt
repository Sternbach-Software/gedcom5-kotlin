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
package org.folg.gedcom.parser

import org.folg.gedcom.model.GedcomTag
import org.gedml.GedcomParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.*
import java.io.*
import java.util.*

/**
 * Call parseGedcom to parse a gedcom file into a list of GedcomTag's
 * User: Dallan
 * Date: 12/23/11
 */
class TreeParser : ContentHandler, org.xml.sax.ErrorHandler {
    private var locator: Locator? = null
    private var tree: GedcomTag? = null
    private var nodeStack: Stack<GedcomTag>? = null
    private var errorHandler: ErrorHandler? = null

    override fun setDocumentLocator(locator: Locator) {
        this.locator = locator
    }

    @Throws(SAXException::class)
    override fun startDocument() {
        tree = null
        nodeStack = Stack()
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        // ignore
    }

    @Throws(SAXException::class)
    override fun startPrefixMapping(prefix: String, uri: String) {
        // ignore
    }

    @Throws(SAXException::class)
    override fun endPrefixMapping(prefix: String) {
        // ignore
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        val node = GedcomTag(atts.getValue("ID"), localName, atts.getValue("REF"))
        if (tree == null) {
            tree = node
        } else {
            nodeStack!!.peek().addChild(node)
        }
        nodeStack!!.push(node)
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        nodeStack!!.pop()
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        val s = String(ch, start, length)
        val tos = nodeStack!!.peek()
        tos.appendValue(s)
    }

    @Throws(SAXException::class)
    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {
        // ignore
    }

    @Throws(SAXException::class)
    override fun processingInstruction(target: String, data: String) {
        // ignore
    }

    @Throws(SAXException::class)
    override fun skippedEntity(name: String) {
        // ignore
    }

    @Throws(SAXException::class)
    override fun warning(exception: SAXParseException) {
        if (errorHandler != null) {
            errorHandler!!.warning(exception.message, exception.lineNumber)
        } else {
            logger.info(exception.message + " @ " + exception.lineNumber)
        }
    }

    @Throws(SAXException::class)
    override fun error(exception: SAXParseException) {
        if (errorHandler != null) {
            errorHandler!!.error(exception.message, exception.lineNumber)
        } else {
            logger.warn(exception.message + " @ " + exception.lineNumber)
        }
    }

    @Throws(SAXException::class)
    override fun fatalError(exception: SAXParseException) {
        if (errorHandler != null) {
            errorHandler!!.fatalError(exception.message, exception.lineNumber)
        } else {
            logger.error(exception.message + " @ " + exception.lineNumber)
        }
    }

    fun setErrorHandler(errorHandler: ErrorHandler?) {
        this.errorHandler = errorHandler
    }

    @Throws(SAXParseException::class, IOException::class)
    fun parseGedcom(gedcomFile: File?): MutableList<GedcomTag?>? {
        val parser = gedcomParser()
        parser.parse(gedcomFile!!.toURI().toString())
        return tree!!._children
    }

    @Throws(SAXParseException::class, IOException::class)
    fun parseGedcom(`is`: InputStream): List<GedcomTag?>? {
        val parser = gedcomParser()
        parser.parse(`is`)
        return tree!!._children
    }

    @Throws(SAXParseException::class, IOException::class)
    fun parseGedcom(reader: Reader?): List<GedcomTag?>? {
        val parser = gedcomParser()
        parser.parse(reader)
        return tree!!._children
    }

    private fun gedcomParser(): GedcomParser {
        val parser = GedcomParser()
        parser.contentHandler = this
        parser.errorHandler = this
        return parser
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("org.folg.gedcom.parser")
    }
}
