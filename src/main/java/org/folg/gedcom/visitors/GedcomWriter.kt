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
package org.folg.gedcom.visitors

import org.folg.gedcom.model.*
import org.folg.gedcom.model.SourceCitation.DataTagContents
import org.folg.gedcom.parser.ModelParser
import org.gedml.AnselOutputStreamWriter
import org.gedml.GedcomParser
import java.io.*
import java.util.*

/**
 * User: Dallan
 * Date: 12/25/11
 *
 * Export a model as GEDCOM
 */
class GedcomWriter : Visitor() {
    private var out: Writer? = null
    private var eol = ""
    private var stack: Stack<Any?>? = null
    private var nestedException: IOException? = null

    @Throws(IOException::class)
    fun write(gedcom: Gedcom?, file: File?) {
        val out: OutputStream = FileOutputStream(file)
        write(gedcom, out)
        out.close()
    }

    @Throws(IOException::class)
    fun write(gedcom: Gedcom?, out: OutputStream?) {
        stack = Stack()
        nestedException = null
        val charset = getCharsetName(gedcom)
        eol = (if (charset == "x-MacRoman") "\r" else "\n")
        val writer = if ("ANSEL" == charset) {
            AnselOutputStreamWriter(out)
        } else {
            OutputStreamWriter(out, charset)
        }
        this.out = BufferedWriter(writer)
        gedcom!!.accept(this)
        this.out!!.write("0 TRLR$eol")
        this.out!!.flush()
        if (nestedException != null) {
            throw nestedException!!
        }
    }

    private fun getCharsetName(gedcom: Gedcom?): String? {
        val header = gedcom?.header
        val generator = header?.generator?.value
        var charset = header?.characterSet?.value
        val version = header?.characterSet?.version
        charset = GedcomParser.Companion.getCorrectedCharsetName(generator, charset, version)
        if (charset.length == 0) {
            charset = "UTF-8" // default
        }
        return charset
    }

    private fun write(
        tag: String?,
        id: String? = null,
        ref: String? = null,
        value: String? = null,
        forceValueOnSeparateLine: Boolean = false
    ) {
        try {
            val level = stack!!.size
            out!!.write("$level ")
            if (id != null && id?.isNotEmpty() == true) {
                out!!.write("@$id@ ")
            }
            out!!.write(tag)
            if (ref != null && ref?.isNotEmpty() == true) {
                out!!.write(" @$ref@")
            }
            if (value != null && value?.isNotEmpty() == true) {
                if (forceValueOnSeparateLine && !value.startsWith("\n")) {
                    out!!.write(eol + (level + 1) + " CONC ")
                } else {
                    out!!.write(" ")
                }
                val buf = StringBuilder(value)
                var cnt = 0
                var lastLine = false
                while (!lastLine) {
                    val nlPos = buf.indexOf("\n")
                    var line: String
                    if (nlPos >= 0) {
                        line = buf.substring(0, nlPos)
                        buf.delete(0, nlPos + 1)
                    } else {
                        line = buf.toString()
                        lastLine = true
                    }
                    if (cnt > 0) {
                        out!!.write(eol + (level + 1) + " CONT ")
                    }
                    while (line.length > 200) {
                        out!!.write(line.substring(0, 200))
                        line = line.substring(200)
                        out!!.write(eol + (level + 1) + " CONC ")
                    }
                    out!!.write(line)
                    cnt++
                }
            }
            out!!.write(eol)
        } catch (e: IOException) {
            nestedException = e
        }
    }

    private fun write(tag: String?, value: String?) {
        write(tag, null, null, value)
    }

    private fun writeFieldExtensions(fieldName: String?, ec: ExtensionContainer) {
        val moreTags = ec.getExtension(ModelParser.Companion.MORE_TAGS_EXTENSION_KEY) as List<GedcomTag>
        if (moreTags != null) {
            for (tag in moreTags) {
                if (fieldName == tag.parentTagName) {
                    stack!!.push(Any()) // placeholder
                    writeGedcomTag(tag)
                    stack!!.pop()
                }
            }
        }
    }

