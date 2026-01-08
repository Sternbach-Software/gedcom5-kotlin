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

import java.io.PrintWriter
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

/**
 * General class to collect counts
 *
 * User: dallan
 * Date: 1/2/12
 */
class CountsCollector {
    var counts: HashMap<String, Counter?> = HashMap()

    inner class Counter internal constructor() {
        var count: Int = 0
        override fun toString(): String {
            return count.toString()
        }
    }

    fun size(): Int {
        return counts.size
    }

    private inner class ValueComparator : Comparator<Map.Entry<String, Counter>> {
        override fun compare(me1: Map.Entry<String, Counter>, me2: Map.Entry<String, Counter>): Int {
            return if (me1.value.count < me2.value.count ||
                (me1.value.count == me2.value.count &&
                        me1.key < me2.key)
            ) {
                1
            } else {
                -1
            }
        }

        fun equals(me1: Map.Entry<String, Counter>, me2: Map.Entry<String, Counter>): Boolean {
            return (me1.value.count == me2.value.count && me1.key == me2.key)
        }
    }

    private inner class KeyComparator : Comparator<Map.Entry<String, Counter?>> {
        override fun compare(me1: Map.Entry<String, Counter?>, me2: Map.Entry<String, Counter?>): Int {
            return (me1.key.compareTo(me2.key))
        }

        fun equals(me1: Map.Entry<String, Counter?>, me2: Map.Entry<String, Counter?>): Boolean {
            return (me1.key == me2.key)
        }
    }

    @JvmOverloads
    fun add(key: String?, count: Int = 1) {
        if (key != null) {
            var c = counts[key]
            if (c == null) {
                c = Counter()
                counts[key] = c
            }
            c!!.count += count
        }
    }

    fun remove(key: String) {
        counts.remove(key)
    }

    fun getCount(key: String): Int {
        val c = counts[key]
        return c?.count ?: 0
    }

    val keys: Set<String>
        get() = counts.keys

    fun addAll(keys: Set<String?>) {
        val iter = keys.iterator()
        while (iter.hasNext()) {
            add(iter.next())
        }
    }

    /**
     * Returns the collection sorted and filtered
     * @param byKey if true, sort by key; otherwise sort descending by count
     * @param minCount only include items greater than minCount
     * @return the collection sorted and filtered
     */
    fun getSortedSet(byKey: Boolean, minCount: Int): Set<Pair<String, Counter?>> {
        val comp =
            if (byKey) KeyComparator()
            else ValueComparator()
        val ss = counts.toList().let{ if(byKey) it.sortedBy { it.first } else it.sortedBy { it.second?.count } }.toMutableSet()
        val iter: Iterator<Map.Entry<String, Counter?>> = counts.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (entry.value!!.count >= minCount) {
                ss.add(entry.toPair())
            }
        }
        return ss
    }

    /**
     * Write the stats in sorted order
     * @param byKey if true, sort by key value; otherwise sort by count
     * @param minCount write out only those entries having count greater than minCount
     * @param writer PrintWriter to write to
     */
    fun writeSorted(byKey: Boolean, minCount: Int, writer: PrintWriter) {
        // add all entries in the hash map appearing at least minCount times into the sorted set
        val ss = getSortedSet(byKey, minCount)
        val iter = ss.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            writer.println(entry.first + "\t" + entry.second)
        }
        writer.flush()
    }
}
