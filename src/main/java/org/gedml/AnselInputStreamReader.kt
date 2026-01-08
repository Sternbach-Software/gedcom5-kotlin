package org.gedml

import java.io.*

/*
* org.gedml.AnselInputStreamReader
* This class reads an input stream of bytes representing
* ANSEL-encoded characters, and delivers a stream of UNICODE characters
* @Author: mhkay@iclway.co.uk
* @GedcomVersion: 20 May 1998
* 20 May 1998: conversion tables updated with input from John Cowan
* Nov 2011: conversion tables updated again based upon
* http://www.heiner-eichmann.de/gedcom/oldansset.htm and http://lcweb2.loc.gov/diglib/codetables/45.html
*/
class AnselInputStreamReader(private val input: InputStream) : InputStreamReader(input) {
    private var pending: Int

    init {
        pending = input.read() // we read one character ahead to cope
        // with non-spacing diacriticals
    }

    /*
    * Return one UNICODE character
    */
    @Throws(IOException::class)
    override fun read(): Int {
        val b = pending
        if (b < 0) return b // return EOF unchanged

        pending = input.read()
        if (b < 128) return b // return ASCII characters unchanged


        // try to match two ansel chars if we can
        if (pending > 0 && ((b >= 0xE0 && b <= 0xFF) || (b >= 0xd7 && b <= 0xd9))) {
            val u = convert2(b * 256 + pending)
            if (u > 0) {
                pending = input.read()
                return u
            }
        }
        // else match one char
        return (convert1(b))
    }

    /*
    * Fill a supplied buffer with UNICODE characters
    */
    @Throws(IOException::class)
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (pending < 0) return -1 // have already hit EOF