    private fun writeString(tag: String?, ec: ExtensionContainer, value: String?) {
        if (value != null && value?.isNotEmpty() == true) {
            write(tag, value)
            writeFieldExtensions(tag, ec)
        }
    }

    private fun writeRef(tag: String, ec: ExtensionContainer, ref: String?) {
        if (ref != null && ref?.isNotEmpty() == true) {
            write(tag, null, ref, null)
            writeFieldExtensions(tag, ec)
        }
    }

    override fun visit(address: Address): Boolean {
        write("ADDR", address.value)
        stack!!.push(address)
        writeString("ADR1", address, address.addressLine1)
        writeString("ADR2", address, address.addressLine2)
        writeString("CITY", address, address.city)
        writeString("STAE", address, address.state)
        writeString("POST", address, address.postalCode)
        writeString("CTRY", address, address.country)
        writeString("_NAME", address, address.name)
        return true
    }

    override fun visit(association: Association): Boolean {
        write("ASSO", null, association.ref, null)
        stack!!.push(association)
        writeString("TYPE", association, association.type)
        writeString("RELA", association, association.relation)
        return true
    }

    override fun visit(change: Change?): Boolean {
        write("CHAN")
        stack!!.push(change)
        return true
    }

    override fun visit(characterSet: CharacterSet): Boolean {
        write("CHAR", characterSet.value)
        stack!!.push(characterSet)
        writeString("VERS", characterSet, characterSet.version)
        return true
    }

    override fun visit(childRef: ChildRef): Boolean {
        write("CHIL", null, childRef.ref, null)
        stack!!.push(childRef)
        writeSpouseRefStrings(childRef)
        return true
    }

    override fun visit(dateTime: DateTime): Boolean {
        write("DATE", dateTime.value)
        stack!!.push(dateTime)
        writeString("TIME", dateTime, dateTime.time)
        return true
    }

    private fun writeEventFactStrings(eventFact: EventFact) {
        writeString("TYPE", eventFact, eventFact.type)
        writeString("DATE", eventFact, eventFact.date)
        writeString("PLAC", eventFact, eventFact.place)
        writeString("CAUS", eventFact, eventFact.cause)
        writeString("RIN", eventFact, eventFact.rin)
        writeString(eventFact.uidTag, eventFact, eventFact.uid)
    }

    override fun visit(eventFact: EventFact): Boolean {
        write(eventFact.tag, eventFact.value)
        stack!!.push(eventFact)
        writeEventFactStrings(eventFact)
        return true
    }

    private fun writeGedcomTag(tag: GedcomTag?) {
        write(tag?.tag, tag?.id, tag?.ref, tag?.value)
        stack!!.push(tag)
        for (child in tag!!._children ?: emptyList()) {
            writeGedcomTag(child)
        }
        stack!!.pop()
    }

    override fun visit(extensionKey: String?, extension: Any?): Boolean {
        if (ModelParser.Companion.MORE_TAGS_EXTENSION_KEY == extensionKey) {
            val moreTags = extension as List<GedcomTag>?
            for (tag in moreTags!!) {
                if (tag.parentTagName == null) { // if field name is not null, the extension should have been written already
                    writeGedcomTag(tag)
                }
            }
        }
        return true
    }

    private fun writePersonFamilyCommonContainerStrings(pf: PersonFamilyCommonContainer) {
        for (refn in pf.referenceNumbers) {
            // it's a problem if there's multiple refns with sub-tags
            writeString("REFN", pf, refn)
        }
        writeString("RIN", pf, pf.rin)
        writeString(pf.uidTag, pf, pf.uid)
    }

    override fun visit(family: Family): Boolean {
        write("FAM", family.id, null, null)
        stack!!.push(family)
        writePersonFamilyCommonContainerStrings(family)
        return true
    }

