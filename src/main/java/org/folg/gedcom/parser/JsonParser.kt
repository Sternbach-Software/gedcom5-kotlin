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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.folg.gedcom.model.Gedcom
import org.folg.gedcom.model.GedcomTag
import java.io.*
import java.nio.charset.Charset

/**
 * User: dallan
 * Date: 1/2/12
 */
class JsonParser {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Gedcom::class.java, GedcomTypeAdapter())
        .create()

    /**
     * Convert json-encoded string to Gedcom object
     *
     * @param json json-encoded string
     * @return Gedcom object
     */
    fun fromJson(json: String?): Gedcom {
        return gson.fromJson(json, Gedcom::class.java)
    }

    /**
     * Read Gedcom object from json character stream
     *
     * @param reader json source reader
     * @return Gedcom object
     */
    fun fromJson(reader: Reader?): Gedcom {
        return gson.fromJson(reader, Gedcom::class.java)
    }

    /**
     * Read Gedcom object from json byte stream
     *
     * @param is json source stream
     * @return Gedcom object
     */
    fun fromJson(`is`: InputStream?): Gedcom {
        val reader = InputStreamReader(`is`, Charset.forName("UTF-8"))
        return gson.fromJson(reader, Gedcom::class.java)
    }

    /**
     * Convert Gedcom object to json
     *
     * @param gedcom Gedcom
     * @return json-encoded string
     */
    fun toJson(gedcom: Gedcom?): String {
        return gson.toJson(gedcom)
    }

    /**
     * Convert output of TreeParser to json
     *
     * @param gedcomTags output of TreeParser
     * @return json-encoded string
     */
    fun toJson(gedcomTags: List<GedcomTag?>?): String {
        return gson.toJson(gedcomTags)
    }

    /**
     * Write Gedcom object to json character stream
     *
     * @param gedcom Gedcom object
     * @param writer json output writer
     */
    fun write(gedcom: Gedcom?, writer: Writer?) {
        gson.toJson(gedcom, writer)
        close(writer)
    }

    /**
     * Write Gedcom object to json byte stream
     *
     * @param gedcom Gedcom object
     * @param os     json output stream
     */
    fun write(gedcom: Gedcom?, os: OutputStream?) {
        val writer = OutputStreamWriter(os, Charset.forName("UTF-8"))
        gson.toJson(gedcom, writer)
        close(writer)
    }

    /**
     * Write Gedcom Tags from TreeParser to json character stream
     *
     * @param gedcomTags Gedcom tags
     * @param writer     json output writer
     */
    fun write(gedcomTags: List<GedcomTag?>?, writer: Writer?) {
        gson.toJson(gedcomTags, writer)
        close(writer)
    }

    /**
     * Write Gedcom Tags from TreeParser to json byte stream
     *
     * @param gedcomTags Gedcom tags
     * @param os         json output stream
     */
    fun write(gedcomTags: List<GedcomTag?>?, os: OutputStream?) {
        val writer = OutputStreamWriter(os, Charset.forName("UTF-8"))
        gson.toJson(gedcomTags, writer)
        close(writer)
    }

    private fun close(writer: Writer?) {
        try {
            writer?.close()
        } catch (e: IOException) {
            throw IllegalStateException("Closing writer failed", e)
        }
    }
}
