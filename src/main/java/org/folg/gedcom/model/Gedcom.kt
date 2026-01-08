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

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * User: Dallan
 * Date: 12/24/11
 */
class Gedcom : ExtensionContainer() {
    var header: Header? = null
    private var subms: MutableList<Submitter>? = null
    private var subn: Submission? = null
    private var people: MutableList<Person>? = null
    private var families: MutableList<Family>? = null
    private var media: MutableList<Media>? = null
    private var notes: MutableList<Note>? = null
    private var sources: MutableList<Source>? = null
    private var repositories: MutableList<Repository>? = null

    @Transient
    private var personIndex: MutableMap<String?, Person>? = null

    @Transient
    private var familyIndex: MutableMap<String?, Family>? = null

    @Transient
    private var mediaIndex: MutableMap<String?, Media>? = null

    @Transient
    private var noteIndex: MutableMap<String?, Note>? = null

    @Transient
    private var sourceIndex: MutableMap<String?, Source>? = null

    @Transient
    private var repositoryIndex: MutableMap<String?, Repository>? = null

    @Transient
    private var submitterIndex: MutableMap<String?, Submitter>? = null

    fun getPeople(): List<Person> {
        return people ?: emptyList()
    }

    fun getPerson(id: String?): Person? {
        return personIndex!![id]
    }

    fun setPeople(people: MutableList<Person>?) {
        this.people = people
    }

    fun addPerson(person: Person) {
        if (people == null) {
            people = ArrayList()
        }
        people!!.add(person)
        if (personIndex != null) {
            personIndex!![person.id] = person
        }
    }

    fun getFamilies(): List<Family> {
        return families ?: emptyList()
    }

    fun getFamily(id: String?): Family? {
        return familyIndex!![id]
    }

    fun setFamilies(families: MutableList<Family>?) {
        this.families = families
    }

    fun addFamily(family: Family) {
        if (families == null) {
            families = ArrayList()
        }
        families!!.add(family)
        if (familyIndex != null) {
            familyIndex!![family.id] = family
        }
    }

    fun getMedia(): List<Media> {
        return media ?: emptyList()
    }

    fun getMedia(id: String?): Media? {
        return mediaIndex!![id]
    }

    fun setMedia(media: MutableList<Media>?) {
        this.media = media
    }

    fun addMedia(m: Media) {
        if (media == null) {
            media = ArrayList()
        }
        media!!.add(m)
        if (mediaIndex != null) {
            mediaIndex!![m.id] = m
        }
    }

    fun getNotes(): List<Note> {
        return notes ?: emptyList()
    }

    fun getNote(id: String?): Note? {
        return noteIndex!![id]
    }

    fun setNotes(notes: MutableList<Note>?) {
        this.notes = notes
    }

    fun addNote(note: Note) {
        if (notes == null) {
            notes = ArrayList()
        }
        notes!!.add(note)
        if (noteIndex != null) {
            noteIndex!![note.id] = note
        }
    }

    fun getSources(): List<Source> {
        return sources ?: emptyList()
    }

    fun getSource(id: String?): Source? {
        return sourceIndex!![id]
    }

    fun setSources(sources: MutableList<Source>?) {
        this.sources = sources
    }

    fun addSource(source: Source) {
        if (sources == null) {
            sources = ArrayList()
        }
        sources!!.add(source)
        if (sourceIndex != null) {
            sourceIndex!![source.id] = source
        }
    }

    fun getRepositories(): List<Repository> {
        return repositories ?: emptyList()
    }

    fun getRepository(id: String?): Repository? {
        return repositoryIndex!![id]
    }

    fun setRepositories(repositories: MutableList<Repository>?) {
        this.repositories = repositories
    }

    fun addRepository(repository: Repository) {
        if (repositories == null) {
            repositories = ArrayList()
        }
        repositories!!.add(repository)
        if (repositoryIndex != null) {
            repositoryIndex!![repository.id] = repository
        }
    }

    fun getSubmitter(id: String?): Submitter? {
        return submitterIndex!![id]
    }

    val submitters: List<Submitter>
        get() = subms ?: emptyList()

    fun setSubmitters(submitters: MutableList<Submitter>?) {
        this.subms = submitters
    }

    fun addSubmitter(submitter: Submitter) {
        if (subms == null) {
            subms = ArrayList()
        }
        subms!!.add(submitter)

        if (submitterIndex != null) {
            submitterIndex!![submitter.id] = submitter
        }
    }