    override fun visit(gedcom: Gedcom?): Boolean {
        return true
    }

    override fun visit(gedcomVersion: GedcomVersion): Boolean {
        write("GEDC")
        stack!!.push(gedcomVersion)
        writeString("VERS", gedcomVersion, gedcomVersion.version)
        writeString("FORM", gedcomVersion, gedcomVersion.form)
        return true
    }

    override fun visit(generator: Generator): Boolean {
        write("SOUR", generator.value)
        stack!!.push(generator)
        writeString("NAME", generator, generator.name)
        writeString("VERS", generator, generator.version)
        return true
    }

    override fun visit(generatorCorporation: GeneratorCorporation): Boolean {
        write("CORP", generatorCorporation.value)
        stack!!.push(generatorCorporation)
        writeString("PHON", generatorCorporation, generatorCorporation.phone)
        writeString(generatorCorporation.wwwTag, generatorCorporation, generatorCorporation.www)
        return true
    }

    override fun visit(generatorData: GeneratorData): Boolean {
        write("DATA", generatorData.value)
        stack!!.push(generatorData)
        writeString("DATE", generatorData, generatorData.date)
        writeString("COPR", generatorData, generatorData.copyright)
        return true
    }

    override fun visit(header: Header): Boolean {
        write("HEAD")
        stack!!.push(header)
        writeString("DEST", header, header.destination)
        writeString("FILE", header, header.file)
        writeString("COPR", header, header.copyright)
        writeString("LANG", header, header.language)
        writeRef("SUBM", header, header.submitterRef)
        writeRef("SUBN", header, header.submissionRef)
        return true
    }

    override fun visit(ldsOrdinance: LdsOrdinance): Boolean {
        write(ldsOrdinance.tag, ldsOrdinance.value)
        stack!!.push(ldsOrdinance)
        writeEventFactStrings(ldsOrdinance)
        writeString("STAT", ldsOrdinance, ldsOrdinance.status)
        writeString("TEMP", ldsOrdinance, ldsOrdinance.temple)
        return true
    }

    override fun visit(media: Media): Boolean {
        write("OBJE", media.id, null, null)
        stack!!.push(media)
        writeString("FORM", media, media.format)
        writeString("TITL", media, media.title)
        writeString("BLOB", media, media.blob)
        writeString(media.fileTag, media, media.file)
        writeString("_PRIM", media, media.primary)
        writeString("_TYPE", media, media.type)
        writeString("_SCBK", media, media.scrapbook)
        writeString("_SSHOW", media, media.slideShow)
        return true
    }

    override fun visit(mediaRef: MediaRef): Boolean {
        write("OBJE", null, mediaRef.ref, null)
        stack!!.push(mediaRef)
        return true
    }

    override fun visit(name: Name): Boolean {
        // handle ALIA name by recording it with that tag
        val tag: String
        var type = name.type
        if ("ALIA" == type) {
            tag = type
            type = null
        } else {
            tag = "NAME"
        }
        write(tag, name.value)
        stack!!.push(name)
        writeString("GIVN", name, name.given)
        writeString("SURN", name, name.surname)
        writeString("NPFX", name, name.prefix)
        writeString("NSFX", name, name.suffix)
        writeString("SPFX", name, name.surnamePrefix)
        writeString("NICK", name, name.nickname)
        writeString(name.typeTag, name, type)
        writeString(name.akaTag, name, name.aka)
        writeString(name.marriedNameTag, name, name.marriedName)
        return true
    }

