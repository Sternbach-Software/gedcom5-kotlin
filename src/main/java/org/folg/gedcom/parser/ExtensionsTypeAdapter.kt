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
import org.folg.gedcom.model.*
import java.lang.reflect.Type

/**
 * User: Dallan
 * Date: 12/18/11
 */
class ExtensionsTypeAdapter : JsonSerializer<Extensions>, JsonDeserializer<Extensions> {
    private val gson = Gson()
    private val classExtensions: MutableMap<String?, Class<*>?> = HashMap()
    private val typeExtensions: MutableMap<String?, Type?> = HashMap()

    fun registerExtension(extensionKey: String?, clazz: Class<*>?) {
        classExtensions[extensionKey] = clazz
    }

    fun registerExtension(extensionKey: String?, type: Type?) {
        typeExtensions[extensionKey] = type
    }

    override fun serialize(src: Extensions, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val o = JsonObject()
        for ((key, extension) in src) {
            val elm = (if (extension is JsonElement) extension else gson.toJsonTree(extension))
            o.add(key, elm)
        }
        return o
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Extensions {
        val o = json.asJsonObject
        val extensions = Extensions()
        for ((key, jsonElement) in o.entrySet()) {
            var extension: Any
            val type = typeExtensions[key]
            if (type != null) {
                extension = gson.fromJson(jsonElement, type)
            } else {
                val clazz = classExtensions[key]
                extension = if (clazz != null) {
                    gson.fromJson<Any>(jsonElement, clazz)
                } else {
                    jsonElement
                }
            }
            extensions.put(key, extension)
        }
        return extensions
    }
}
