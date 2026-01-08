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

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.folg.gedcom.model.*
import java.lang.reflect.Type

/**
 * User: Dallan
 * Date: 12/31/11
 */
class GedcomTypeAdapter : JsonSerializer<Gedcom?>, JsonDeserializer<Gedcom> {
    private val gson: Gson
    private val extensionsTypeAdapter = ExtensionsTypeAdapter()

    init {
        // built-in extension
        extensionsTypeAdapter.registerExtension(
            ModelParser.Companion.MORE_TAGS_EXTENSION_KEY,
            object : TypeToken<List<GedcomTag?>?>() {}.type
        )
        gson = GsonBuilder()
            .registerTypeAdapter(Extensions::class.java, extensionsTypeAdapter)
            .create()
    }

    fun registerExtension(extensionKey: String?, clazz: Class<*>?) {
        extensionsTypeAdapter.registerExtension(extensionKey, clazz)
    }

    fun registerExtension(extensionKey: String?, type: Type?) {
        extensionsTypeAdapter.registerExtension(extensionKey, type)
    }

    override fun serialize(src: Gedcom?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return gson.toJsonTree(src)
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Gedcom {
        val gedcom = gson.fromJson(json, Gedcom::class.java)
        gedcom.createIndexes()
        return gedcom
    }
}