    override fun visit(note: Note): Boolean {
        var visitChildren = false
        if (note.isSourceCitationsUnderValue && note._sourceCitations?.isNotEmpty() == true && note.value != null && note.value?.isNotEmpty() == true) {
            // yuck: handle Reunion broken citations: 0 NOTE 1 CONT ... 2 SOUR; also Easytree: 0 NOTE 1 CONC ... 2 SOUR
            write("NOTE", note.id, null, note.value, true)
            stack!!.push(note)
            stack!!.push(Any()) // increment level to 2
            for (sc in note._sourceCitations!!) {
                sc!!.accept(this)
            }
            stack!!.pop()
            visitChildren = true
        } else {
            write("NOTE", note.id, null, note.value)
            stack!!.push(note)
        }

        // write note strings
        writeString("RIN", note, note.rin)

        if (visitChildren) {
            // if we return false below we need to visit the rest of the children and pop the stack here
            note.visitContainedObjects(this, false)
            stack!!.pop()
        }
        return !visitChildren
    }

    override fun visit(noteRef: NoteRef): Boolean {
        write("NOTE", null, noteRef.ref, null)
        stack!!.push(noteRef)
        return true
    }

    override fun visit(parentFamilyRef: ParentFamilyRef): Boolean {
        write("FAMC", null, parentFamilyRef.ref, null)
        stack!!.push(parentFamilyRef)
        writeSpouseFamilyRefStrings(parentFamilyRef)
        writeString("PEDI", parentFamilyRef, parentFamilyRef.relationshipType)
        writeString("_PRIMARY", parentFamilyRef, parentFamilyRef.primary)
        return true
    }

    override fun visit(parentRelationship: ParentRelationship, isFather: Boolean): Boolean {
        write(if (isFather) "_FREL" else "_MREL", parentRelationship.value)
        stack!!.push(parentRelationship)
        return true
    }

    override fun visit(person: Person): Boolean {
        write("INDI", person.id, null, null)
        stack!!.push(person)
        writeRef("ANCI", person, person.ancestorInterestSubmitterRef)
        writeRef("DESI", person, person.descendantInterestSubmitterRef)
        writeString("RFN", person, person.recordFileNumber)
        writeString("PHON", person, person.phone)
        writeString(person.emailTag, person, person.email)
        writePersonFamilyCommonContainerStrings(person)
        return true
    }

    override fun visit(repository: Repository): Boolean {
        write("REPO", repository.id, null, repository.value)
        stack!!.push(repository)
        writeString("NAME", repository, repository.name)
        writeString("PHON", repository, repository.phone)
        writeString("RIN", repository, repository.rin)
        writeString(repository.emailTag, repository, repository.email)
        writeString(repository.wwwTag, repository, repository.www)
        return true
    }

    override fun visit(repositoryRef: RepositoryRef): Boolean {
        write("REPO", null, repositoryRef.ref, repositoryRef.value)
        stack!!.push(repositoryRef)
        if (repositoryRef.isMediUnderCalnTag == true ||
            (repositoryRef.callNumber != null && repositoryRef.callNumber?.isNotEmpty() == true)
        ) {
            write("CALN", repositoryRef.callNumber)
        }
        if (repositoryRef.isMediUnderCalnTag == true) {
            stack!!.push(Any()) // placeholder
        }
        writeString("MEDI", repositoryRef, repositoryRef.mediaType)
        if (repositoryRef.isMediUnderCalnTag == true) {
            stack!!.pop()
        }

        return true
    }

    override fun visit(source: Source): Boolean {
        write("SOUR", source.id, null, null)
        stack!!.push(source)
        writeString("AUTH", source, source.author)
        writeString("TITL", source, source.title)
        writeString("ABBR", source, source.abbreviation)
        writeString("PUBL", source, source.publicationFacts)
        writeString("TEXT", source, source.text)
        writeString("REFN", source, source.referenceNumber)
        writeString("RIN", source, source.rin)
        writeString("MEDI", source, source.mediaType)
        writeString("CALN", source, source.callNumber)
        writeString(source.typeTag, source, source.type)
        writeString(source.uidTag, source, source.uid)
        writeString("_PAREN", source, source.paren)
        writeString("_ITALIC", source, source.italic)
        writeString("DATE", source, source.date)
        return true
    }