        for (i in off until off + len) {
            val c = read()
            if (c < 0) {
                return i - off
            }
            cbuf[off + i] = c.toChar()
        }
        return len
    }

    /*
    * Determine the character code in use
    */
    override fun getEncoding(): String {
        return "ANSEL"
    }

    /*
    * Conversion table for ANSEL characters coded in one byte
    */
    private fun convert1(ansel: Int): Int {
        return when (ansel) {
            0x8D -> 0x200D //  zero width joiner
            0x8E -> 0x200C //  zero width non-joiner
            0xA1 -> 0x0141 //  capital L with stroke
            0xA2 -> 0x00D8 //  capital O with oblique stroke
            0xA3 -> 0x00D0 //  capital icelandic letter Eth
            0xA4 -> 0x00DE //  capital Icelandic letter Thorn
            0xA5 -> 0x00C6 //  capital diphthong A with E
            0xA6 -> 0x0152 //  capital ligature OE
            0xA7 -> 0x02B9 //  modified letter prime
            0xA8 -> 0x00B7 //  middle dot
            0xA9 -> 0x266D //  music flat sign
            0xAA -> 0x00AE //  registered trade mark sign
            0xAB -> 0x00B1 //  plus-minus sign
            0xAC -> 0x01A0 //  capital O with horn
            0xAD -> 0x01AF //  capital U with horn
            0xAE -> 0x02BE //  modifier letter right half ring
            0xB0 -> 0x00B0 //  modifier letter left half ring
            0xB1 -> 0x0142 //  small l with stroke
            0xB2 -> 0x00F8 //  small o with oblique stroke
            0xB3 -> 0x0111 //  small D with stroke
            0xB4 -> 0x00FE //  small Icelandic letter Thorn
            0xB5 -> 0x00E6 //  small diphthong a with e
            0xB6 -> 0x0153 //  small ligature OE
            0xB7 -> 0x02BA //  modified letter double prime
            0xB8 -> 0x0131 //  small dotless i
            0xB9 -> 0x00A3 //  pound sign
            0xBA -> 0x00F0 //  small Icelandic letter Eth
            0xBC -> 0x01A1 //  small o with horn
            0xBD -> 0x01B0 //  small u with horn
            0xC0 -> 0x00B0 //  degree sign, ring above
            0xC1 -> 0x2113 //  script small l
            0xC2 -> 0x2117 //  sound recording copyright
            0xC3 -> 0x00A9 //  copyright sign
            0xC4 -> 0x266F //  music sharp sign
            0xC5 -> 0x00BF //  inverted question mark
            0xC6 -> 0x00A1 //  inverted exclamation mark
            0xCF -> 0x00DF //  small German letter sharp s
            0xE0 -> 0x0309 //  hook above
            0xE1 -> 0x0300 //  grave accent
            0xE2 -> 0x0301 //  acute accent
            0xE3 -> 0x0302 //  circumflex accent
            0xE4 -> 0x0303 //  tilde
            0xE5 -> 0x0304 //  combining macron
            0xE6 -> 0x0306 //  breve
            0xE7 -> 0x0307 //  dot above
            0xE9 -> 0x030C //  caron
            0xEA -> 0x030A //  ring above
            0xEB -> 0xFE20 //  ligature left half
            0xEC -> 0xFE21 //  ligature right half
            0xED -> 0x0315 //  comma above right
            0xEE -> 0x030B //  double acute accent
            0xEF -> 0x0310 //  candrabindu
            0xF0 -> 0x0327 //  combining cedilla
            0xF1 -> 0x0328 //  ogonek
            0xF2 -> 0x0323 //  dot below
            0xF3 -> 0x0324 //  diaeresis below
            0xF4 -> 0x0325 //  ring below
            0xF5 -> 0x0333 //  double low line
            0xF6 -> 0x0332 //  low line (= line below?)
            0xF7 -> 0x0326 //  comma below
            0xF8 -> 0x031C //  combining half ring below
            0xF9 -> 0x032E //  breve below
            0xFA -> 0xFE22 //  double tilde left half
            0xFB -> 0xFE23 //  double tilde right half
            0xFE -> 0x0313 //  comma above
            0x81 -> 0x00FC // ü - umlaught
            0x88 -> 0x0098 // non-sort begin
            0x89 -> 0x009C // non-sort end
            0x92 -> 0x0027 // apostophe
            0x93 -> 0x201C // left double quotation mark (not in the standard)
            0x94 -> 0x201D // right double quotation mark (not in the standard)
            0xC7 -> 0x00DF // eszett symbol
            0xC8 -> 0x20AC // euro sign
            0xE8 -> 0x0308 // umlaut, diaresis
            0xBE -> 0x25AF // white vertical rectangle
            0xBF -> 0x25AE // black vertical rectangle
            0xCD -> 0x0065 // e in middle of line
            0xCE -> 0x006F // o in middle of line

            else -> 0xFFFD // if no match, use Unicode REPLACEMENT CHARACTER
        }
    }

    /*
    * Conversion table for ANSEL characters coded in two bytes
    */
    private fun convert2(ansel: Int): Int {
        return when (ansel) {
            0xE020 -> 0x02C0 //  modifier letter glottal stop
            0xE041 -> 0x1EA2 //  capital a with hook above
            0xE045 -> 0x1EBA //  capital e with hook above
            0xE049 -> 0x1EC8 //  capital i with hook above
            0xE04F -> 0x1ECE //  capital o with hook above
            0xE055 -> 0x1EE6 //  capital u with hook above
            0xE059 -> 0x1EF6 //  capital y with hook above
            0xE061 -> 0x1EA3 //  small a with hook above
            0xE065 -> 0x1EBB //  small e with hook above
            0xE069 -> 0x1EC9 //  small i with hook above
            0xE06F -> 0x1ECF //  small o with hook above
            0xE075 -> 0x1EE7 //  small u with hook above
            0xE079 -> 0x1EF7 //  small y with hook above
            0xE120 -> 0x02CB //  modifier letter grave accent
            0xE141 -> 0x00C0 //  capital A with grave accent
            0xE145 -> 0x00C8 //  capital E with grave accent
            0xE149 -> 0x00CC //  capital I with grave accent
            0xE14F -> 0x00D2 //  capital O with grave accent
            0xE155 -> 0x00D9 //  capital U with grave accent
            0xE157 -> 0x1E80 //  capital w with grave
            0xE159 -> 0x1EF2 //  capital y with grave
            0xE161 -> 0x00E0 //  small a with grave accent
            0xE165 -> 0x00E8 //  small e with grave accent
            0xE169 -> 0x00EC //  small i with grave accent
            0xE16F -> 0x00F2 //  small o with grave accent
            0xE175 -> 0x00F9 //  small u with grave accent
            0xE177 -> 0x1E81 //  small w with grave
            0xE179 -> 0x1EF3 //  small y with grave
            0xE220 -> 0x02CA //  modifier letter acute accent
            0xE241 -> 0x00C1 //  capital A with acute accent
            0xE243 -> 0x0106 //  capital C with acute accent
            0xE245 -> 0x00C9 //  capital E with acute accent
            0xE247 -> 0x01F4 //  capital g with acute
            0xE249 -> 0x00CD //  capital I with acute accent
            0xE24B -> 0x1E30 //  capital k with acute
            0xE24C -> 0x0139 //  capital L with acute accent
            0xE24D -> 0x1E3E //  capital m with acute
            0xE24E -> 0x0143 //  capital N with acute accent
            0xE24F -> 0x00D3 //  capital O with acute accent
            0xE250 -> 0x1E54 //  capital p with acute
            0xE252 -> 0x0154 //  capital R with acute accent
            0xE253 -> 0x015A //  capital S with acute accent
            0xE255 -> 0x00DA //  capital U with acute accent
            0xE257 -> 0x1E82 //  capital w with acute
            0xE259 -> 0x00DD //  capital Y with acute accent
            0xE25A -> 0x0179 //  capital Z with acute accent
            0xE261 -> 0x00E1 //  small a with acute accent
            0xE263 -> 0x0107 //  small c with acute accent
            0xE265 -> 0x00E9 //  small e with acute accent
            0xE267 -> 0x01F5 //  small g with acute
            0xE269 -> 0x00ED //  small i with acute accent
            0xE26B -> 0x1E31 //  small k with acute
            0xE26C -> 0x013A //  small l with acute accent
            0xE26D -> 0x1E3F //  small m with acute
            0xE26E -> 0x0144 //  small n with acute accent
            0xE26F -> 0x00F3 //  small o with acute accent
            0xE270 -> 0x1E55 //  small p with acute
            0xE272 -> 0x0155 //  small r with acute accent
            0xE273 -> 0x015B //  small s with acute accent
            0xE275 -> 0x00FA //  small u with acute accent
            0xE277 -> 0x1E83 //  small w with acute
            0xE279 -> 0x00FD //  small y with acute accent
            0xE27A -> 0x017A //  small z with acute accent
            0xE2A5 -> 0x01FC //  capital ae with acute
            0xE2B5 -> 0x01FD //  small ae with acute
            0xE320 -> 0x02C6 //  modifier letter circumflex accent
            0xE341 -> 0x00C2 //  capital A with circumflex accent
            0xE343 -> 0x0108 //  capital c with circumflex
            0xE345 -> 0x00CA //  capital E with circumflex accent
            0xE347 -> 0x011C //  capital g with circumflex
            0xE348 -> 0x0124 //  capital h with circumflex
            0xE349 -> 0x00CE //  capital I with circumflex accent
            0xE34A -> 0x0134 //  capital j with circumflex
            0xE34F -> 0x00D4 //  capital O with circumflex accent
            0xE353 -> 0x015C //  capital s with circumflex
            0xE355 -> 0x00DB //  capital U with circumflex
            0xE357 -> 0x0174 //  capital w with circumflex
            0xE359 -> 0x0176 //  capital y with circumflex
            0xE35A -> 0x1E90 //  capital z with circumflex
            0xE361 -> 0x00E2 //  small a with circumflex accent
            0xE363 -> 0x0109 //  small c with circumflex
            0xE365 -> 0x00EA //  small e with circumflex accent
            0xE367 -> 0x011D //  small g with circumflex
            0xE368 -> 0x0125 //  small h with circumflex
            0xE369 -> 0x00EE //  small i with circumflex accent
            0xE36A -> 0x0135 //  small j with circumflex
            0xE36F -> 0x00F4 //  small o with circumflex accent
            0xE373 -> 0x015D //  small s with circumflex
            0xE375 -> 0x00FB //  small u with circumflex
            0xE377 -> 0x0175 //  small w with circumflex
            0xE379 -> 0x0177 //  small y with circumflex
            0xE37A -> 0x1E91 //  small z with circumflex
            0xE420 -> 0x02DC //  small tilde
            0xE441 -> 0x00C3 //  capital A with tilde
            0xE445 -> 0x1EBC //  capital e with tilde
            0xE449 -> 0x0128 //  capital i with tilde
            0xE44E -> 0x00D1 //  capital N with tilde
            0xE44F -> 0x00D5 //  capital O with tilde
            0xE455 -> 0x0168 //  capital u with tilde
            0xE456 -> 0x1E7C //  capital v with tilde
            0xE459 -> 0x1EF8 //  capital y with tilde
            0xE461 -> 0x00E3 //  small a with tilde
            0xE465 -> 0x1EBD //  small e with tilde
            0xE469 -> 0x0129 //  small i with tilde
            0xE46E -> 0x00F1 //  small n with tilde
            0xE46F -> 0x00F5 //  small o with tilde
            0xE475 -> 0x0169 //  small u with tilde
            0xE476 -> 0x1E7D //  small v with tilde
            0xE479 -> 0x1EF9 //  small y with tilde
            0xE520 -> 0x02C9 //  modifier letter macron
            0xE541 -> 0x0100 //  capital a with macron
            0xE545 -> 0x0112 //  capital e with macron
            0xE547 -> 0x1E20 //  capital g with macron
            0xE549 -> 0x012A //  capital i with macron
            0xE54F -> 0x014C //  capital o with macron
            0xE555 -> 0x016A //  capital u with macron
            0xE561 -> 0x0101 //  small a with macron
            0xE565 -> 0x0113 //  small e with macron
            0xE567 -> 0x1E21 //  small g with macron
            0xE569 -> 0x012B //  small i with macron
            0xE56F -> 0x014D //  small o with macron
            0xE575 -> 0x016B //  small u with macron
            0xE5A5 -> 0x01E2 //  capital ae with macron
            0xE5B5 -> 0x01E3 //  small ae with macron
            0xE620 -> 0x02D8 //  breve
            0xE641 -> 0x0102 //  capital A with breve
            0xE645 -> 0x0114 //  capital e with breve
            0xE647 -> 0x011E //  capital g with breve
            0xE649 -> 0x012C //  capital i with breve
            0xE64F -> 0x014E //  capital o with breve
            0xE655 -> 0x016C //  capital u with breve
            0xE661 -> 0x0103 //  small a with breve
            0xE665 -> 0x0115 //  small e with breve
            0xE667 -> 0x011F //  small g with breve
            0xE669 -> 0x012D //  small i with breve
            0xE66F -> 0x014F //  small o with breve
            0xE675 -> 0x016D //  small u with breve
            0xE720 -> 0x02D9 //  dot above
            0xE742 -> 0x1E02 //  capital b with dot above
            0xE743 -> 0x010A //  capital c with dot above
            0xE744 -> 0x1E0A //  capital d with dot above
            0xE745 -> 0x0116 //  capital e with dot above
            0xE746 -> 0x1E1E //  capital f with dot above
            0xE747 -> 0x0120 //  capital g with dot above
            0xE748 -> 0x1E22 //  capital h with dot above
            0xE749 -> 0x0130 //  capital i with dot above
            0xE74D -> 0x1E40 //  capital m with dot above
            0xE74E -> 0x1E44 //  capital n with dot above
            0xE750 -> 0x1E56 //  capital p with dot above
            0xE752 -> 0x1E58 //  capital r with dot above
            0xE753 -> 0x1E60 //  capital s with dot above
            0xE754 -> 0x1E6A //  capital t with dot above
            0xE757 -> 0x1E86 //  capital w with dot above
            0xE758 -> 0x1E8A //  capital x with dot above
            0xE759 -> 0x1E8E //  capital y with dot above
            0xE75A -> 0x017B //  capital Z with dot above
            0xE762 -> 0x1E03 //  small b with dot above
            0xE763 -> 0x010B //  small c with dot above
            0xE764 -> 0x1E0B //  small d with dot above
            0xE765 -> 0x0117 //  small e with dot above
            0xE766 -> 0x1E1F //  small f with dot above
            0xE767 -> 0x0121 //  small g with dot above
            0xE768 -> 0x1E23 //  small h with dot above
            0xE76D -> 0x1E41 //  small m with dot above
            0xE76E -> 0x1E45 //  small n with dot above
            0xE770 -> 0x1E57 //  small p with dot above
            0xE772 -> 0x1E59 //  small r with dot above
            0xE773 -> 0x1E61 //  small s with dot above
            0xE774 -> 0x1E6B //  small t with dot above
            0xE777 -> 0x1E87 //  small w with dot above
            0xE778 -> 0x1E8B //  small x with dot above
            0xE779 -> 0x1E8F //  small y with dot above
            0xE77A -> 0x017C //  small z with dot above
            0xE820 -> 0x00A8 //  diaeresis
            0xE841 -> 0x00C4 //  capital A with diaeresis
            0xE845 -> 0x00CB //  capital E with diaeresis
            0xE848 -> 0x1E26 //  capital h with diaeresis
            0xE849 -> 0x00CF //  capital I with diaeresis
            0xE84F -> 0x00D6 //  capital O with diaeresis
            0xE855 -> 0x00DC //  capital U with diaeresis
            0xE857 -> 0x1E84 //  capital w with diaeresis
            0xE858 -> 0x1E8C //  capital x with diaeresis
            0xE859 -> 0x0178 //  capital y with diaeresis
            0xE861 -> 0x00E4 //  small a with diaeresis
            0xE865 -> 0x00EB //  small e with diaeresis
            0xE868 -> 0x1E27 //  small h with diaeresis
            0xE869 -> 0x00EF //  small i with diaeresis
            0xE86F -> 0x00F6 //  small o with diaeresis
            0xE874 -> 0x1E97 //  small t with diaeresis
            0xE875 -> 0x00FC //  small u with diaeresis
            0xE877 -> 0x1E85 //  small w with diaeresis
            0xE878 -> 0x1E8D //  small x with diaeresis
            0xE879 -> 0x00FF //  small y with diaeresis
            0xE920 -> 0x02C7 //  caron
            0xE941 -> 0x01CD //  capital a with caron
            0xE943 -> 0x010C //  capital C with caron
            0xE944 -> 0x010E //  capital D with caron
            0xE945 -> 0x011A //  capital E with caron
            0xE947 -> 0x01E6 //  capital g with caron
            0xE949 -> 0x01CF //  capital i with caron
            0xE94B -> 0x01E8 //  capital k with caron
            0xE94C -> 0x013D //  capital L with caron
            0xE94E -> 0x0147 //  capital N with caron
            0xE94F -> 0x01D1 //  capital o with caron
            0xE952 -> 0x0158 //  capital R with caron
            0xE953 -> 0x0160 //  capital S with caron
            0xE954 -> 0x0164 //  capital T with caron
            0xE955 -> 0x01D3 //  capital u with caron
            0xE95A -> 0x017D //  capital Z with caron
            0xE961 -> 0x01CE //  small a with caron
            0xE963 -> 0x010D //  small c with caron
            0xE964 -> 0x010F //  small d with caron
            0xE965 -> 0x011B //  small e with caron
            0xE967 -> 0x01E7 //  small g with caron
            0xE969 -> 0x01D0 //  small i with caron
            0xE96A -> 0x01F0 //  small j with caron
            0xE96B -> 0x01E9 //  small k with caron
            0xE96C -> 0x013E //  small l with caron
            0xE96E -> 0x0148 //  small n with caron
            0xE96F -> 0x01D2 //  small o with caron
            0xE972 -> 0x0159 //  small r with caron
            0xE973 -> 0x0161 //  small s with caron
            0xE974 -> 0x0165 //  small t with caron
            0xE975 -> 0x01D4 //  small u with caron
            0xE97A -> 0x017E //  small z with caron
            0xEA20 -> 0x02DA //  ring above
            0xEA41 -> 0x00C5 //  capital A with ring above
            0xEA61 -> 0x00E5 //  small a with ring above
            0xEA75 -> 0x016F //  small u with ring above
            0xEA77 -> 0x1E98 //  small w with ring above
            0xEA79 -> 0x1E99 //  small y with ring above
            0xEAAD -> 0x016E //  capital U with ring above
            0xED20 -> 0x02BC //  modifier letter apostrophe
            0xEE20 -> 0x02DD //  double acute accent
            0xEE4F -> 0x0150 //  capital O with double acute
            0xEE55 -> 0x0170 //  capital U with double acute
            0xEE6F -> 0x0151 //  small o with double acute
            0xEE75 -> 0x0171 //  small u with double acute
            0xF020 -> 0x00B8 //  cedilla
            0xF043 -> 0x00C7 //  capital C with cedilla
            0xF044 -> 0x1E10 //  capital d with cedilla
            0xF047 -> 0x0122 //  capital g with cedilla
            0xF048 -> 0x1E28 //  capital h with cedilla
            0xF04B -> 0x0136 //  capital k with cedilla
            0xF04C -> 0x013B //  capital l with cedilla
            0xF04E -> 0x0145 //  capital n with cedilla
            0xF052 -> 0x0156 //  capital r with cedilla
            0xF053 -> 0x015E //  capital S with cedilla
            0xF054 -> 0x0162 //  capital T with cedilla
            0xF063 -> 0x00E7 //  small c with cedilla
            0xF064 -> 0x1E11 //  small d with cedilla
            0xF067 -> 0x0123 //  small g with cedilla
            0xF068 -> 0x1E29 //  small h with cedilla
            0xF06B -> 0x0137 //  small k with cedilla
            0xF06C -> 0x013C //  small l with cedilla
            0xF06E -> 0x0146 //  small n with cedilla
            0xF072 -> 0x0157 //  small r with cedilla
            0xF073 -> 0x015F //  small s with cedilla
            0xF074 -> 0x0163 //  small t with cedilla
            0xF120 -> 0x02DB //  ogonek
            0xF141 -> 0x0104 //  capital A with ogonek
            0xF145 -> 0x0118 //  capital E with ogonek
            0xF149 -> 0x012E //  capital i with ogonek
            0xF14F -> 0x01EA //  capital o with ogonek
            0xF155 -> 0x0172 //  capital u with ogonek
            0xF161 -> 0x0105 //  small a with ogonek
            0xF165 -> 0x0119 //  small e with ogonek
            0xF169 -> 0x012F //  small i with ogonek
            0xF16F -> 0x01EB //  small o with ogonek
            0xF175 -> 0x0173 //  small u with ogonek
            0xF241 -> 0x1EA0 //  capital a with dot below
            0xF242 -> 0x1E04 //  capital b with dot below
            0xF244 -> 0x1E0C //  capital d with dot below
            0xF245 -> 0x1EB8 //  capital e with dot below
            0xF248 -> 0x1E24 //  capital h with dot below
            0xF249 -> 0x1ECA //  capital i with dot below
            0xF24B -> 0x1E32 //  capital k with dot below
            0xF24C -> 0x1E36 //  capital l with dot below
            0xF24D -> 0x1E42 //  capital m with dot below
            0xF24E -> 0x1E46 //  capital n with dot below
            0xF24F -> 0x1ECC //  capital o with dot below
            0xF252 -> 0x1E5A //  capital r with dot below
            0xF253 -> 0x1E62 //  capital s with dot below
            0xF254 -> 0x1E6C //  capital t with dot below
            0xF255 -> 0x1EE4 //  capital u with dot below
            0xF256 -> 0x1E7E //  capital v with dot below
            0xF257 -> 0x1E88 //  capital w with dot below
            0xF259 -> 0x1EF4 //  capital y with dot below
            0xF25A -> 0x1E92 //  capital z with dot below
            0xF261 -> 0x1EA1 //  small a with dot below
            0xF262 -> 0x1E05 //  small b with dot below
            0xF264 -> 0x1E0D //  small d with dot below
            0xF265 -> 0x1EB9 //  small e with dot below
            0xF268 -> 0x1E25 //  small h with dot below
            0xF269 -> 0x1ECB //  small i with dot below
            0xF26B -> 0x1E33 //  small k with dot below
            0xF26C -> 0x1E37 //  small l with dot below
            0xF26D -> 0x1E43 //  small m with dot below
            0xF26E -> 0x1E47 //  small n with dot below
            0xF26F -> 0x1ECD //  small o with dot below
            0xF272 -> 0x1E5B //  small r with dot below
            0xF273 -> 0x1E63 //  small s with dot below
            0xF274 -> 0x1E6D //  small t with dot below
            0xF275 -> 0x1EE5 //  small u with dot below
            0xF276 -> 0x1E7F //  small v with dot below
            0xF277 -> 0x1E89 //  small w with dot below
            0xF279 -> 0x1EF5 //  small y with dot below
            0xF27A -> 0x1E93 //  small z with dot below
            0xF355 -> 0x1E72 //  capital u with diaeresis below
            0xF375 -> 0x1E73 //  small u with diaeresis below
            0xF441 -> 0x1E00 //  capital a with ring below
            0xF461 -> 0x1E01 //  small a with ring below
            0xF520 -> 0x2017 //  double line below
            0xF948 -> 0x1E2A //  capital h with breve below
            0xF968 -> 0x1E2B //  small h with breve below
            0xFC20 -> 0x0338 //  space with diagonal stroke
            0xFC41 -> 0x023A //  capital A with diagonal stroke
            0xFC43 -> 0x023B //  capital C with diagonal stroke
            0xFC45 -> 0x0246 //  capital E with diagonal stroke
            0xFC4B -> 0xA742 //  capital K with diagonal stroke
            0xFC4C -> 0x0141 //  capital L with diagonal stroke
            0xFC4F -> 0x00D8 //  capital O with diagonal stroke
            0xFC51 -> 0xA758 //  capital Q with diagonal stroke
            0xFC54 -> 0x023E //  capital T with diagonal stroke
            0xFC56 -> 0xA75E //  capital V with diagonal stroke
            0xFC61 -> 0x2C65 //  small a with diagonal stroke
            0xFC63 -> 0x023C //  small c with diagonal stroke
            0xFC65 -> 0x0247 //  small e with diagonal stroke
            0xFC6B -> 0xA743 //  small k with diagonal stroke
            0xFC6C -> 0x0142 //  small l with diagonal stroke
            0xFC6F -> 0x00F8 //  small o with diagonal stroke
            0xFC71 -> 0xA759 //  small q with diagonal stroke
            0xFC74 -> 0x2C66 //  small t with diagonal stroke
            0xFC76 -> 0xA75F //  small v with diagonal stroke
            0xD741 -> 0x2550 //  box drawings double horizontal
            0xD742 -> 0x2551 //  box drawings double vertical
            0xD743 -> 0x2557 //  box drawings double down and left
            0xD744 -> 0x255D //  box drawings double up and left
            0xD745 -> 0x255A //  box drawings double up and right
            0xD746 -> 0x2554 //  box drawings double down and right
            0xD747 -> 0x2563 //  box drawings double vertical and left
            0xD748 -> 0x2569 //  box drawings double up and horizontal
            0xD749 -> 0x2560 //  box drawings double vertical and right
            0xD74A -> 0x2566 //  box drawings double down and horizontal
            0xD74B -> 0x256C //  box drawings double vertical and horizontal
            0xD74C -> 0x2562 //  box drawings double vertical and single left
            0xD74D -> 0x2567 //  box drawings single up and double horizontal
            0xD74E -> 0x255F //  box drawings double vertical and single right
            0xD74F -> 0x2564 //  box drawings single down and double horizontal
            0xD750 -> 0x2561 //  box drawings single vertical and double left
            0xD751 -> 0x2568 //  box drawings double up and single horizontal
            0xD752 -> 0x255E //  box drawings single vertical and double right
            0xD753 -> 0x2565 //  box drawings double down and single horizontal
            0xD754 -> 0x256A //  box drawings single vertical and double horizontal
            0xD755 -> 0x2591 //  light shade - 25%
            0xD756 -> 0x2592 //  medium shade - 50%
            0xD757 -> 0x2593 //  dark shade - 75%
            0xD758 -> 0x2588 //  full block
            0xD761 -> 0x2500 //  box drawings light horizontal
            0xD762 -> 0x2502 //  box drawings light vertical
            0xD763 -> 0x2510 //  box drawings light down and left
            0xD764 -> 0x2518 //  box drawings light up and left
            0xD765 -> 0x2514 //  box drawings light up and right
            0xD766 -> 0x250C //  box drawings light down and right
            0xD767 -> 0x2524 //  box drawings light vertical and left
            0xD768 -> 0x2534 //  box drawings light up and horizontal
            0xD769 -> 0x251C //  box drawings light vertical and right
            0xD76A -> 0x252C //  box drawings light down and horizontal
            0xD76B -> 0x253C //  box drawings light vertical and horizontal
            0xD76C -> 0x2555 //  box drawings light single down and double left
            0xD76D -> 0x255B //  box drawings light single up and double left
            0xD76E -> 0x2558 //  box drawings light single up and double right
            0xD76F -> 0x2552 //  box drawings light single down and double right
            0xD770 -> 0x2556 //  box drawings light double down and single left
            0xD771 -> 0x255C //  box drawings light double up and single left
            0xD772 -> 0x2559 //  box drawings light double up and single right
            0xD773 -> 0x2553 //  box drawings light double down and single right
            0xD774 -> 0x256B //  box drawings light double vertical and single horizontal
            0xD775 -> 0x258C //  left half block
            0xD776 -> 0x2580 //  upper half block
            0xD777 -> 0x2590 //  right half block
            0xD778 -> 0x2584 //  lower half block
            0xD779 -> 0x25AA //  black small square
            0xD824 -> 0x03C2 //  Greek small letter final sigma
            0xD841 -> 0x0391 //  Greek capital letter alpha
            0xD842 -> 0x0392 //  Greek capital letter beta
            0xD843 -> 0x03A5 //  Greek capital letter upsilon
            0xD844 -> 0x0394 //  Greek capital letter delta
            0xD845 -> 0x0395 //  Greek capital letter epsilon
            0xD846 -> 0x03A6 //  Greek capital letter phi
            0xD847 -> 0x0393 //  Greek capital letter gamma
            0xD848 -> 0x03A8 //  Greek capital letter psi
            0xD849 -> 0x0399 //  Greek capital letter iota
            0xD84B -> 0x039A //  Greek capital letter kappa
            0xD84C -> 0x039B //  Greek capital letter lamda
            0xD84D -> 0x039C //  Greek capital letter mu
            0xD84E -> 0x039D //  Greek capital letter nu
            0xD84F -> 0x039F //  Greek capital letter omicron
            0xD850 -> 0x03A0 //  Greek capital letter pi
            0xD851 -> 0x03A7 //  Greek capital letter chi
            0xD852 -> 0x03A1 //  Greek capital letter rho
            0xD853 -> 0x03A3 //  Greek capital letter sigma
            0xD854 -> 0x03A4 //  Greek capital letter tau
            0xD855 -> 0x03A9 //  Greek capital letter omega
            0xD856 -> 0x0398 //  Greek capital letter theta
            0xD857 -> 0x0397 //  Greek capital letter eta
            0xD858 -> 0x039E //  Greek capital letter xi
            0xD85A -> 0x0396 //  Greek capital letter zeta
            0xD861 -> 0x03B1 //  Greek small letter alpha
            0xD862 -> 0x03B2 //  Greek small letter beta
            0xD863 -> 0x03C5 //  Greek small letter upsilon
            0xD864 -> 0x03B4 //  Greek small letter delta
            0xD865 -> 0x03B5 //  Greek small letter epsilon
            0xD866 -> 0x03C6 //  Greek small letter phi
            0xD867 -> 0x03B3 //  Greek small letter gamma
            0xD868 -> 0x03C8 //  Greek small letter psi
            0xD869 -> 0x03B9 //  Greek small letter iota
            0xD86B -> 0x03BA //  Greek small letter kappa
            0xD86C -> 0x03BB //  Greek small letter lamda
            0xD86D -> 0x03BC //  Greek small letter mu
            0xD86E -> 0x03BD //  Greek small letter nu
            0xD86F -> 0x03BF //  Greek small letter omicron
            0xD870 -> 0x03C0 //  Greek small letter pi
            0xD871 -> 0x03C7 //  Greek small letter chi
            0xD872 -> 0x03C1 //  Greek small letter rho
            0xD873 -> 0x03C3 //  Greek small letter sigma
            0xD874 -> 0x03C4 //  Greek small letter tau
            0xD875 -> 0x03C9 //  Greek small letter omega
            0xD876 -> 0x03B8 //  Greek small letter theta
            0xD877 -> 0x03B7 //  Greek small letter eta
            0xD878 -> 0x03BE //  Greek small letter xi
            0xD87A -> 0x03B6 //  Greek small letter zeta
            0xD920 -> 0x0E3F //  Thai currency symbol baht
            0xD921 -> 0x00A2 //  cent sign
            0xD922 -> 0x00A5 //  yen sign
            0xD923 -> 0x20A7 //  peseta sign
            0xD924 -> 0x0192 //  florin currency sign
            0xD925 -> 0x00A4 //  currency sign
            0xD926 -> 0x20A4 //  lira sign
            0xD927 -> 0x20A0 //  Euro-currency sign
            0xD928 -> 0x20A1 //  colon sign
            0xD929 -> 0x20A2 //  cruzeiro sign
            0xD92A -> 0x20A3 //  French franc sign
            0xD92B -> 0x20A5 //  mill sign
            0xD92C -> 0x20A6 //  naira sign
            0xD92D -> 0x20A8 //  rupee sign
            0xD92E -> 0x20A9 //  won sign
            0xD92F -> 0x20AA //  new sheqel sign
            0xD930 -> 0x20AC //  Euro sign
            0xD931 -> 0x00B9 //  superscript one
            0xD932 -> 0x00B2 //  superscript two
            0xD933 -> 0x00B3 //  superscript three
            0xD941 -> 0x00AA //  feminine ordinal indicator
            0xD942 -> 0x00BA //  masculine ordinal indicator
            0xD943 -> 0x00AB //  left-pointing double angle quotation mark
            0xD944 -> 0x00BB //  right-pointing double angle quotation mark
            0xD945 -> 0x00A6 //  broken vertical bar
            0xD946 -> 0x00B6 //  pilcrow/paragraph sign
            0xD947 -> 0x00A7 //  section sign
            0xD948 -> 0x2310 //  reversed not sign
            0xD949 -> 0x2020 //  dagger
            0xD94A -> 0x2021 //  double dagger
            0xD94C -> 0x2122 //  trade mark sign
            0xD94D -> 0xFB01 //  Latin small ligature fi
            0xD94E -> 0xFB02 //  Latin small ligature fl
            0xD94F -> 0x2039 //  left-pointing single angle quotation mark
            0xD950 -> 0x203A //  right-pointing single angle quotation mark
            0xD951 -> 0x2030 //  per mille sign
            0xD952 -> 0x2026 //  horizontal ellipsis
            0xD953 -> 0x201C //  left double quotation mark
            0xD954 -> 0x201D //  right double quotation mark
            0xD955 -> 0x201A //  single low-9 quotation mark
            0xD956 -> 0x201E //  double low-9 quotation mark
            0xD957 -> 0x2022 //  bullet
            0xD958 -> 0x2013 //  en dash
            0xD959 -> 0x00A0 //  no-break space
            0xD95A -> 0x00B5 //  micro sign
            0xD961 -> 0x00BD //  vulgar fraction one half
            0xD962 -> 0x00BC //  vulgar fraction one quarter
            0xD963 -> 0x00BE //  vulgar fraction three quarters
            0xD964 -> 0x221E //  infinity
            0xD965 -> 0x2205 //  empty set
            0xD966 -> 0x2208 //  element of
            0xD967 -> 0x2229 //  intersection
            0xD968 -> 0x00AC //  not sign
            0xD969 -> 0x2261 //  identical to
            0xD96A -> 0x00D7 //  multiplication sign
            0xD96B -> 0x2265 //  greater-than or equal to
            0xD96C -> 0x2264 //  less-than or equal to
            0xD96D -> 0x00F7 //  division sign
            0xD96E -> 0x2248 //  almost equal to
            0xD96F -> 0x22C5 //  dot operator
            0xD970 -> 0x2320 //  top half integral
            0xD971 -> 0x2321 //  bottom half integral
            0xD972 -> 0x221A //  square root
            0xD973 -> 0x2044 //  fraction/division slash
            0xD974 -> 0x2018 //  left single quotation mark
            0xD975 -> 0x2019 //  right single quotation mark
            0xD976 -> 0x2014 //  em dash
            0xD977 -> 0x00AD //  soft hyphen
            0xD979 -> 0x20AD //  kip sign
            0xD97A -> 0x20AE //  tugrik sign
            0xD97B -> 0x20AF //  drachma sign
            0xD97C -> 0x20AB //  dong sign

            else -> -1
        }
    }
}
