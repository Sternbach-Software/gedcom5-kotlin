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
package org.folg.gedcom.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simple data structure for a gedcom node - json-friendly
 * User: Dallan
 * Date: 12/23/11
 */
class GedcomTag(var id: String?, @JvmField var tag: String?, var ref: String?) : Comparable<GedcomTag> {
    var value: String? = null
    var parentTagName: String? = null // used by ModelParser to store tags under string fields
    var _children: MutableList<GedcomTag?>? = null

    fun appendValue(value: String?) {
        if (this.value == null) {
            this.value = value
        } else {
            this.value += value
        }
    }

    fun getChildren(): List<GedcomTag?> = _children ?: emptyList<GedcomTag>()

    fun addChild(child: GedcomTag?) {
        if (_children == null) {
            _children = ArrayList()
        }
        _children!!.add(child)
    }

    private fun getSortedChildren(children: List<GedcomTag?>): List<GedcomTag?> = children.sortedBy { it }

    val isEmpty: Boolean
        get() = (id == null || id!!.isEmpty()) &&
                (ref == null || ref!!.isEmpty()) &&
                (parentTagName == null || parentTagName!!.isEmpty()) &&
                (value == null || value!!.isEmpty()) &&
                (_children == null || _children!!.size == 0)

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val gt = o as GedcomTag

        if ((if (tag == null) "" else tag) != if (gt.tag == null) "" else gt.tag) {
            return false
        }
        if ((if (id == null) "" else id) != if (gt.id == null) "" else gt.id) {
            return false
        }
        if ((if (ref == null) "" else ref) != if (gt.ref == null) "" else gt.ref) {
            return false
        }
        if ((if (parentTagName == null) "" else parentTagName) != if (gt.parentTagName == null) "" else gt.parentTagName) {
            return false
        }
        if ((if (value == null) "" else value) != if (gt.value == null) "" else gt.value) {
            return false
        }
        if (getChildren().size != gt.getChildren().size) {
            return false
        }

        // this is horribly inefficient - if we're going to do this a lot during production we need to rewrite it
        // but as-is, it's convenient for debugging
        val sortedChildren = getSortedChildren(getChildren())
        val compareChildren = getSortedChildren(gt.getChildren())

        for (indx in sortedChildren.indices) {
            if (sortedChildren[indx] != compareChildren[indx]) {
                logger.info("!tag=" + sortedChildren[indx].toString() + "<=>" + compareChildren[indx].toString())
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = if (id != null && id!!.isNotEmpty()) id.hashCode() else 0
        result = 31 * result + (if (tag != null && tag!!.isNotEmpty()) tag.hashCode() else 0)
        result = 31 * result + (if (ref != null && ref!!.isNotEmpty()) ref.hashCode() else 0)
        result =
            31 * result + (if (parentTagName != null && parentTagName!!.isNotEmpty()) parentTagName.hashCode() else 0)
        result = 31 * result + (if (value != null && value!!.isNotEmpty()) value.hashCode() else 0)

        // Calculate the children hash code based on the actual value of each child hashcode
        // result = 31 * result + (children != null ? children.hashCode() : 0);
        if (_children != null) {
            for (child in getSortedChildren(_children!!)) {
                result = 31 * result + child.hashCode()
            }
        }

        return result
    }

    override fun toString(): String {
        val buf = StringBuilder()
        if (tag != null) {
            buf.append(" tag:$tag")
        }
        if (id != null) {
            buf.append(" id:$id")
        }
        if (ref != null) {
            buf.append(" ref:$ref")
        }
        if (parentTagName != null) {
            buf.append(" parentTag:$parentTagName")
        }
        if (value != null) {
            buf.append(" value:$value")
        }
        if (_children != null) {
            buf.append(" [")
            for (child in getSortedChildren(_children!!)) {
                buf.append(child.toString())
            }
            buf.append(" ]")
        }
        return buf.toString()
    }

    override fun compareTo(tag: GedcomTag): Int {
        var c =
            (if (this.tag == null) "" else this.tag)!!.compareTo((if (tag.tag == null) "" else tag.tag)!!)
        if (c != 0) return c
        c = (if (id == null) "" else id)!!.compareTo((if (tag.id == null) "" else tag.id)!!)
        if (c != 0) return c
        c = (if (ref == null) "" else ref)!!.compareTo((if (tag.ref == null) "" else tag.ref)!!)
        if (c != 0) return c
        c =
            (if (parentTagName == null) "" else parentTagName)!!.compareTo((if (tag.parentTagName == null) "" else tag.parentTagName)!!)
        if (c != 0) return c
        c =
            (if (value == null) "" else value)!!.compareTo((if (tag.value == null) "" else tag.value)!!)
        if (c != 0) return c
        // compare hashcode because that compares children as well
        return this.hashCode().compareTo(tag.hashCode())
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("org.folg.gedcom.model")
    }
}