    var submission: Submission?
        /**
         * Use this function in place of Header.getSubmission
         *
         * @return Submission top-level record or from header
         */
        get() {
            if (subn != null) {
                return subn
            } else if (header != null) {
                return header?.submission
            }
            return null
        }
        set(subn) {
            this.subn = subn
        }

    fun updateReferences() {
        for (person in getPeople()) {
            for (spouseRef in person.spouseFamilyRefs) {
                val family = getFamily(spouseRef?.ref)
                val spouseRefInHusbands =
                    family!!._husbandRefs?.stream()?.anyMatch { ref: SpouseRef? -> ref?.ref == person.id } ?: false
                val spouseRefInWives =
                    family._wifeRefs?.stream()?.anyMatch { ref: SpouseRef? -> ref?.ref == person.id } ?: false
                if (!spouseRefInHusbands && !spouseRefInWives) {
                    val fact = person._eventsFacts?.stream()?.filter { event: EventFact -> event.tag == "SEX" }?.findFirst() ?: Optional.empty()
                    val ref = SpouseRef()
                    ref.ref = person.id

                    if (fact.isPresent && fact.get().value == "F") {
                        family.addWife(ref)
                    } else {
                        family.addHusband(ref)
                    }
                }
            }

            for (parentRef in person.parentFamilyRefs) {
                val family = getFamily(parentRef?.ref)
                val containsReference =
                    family!!._childRefs?.stream()?.anyMatch { ref: ChildRef? -> ref?.ref == person.id }
                if (containsReference == false) {
                    val ref = ChildRef()
                    ref.ref = person.id
                    family.addChild(ref)
                }
            }
        }

        for (family in getFamilies()) {
            for (ref in family._husbandRefs ?: emptyList()) {
                val person = getPerson(ref?.ref)
                val containsRef = person?.spouseFamilyRefs?.stream()
                    ?.anyMatch { spouseFamilyRef: SpouseFamilyRef? -> spouseFamilyRef?.ref == family.id }
                if (containsRef == false) {
                    val spouseFamilyRef = SpouseFamilyRef()
                    spouseFamilyRef.ref = family.id
                    person!!.addSpouseFamilyRef(spouseFamilyRef)
                }
            }
            for (ref in family._wifeRefs ?: emptyList()) {
                val person = getPerson(ref?.ref)
                val containsRef = person?.spouseFamilyRefs?.stream()
                    ?.anyMatch { spouseFamilyRef: SpouseFamilyRef? -> spouseFamilyRef?.ref == family.id }
                if (containsRef == false) {
                    val spouseFamilyRef = SpouseFamilyRef()
                    spouseFamilyRef.ref = family.id
                    person!!.addSpouseFamilyRef(spouseFamilyRef)
                }
            }

            for (ref in family._childRefs ?: emptyList()) {
                val person = getPerson(ref?.ref)
                val containsRef = person?.parentFamilyRefs?.stream()
                    ?.anyMatch { parentFamilyRef: ParentFamilyRef? -> parentFamilyRef?.ref == family.id }
                if (containsRef == false) {
                    val spouseFamilyRef = ParentFamilyRef()
                    spouseFamilyRef.ref = family.id
                    person.addParentFamilyRef(spouseFamilyRef)
                }
            }
        }
    }

    fun createIndexes() {
        personIndex = HashMap()
        for (person in getPeople()) {
            personIndex!![person.id] = person
        }
        familyIndex = HashMap()
        for (family in getFamilies()) {
            familyIndex!![family.id] = family
        }
        mediaIndex = HashMap()
        for (m in getMedia()) {
            mediaIndex!![m.id] = m
        }
        noteIndex = HashMap()
        for (note in getNotes()) {
            noteIndex!![note.id] = note
        }
        sourceIndex = HashMap()
        for (source in getSources()) {
            sourceIndex!![source.id] = source
        }
        repositoryIndex = HashMap()
        for (repository in getRepositories()) {
            repositoryIndex!![repository.id] = repository
        }

        submitterIndex = HashMap()
        for (submitter in submitters) {
            submitterIndex!![submitter.id] = submitter
        }
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            if (header != null) {
                header!!.accept(visitor)
            }
            for (submitter in submitters) {
                submitter.accept(visitor)
            }
            if (subn != null) {
                subn!!.accept(visitor)
            }
            for (person in getPeople()) {
                person.accept(visitor)
            }
            for (family in getFamilies()) {
                family.accept(visitor)
            }
            for (media in getMedia()) {
                media.accept(visitor)
            }
            for (note in getNotes()) {
                note.accept(visitor)
            }
            for (source in getSources()) {
                source.accept(visitor)
            }
            for (repository in getRepositories()) {
                repository.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