    private fun writeUnderData(tag: String, sourceCitation: SourceCitation, value: String?) {
        if (value != null && value?.isNotEmpty() == true) {
            write("DATA")
            stack!!.push(Any()) // placeholder
            writeString(tag, sourceCitation, value)
            stack!!.pop()
        }
    }

    override fun visit(sourceCitation: SourceCitation): Boolean {
        write("SOUR", null, sourceCitation.ref, sourceCitation.value)
        stack!!.push(sourceCitation)
        writeString("PAGE", sourceCitation, sourceCitation.page)
        writeString("QUAY", sourceCitation, sourceCitation.quality)
        if (sourceCitation.dataTagContents == DataTagContents.COMBINED &&
            (sourceCitation.date != null && sourceCitation.date?.isNotEmpty() == true ||
                    sourceCitation.text != null && sourceCitation.text?.isNotEmpty() == true)
        ) {
            write("DATA")
            stack!!.push(Any()) // placeholder
            writeString("DATE", sourceCitation, sourceCitation.date)
            writeString("TEXT", sourceCitation, sourceCitation.text)
            stack!!.pop()
        } else if (sourceCitation.dataTagContents == DataTagContents.DATE) {
            writeUnderData("DATE", sourceCitation, sourceCitation.date)
            writeString("TEXT", sourceCitation, sourceCitation.text)
        } else if (sourceCitation.dataTagContents == DataTagContents.TEXT) {
            writeUnderData("TEXT", sourceCitation, sourceCitation.text)
            writeString("DATE", sourceCitation, sourceCitation.date)
        } else if (sourceCitation.dataTagContents == DataTagContents.SEPARATE) {
            writeUnderData("DATE", sourceCitation, sourceCitation.date)
            writeUnderData("TEXT", sourceCitation, sourceCitation.text)
        } else if (sourceCitation.dataTagContents == null) {
            writeString("DATE", sourceCitation, sourceCitation.date)
            writeString("TEXT", sourceCitation, sourceCitation.text)
        }
        return true
    }

    private fun writeSpouseRefStrings(spouseRef: SpouseRef) {
        writeString("_PREF", spouseRef, spouseRef.preferred)
    }

    override fun visit(spouseRef: SpouseRef, isHusband: Boolean): Boolean {
        write(if (isHusband) "HUSB" else "WIFE", null, spouseRef.ref, null)
        stack!!.push(spouseRef)
        writeSpouseRefStrings(spouseRef)
        return true
    }

    private fun writeSpouseFamilyRefStrings(spouseFamilyRef: SpouseFamilyRef) {
        // nothing to write
    }

    override fun visit(spouseFamilyRef: SpouseFamilyRef): Boolean {
        write("FAMS", null, spouseFamilyRef.ref, null)
        stack!!.push(spouseFamilyRef)
        writeSpouseFamilyRefStrings(spouseFamilyRef)
        return true
    }

    override fun visit(submission: Submission): Boolean {
        write("SUBN", submission.id, null, null)
        stack!!.push(submission)
        writeString("DESC", submission, submission.description)
        writeString("ORDI", submission, submission.ordinanceFlag)
        return true
    }

    override fun visit(submitter: Submitter): Boolean {
        write("SUBM", submitter.id, null, submitter.value)
        stack!!.push(submitter)
        writeString("PHON", submitter, submitter.phone)
        writeString("NAME", submitter, submitter.name)
        writeString("RIN", submitter, submitter.rin)
        writeString("LANG", submitter, submitter.language)
        writeString(submitter.wwwTag, submitter, submitter.www)
        writeString(submitter.emailTag, submitter, submitter.email)
        return true
    }

    override fun endVisit(obj: ExtensionContainer?) {
        if (obj !is Gedcom) {
            stack!!.pop()
        }
    }
}
