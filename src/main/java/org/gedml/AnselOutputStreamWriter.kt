package org.gedml

import java.io.*

/*
* org.lm.gedml.AnselOutputStreamWriter
* This class produces an output stream of bytes representing
* ANSEL-encoded characters, from UNICODE characters supplied as
* input.
* @Author: mhkay@iclway.co.uk
* @Version: 20 May 1998
* 20 May 1998: conversion tables updated with input from John Cowan
* Nov 2011: conversion tables updated again based upon
* http://www.heiner-eichmann.de/gedcom/oldansset.htm and http://lcweb2.loc.gov/diglib/codetables/45.html
*/
class AnselOutputStreamWriter(private val output: OutputStream?) : OutputStreamWriter(output) {
    /*
    * Write one UNICODE character
    */
    @Throws(IOException::class)
    override fun write(c: Int) {
        val ansel: Int
        if (c < 128) output!!.write(c)
        else {
            ansel = convert(c)
            if (ansel < 256) {
                output!!.write(ansel)
            } else {
                output!!.write(ansel / 256)
                output.write(ansel % 256)
            }
        }
    }

    /*
    * Write part of an array of UNICODE characters
    */
    @Throws(IOException::class)
    override fun write(cbuf: CharArray, off: Int, len: Int) {
        for (i in off until off + len) {
            write(cbuf[i].code)
        }
    }

    /*
    * Write a string of UNICODE characters
    */
    @Throws(IOException::class)
    override fun write(s: String) {
        for (i in 0 until s.length) {
            write(s[i].code)
        }
    }

    /*
    * Determine the character code in use
    */
    override fun getEncoding(): String {
        return "ANSEL"
    }

    /*
    * Conversion table for UNICODE to Ansel
    */
    private fun convert(unicode: Int): Int {
        return when (unicode) {
            0x00A1 -> 0xC6 //  inverted exclamation mark
            0x00A3 -> 0xB9 //  pound sign
            0x00A8 -> 0xE820 //  space with diaeresis
            0x00A9 -> 0xC3 //  copyright sign
            0x00AE -> 0xAA //  registered trade mark sign
            0x00B0 -> 0xC0 //  degree sign, ring above
            0x00B1 -> 0xAB //  plus-minus sign
            0x00B7 -> 0xA8 //  middle dot
            0x00B8 -> 0xF020 //  cedilla
            0x00BF -> 0xC5 //  inverted question mark
            0x00C0 -> 0xE141 //  capital A with grave accent
            0x00C1 -> 0xE241 //  capital A with acute accent
            0x00C2 -> 0xE341 //  capital A with circumflex accent
            0x00C3 -> 0xE441 //  capital A with tilde
            0x00C4 -> 0xE841 //  capital A with diaeresis
            0x00C5 -> 0xEA41 //  capital A with ring above
            0x00C6 -> 0xA5 //  capital diphthong A with E
            0x00C7 -> 0xF043 //  capital C with cedilla
            0x00C8 -> 0xE145 //  capital E with grave accent
            0x00C9 -> 0xE245 //  capital E with acute accent
            0x00CA -> 0xE345 //  capital E with circumflex accent
            0x00CB -> 0xE845 //  capital E with diaeresis
            0x00CC -> 0xE149 //  capital I with grave accent
            0x00CD -> 0xE249 //  capital I with acute accent
            0x00CE -> 0xE349 //  capital I with circumflex accent
            0x00CF -> 0xE849 //  capital I with diaeresis
            0x00D0 -> 0xA3 //  capital icelandic letter Eth
            0x00D1 -> 0xE44E //  capital N with tilde
            0x00D2 -> 0xE14F //  capital O with grave accent
            0x00D3 -> 0xE24F //  capital O with acute accent
            0x00D4 -> 0xE34F //  capital O with circumflex accent
            0x00D5 -> 0xE44F //  capital O with tilde
            0x00D6 -> 0xE84F //  capital O with diaeresis
            0x00D8 -> 0xA2 //  capital O with oblique stroke
            0x00D9 -> 0xE155 //  capital U with grave accent
            0x00DA -> 0xE255 //  capital U with acute accent
            0x00DB -> 0xE355 //  capital U with circumflex
            0x00DC -> 0xE855 //  capital U with diaeresis
            0x00DD -> 0xE259 //  capital Y with acute accent
            0x00DE -> 0xA4 //  capital Icelandic letter Thorn
            0x00DF -> 0xCF //  small German letter sharp s
            0x00E0 -> 0xE161 //  small a with grave accent
            0x00E1 -> 0xE261 //  small a with acute accent
            0x00E2 -> 0xE361 //  small a with circumflex accent
            0x00E3 -> 0xE461 //  small a with tilde
            0x00E4 -> 0xE861 //  small a with diaeresis
            0x00E5 -> 0xEA61 //  small a with ring above
            0x00E6 -> 0xB5 //  small diphthong a with e
            0x00E7 -> 0xF063 //  small c with cedilla
            0x00E8 -> 0xE165 //  small e with grave accent
            0x00E9 -> 0xE265 //  small e with acute accent
            0x00EA -> 0xE365 //  small e with circumflex accent
            0x00EB -> 0xE865 //  small e with diaeresis
            0x00EC -> 0xE169 //  small i with grave accent
            0x00ED -> 0xE269 //  small i with acute accent
            0x00EE -> 0xE369 //  small i with circumflex accent
            0x00EF -> 0xE869 //  small i with diaeresis
            0x00F0 -> 0xBA //  small Icelandic letter Eth
            0x00F1 -> 0xE46E //  small n with tilde
            0x00F2 -> 0xE16F //  small o with grave accent
            0x00F3 -> 0xE26F //  small o with acute accent
            0x00F4 -> 0xE36F //  small o with circumflex accent
            0x00F5 -> 0xE46F //  small o with tilde
            0x00F6 -> 0xE86F //  small o with diaeresis
            0x00F8 -> 0xB2 //  small o with oblique stroke
            0x00F9 -> 0xE175 //  small u with grave accent
            0x00FA -> 0xE275 //  small u with acute accent
            0x00FB -> 0xE375 //  small u with circumflex
            0x00FC -> 0xE875 //  small u with diaeresis
            0x00FD -> 0xE279 //  small y with acute accent
            0x00FE -> 0xB4 //  small Icelandic letter Thorn
            0x00FF -> 0xE879 //  small y with diaeresis
            0x0100 -> 0xE541 //  capital a with macron
            0x0101 -> 0xE561 //  small a with macron
            0x0102 -> 0xE641 //  capital A with breve
            0x0103 -> 0xE661 //  small a with breve
            0x0104 -> 0xF141 //  capital A with ogonek
            0x0105 -> 0xF161 //  small a with ogonek
            0x0106 -> 0xE243 //  capital C with acute accent
            0x0107 -> 0xE263 //  small c with acute accent
            0x0108 -> 0xE343 //  capital c with circumflex
            0x0109 -> 0xE363 //  small c with circumflex
            0x010A -> 0xE743 //  capital c with dot above
            0x010B -> 0xE763 //  small c with dot above
            0x010C -> 0xE943 //  capital C with caron
            0x010D -> 0xE963 //  small c with caron
            0x010E -> 0xE944 //  capital D with caron
            0x010F -> 0xE964 //  small d with caron
            0x0110 -> 0xA3 //  capital D with stroke
            0x0111 -> 0xB3 //  small D with stroke
            0x0112 -> 0xE545 //  capital e with macron
            0x0113 -> 0xE565 //  small e with macron
            0x0114 -> 0xE645 //  capital e with breve
            0x0115 -> 0xE665 //  small e with breve
            0x0116 -> 0xE745 //  capital e with dot above
            0x0117 -> 0xE765 //  small e with dot above
            0x0118 -> 0xF145 //  capital E with ogonek
            0x0119 -> 0xF165 //  small e with ogonek
            0x011A -> 0xE945 //  capital E with caron
            0x011B -> 0xE965 //  small e with caron
            0x011C -> 0xE347 //  capital g with circumflex
            0x011D -> 0xE367 //  small g with circumflex
            0x011E -> 0xE647 //  capital g with breve
            0x011F -> 0xE667 //  small g with breve
            0x0120 -> 0xE747 //  capital g with dot above
            0x0121 -> 0xE767 //  small g with dot above
            0x0122 -> 0xF047 //  capital g with cedilla
            0x0123 -> 0xF067 //  small g with cedilla
            0x0124 -> 0xE348 //  capital h with circumflex
            0x0125 -> 0xE368 //  small h with circumflex
            0x0128 -> 0xE449 //  capital i with tilde
            0x0129 -> 0xE469 //  small i with tilde
            0x012A -> 0xE549 //  capital i with macron
            0x012B -> 0xE569 //  small i with macron
            0x012C -> 0xE649 //  capital i with breve
            0x012D -> 0xE669 //  small i with breve
            0x012E -> 0xF149 //  capital i with ogonek
            0x012F -> 0xF169 //  small i with ogonek
            0x0130 -> 0xE749 //  capital i with dot above
            0x0131 -> 0xB8 //  small dotless i
            0x0134 -> 0xE34A //  capital j with circumflex
            0x0135 -> 0xE36A //  small j with circumflex
            0x0136 -> 0xF04B //  capital k with cedilla
            0x0137 -> 0xF06B //  small k with cedilla
            0x0139 -> 0xE24C //  capital L with acute accent
            0x013A -> 0xE26C //  small l with acute accent
            0x013B -> 0xF04C //  capital l with cedilla
            0x013C -> 0xF06C //  small l with cedilla
            0x013D -> 0xE94C //  capital L with caron
            0x013E -> 0xE96C //  small l with caron
            0x0141 -> 0xA1 //  capital L with stroke
            0x0142 -> 0xB1 //  small l with stroke
            0x0143 -> 0xE24E //  capital N with acute accent
            0x0144 -> 0xE26E //  small n with acute accent
            0x0145 -> 0xF04E //  capital n with cedilla
            0x0146 -> 0xF06E //  small n with cedilla
            0x0147 -> 0xE94E //  capital N with caron
            0x0148 -> 0xE96E //  small n with caron
            0x014C -> 0xE54F //  capital o with macron
            0x014D -> 0xE56F //  small o with macron
            0x014E -> 0xE64F //  capital o with breve
            0x014F -> 0xE66F //  small o with breve
            0x0150 -> 0xEE4F //  capital O with double acute
            0x0151 -> 0xEE6F //  small o with double acute
            0x0152 -> 0xA6 //  capital ligature OE
            0x0153 -> 0xB6 //  small ligature OE
            0x0154 -> 0xE252 //  capital R with acute accent
            0x0155 -> 0xE272 //  small r with acute accent
            0x0156 -> 0xF052 //  capital r with cedilla
            0x0157 -> 0xF072 //  small r with cedilla
            0x0158 -> 0xE952 //  capital R with caron
            0x0159 -> 0xE972 //  small r with caron
            0x015A -> 0xE253 //  capital S with acute accent
            0x015B -> 0xE273 //  small s with acute accent
            0x015C -> 0xE353 //  capital s with circumflex
            0x015D -> 0xE373 //  small s with circumflex
            0x015E -> 0xF053 //  capital S with cedilla
            0x015F -> 0xF073 //  small s with cedilla
            0x0160 -> 0xE953 //  capital S with caron
            0x0161 -> 0xE973 //  small s with caron
            0x0162 -> 0xF054 //  capital T with cedilla
            0x0163 -> 0xF074 //  small t with cedilla
            0x0164 -> 0xE954 //  capital T with caron
            0x0165 -> 0xE974 //  small t with caron
            0x0168 -> 0xE455 //  capital u with tilde
            0x0169 -> 0xE475 //  small u with tilde
            0x016A -> 0xE555 //  capital u with macron
            0x016B -> 0xE575 //  small u with macron
            0x016C -> 0xE655 //  capital u with breve
            0x016D -> 0xE675 //  small u with breve
            0x016E -> 0xEAAD //  capital U with ring above
            0x016F -> 0xEA75 //  small u with ring above
            0x0170 -> 0xEE55 //  capital U with double acute
            0x0171 -> 0xEE75 //  small u with double acute
            0x0172 -> 0xF155 //  capital u with ogonek
            0x0173 -> 0xF175 //  small u with ogonek
            0x0174 -> 0xE357 //  capital w with circumflex
            0x0175 -> 0xE377 //  small w with circumflex
            0x0176 -> 0xE359 //  capital y with circumflex
            0x0177 -> 0xE379 //  small y with circumflex
            0x0178 -> 0xE859 //  capital y with diaeresis
            0x0179 -> 0xE25A //  capital Z with acute accent
            0x017A -> 0xE27A //  small z with acute accent
            0x017B -> 0xE75A //  capital Z with dot above
            0x017C -> 0xE77A //  small z with dot above
            0x017D -> 0xE95A //  capital Z with caron
            0x017E -> 0xE97A //  small z with caron
            0x01A0 -> 0xAC //  capital O with horn
            0x01A1 -> 0xBC //  small o with horn
            0x01AF -> 0xAD //  capital U with horn
            0x01B0 -> 0xBD //  small u with horn
            0x01CD -> 0xE941 //  capital a with caron
            0x01CE -> 0xE961 //  small a with caron
            0x01CF -> 0xE949 //  capital i with caron
            0x01D0 -> 0xE969 //  small i with caron
            0x01D1 -> 0xE94F //  capital o with caron
            0x01D2 -> 0xE96F //  small o with caron
            0x01D3 -> 0xE955 //  capital u with caron
            0x01D4 -> 0xE975 //  small u with caron
            0x01E2 -> 0xE5A5 //  capital ae with macron
            0x01E3 -> 0xE5B5 //  small ae with macron
            0x01E6 -> 0xE947 //  capital g with caron
            0x01E7 -> 0xE967 //  small g with caron
            0x01E8 -> 0xE94B //  capital k with caron
            0x01E9 -> 0xE96B //  small k with caron
            0x01EA -> 0xF14F //  capital o with ogonek
            0x01EB -> 0xF16F //  small o with ogonek
            0x01F0 -> 0xE96A //  small j with caron
            0x01F4 -> 0xE247 //  capital g with acute
            0x01F5 -> 0xE267 //  small g with acute
            0x01FC -> 0xE2A5 //  capital ae with acute
            0x01FD -> 0xE2B5 //  small ae with acute
            0x02B9 -> 0xA7 //  modified letter prime
            0x02BA -> 0xB7 //  modified letter double prime
            0x02BC -> 0xED20 //  space with high comma
            0x02BE -> 0xAE //  modifier letter right half ring
            0x02BF -> 0xB0 //  modifier letter left half ring
            0x02C0 -> 0xE020 //  space with low-rising tone mark
            0x02C6 -> 0xE320 //  space with circumflex accent
            0x02C7 -> 0xE920 //  space with caron
            0x02C9 -> 0xE520 //  space with macron
            0x02CA -> 0xE220 //  space with acute accent
            0x02CB -> 0xE120 //  space with grave accent
            0x02D8 -> 0xE620 //  space with breve
            0x02D9 -> 0xE720 //  space with dot above
            0x02DA -> 0xEA20 //  space with ring above
            0x02DB -> 0xF120 //  space with ogonek
            0x02DC -> 0xE420 //  space with tilde
            0x02DD -> 0xEE20 //  space with double acute accent
            0x0300 -> 0xE1 //  grave accent
            0x0301 -> 0xE2 //  acute accent
            0x0302 -> 0xE3 //  circumflex accent
            0x0303 -> 0xE4 //  tilde
            0x0304 -> 0xE5 //  combining macron
            0x0306 -> 0xE6 //  breve
            0x0307 -> 0xE7 //  dot above
            0x0309 -> 0xE0 //  hook above
            0x030A -> 0xEA //  ring above
            0x030B -> 0xEE //  double acute accent
            0x030C -> 0xE9 //  caron
            0x0310 -> 0xEF //  candrabindu
            0x0313 -> 0xFE //  comma above
            0x0315 -> 0xED //  comma above right
            0x031C -> 0xF8 //  combining half ring below
            0x0323 -> 0xF2 //  dot below
            0x0324 -> 0xF3 //  diaeresis below
            0x0325 -> 0xF4 //  ring below
            0x0326 -> 0xF7 //  comma below
            0x0327 -> 0xF0 //  combining cedilla
            0x0328 -> 0xF1 //  ogonek
            0x032E -> 0xF9 //  breve below
            0x0332 -> 0xF6 //  low line (= line below?)
            0x0333 -> 0xF5 //  double low line
            0x1E00 -> 0xF441 //  capital a with ring below
            0x1E01 -> 0xF461 //  small a with ring below
            0x1E02 -> 0xE742 //  capital b with dot above
            0x1E03 -> 0xE762 //  small b with dot above
            0x1E04 -> 0xF242 //  capital b with dot below
            0x1E05 -> 0xF262 //  small b with dot below
            0x1E0A -> 0xE744 //  capital d with dot above
            0x1E0B -> 0xE764 //  small d with dot above
            0x1E0C -> 0xF244 //  capital d with dot below
            0x1E0D -> 0xF264 //  small d with dot below
            0x1E10 -> 0xF044 //  capital d with cedilla
            0x1E11 -> 0xF064 //  small d with cedilla
            0x1E1E -> 0xE746 //  capital f with dot above
            0x1E1F -> 0xE766 //  small f with dot above
            0x1E20 -> 0xE547 //  capital g with macron
            0x1E21 -> 0xE567 //  small g with macron
            0x1E22 -> 0xE748 //  capital h with dot above
            0x1E23 -> 0xE768 //  small h with dot above
            0x1E24 -> 0xF248 //  capital h with dot below
            0x1E25 -> 0xF268 //  small h with dot below
            0x1E26 -> 0xE848 //  capital h with diaeresis
            0x1E27 -> 0xE868 //  small h with diaeresis
            0x1E28 -> 0xF048 //  capital h with cedilla
            0x1E29 -> 0xF068 //  small h with cedilla
            0x1E2A -> 0xF948 //  capital h with breve below
            0x1E2B -> 0xF968 //  small h with breve below
            0x1E30 -> 0xE24B //  capital k with acute
            0x1E31 -> 0xE26B //  small k with acute
            0x1E32 -> 0xF24B //  capital k with dot below
            0x1E33 -> 0xF26B //  small k with dot below
            0x1E36 -> 0xF24C //  capital l with dot below
            0x1E37 -> 0xF26C //  small l with dot below
            0x1E3E -> 0xE24D //  capital m with acute
            0x1E3F -> 0xE26D //  small m with acute
            0x1E40 -> 0xE74D //  capital m with dot above
            0x1E41 -> 0xE76D //  small m with dot above
            0x1E42 -> 0xF24D //  capital m with dot below
            0x1E43 -> 0xF26D //  small m with dot below
            0x1E44 -> 0xE74E //  capital n with dot above
            0x1E45 -> 0xE76E //  small n with dot above
            0x1E46 -> 0xF24E //  capital n with dot below
            0x1E47 -> 0xF26E //  small n with dot below
            0x1E54 -> 0xE250 //  capital p with acute
            0x1E55 -> 0xE270 //  small p with acute
            0x1E56 -> 0xE750 //  capital p with dot above
            0x1E57 -> 0xE770 //  small p with dot above
            0x1E58 -> 0xE752 //  capital r with dot above
            0x1E59 -> 0xE772 //  small r with dot above
            0x1E5A -> 0xF252 //  capital r with dot below
            0x1E5B -> 0xF272 //  small r with dot below
            0x1E60 -> 0xE753 //  capital s with dot above
            0x1E61 -> 0xE773 //  small s with dot above
            0x1E62 -> 0xF253 //  capital s with dot below
            0x1E63 -> 0xF273 //  small s with dot below
            0x1E6A -> 0xE754 //  capital t with dot above
            0x1E6B -> 0xE774 //  small t with dot above
            0x1E6C -> 0xF254 //  capital t with dot below
            0x1E6D -> 0xF274 //  small t with dot below
            0x1E72 -> 0xF355 //  capital u with diaeresis below
            0x1E73 -> 0xF375 //  small u with diaeresis below
            0x1E7C -> 0xE456 //  capital v with tilde
            0x1E7D -> 0xE476 //  small v with tilde
            0x1E7E -> 0xF256 //  capital v with dot below
            0x1E7F -> 0xF276 //  small v with dot below
            0x1E80 -> 0xE157 //  capital w with grave
            0x1E81 -> 0xE177 //  small w with grave
            0x1E82 -> 0xE257 //  capital w with acute
            0x1E83 -> 0xE277 //  small w with acute
            0x1E84 -> 0xE857 //  capital w with diaeresis
            0x1E85 -> 0xE877 //  small w with diaeresis
            0x1E86 -> 0xE757 //  capital w with dot above
            0x1E87 -> 0xE777 //  small w with dot above
            0x1E88 -> 0xF257 //  capital w with dot below
            0x1E89 -> 0xF277 //  small w with dot below
            0x1E8A -> 0xE758 //  capital x with dot above
            0x1E8B -> 0xE778 //  small x with dot above
            0x1E8C -> 0xE858 //  capital x with diaeresis
            0x1E8D -> 0xE878 //  small x with diaeresis
            0x1E8E -> 0xE759 //  capital y with dot above
            0x1E8F -> 0xE779 //  small y with dot above
            0x1E90 -> 0xE35A //  capital z with circumflex
            0x1E91 -> 0xE37A //  small z with circumflex
            0x1E92 -> 0xF25A //  capital z with dot below
            0x1E93 -> 0xF27A //  small z with dot below
            0x1E97 -> 0xE874 //  small t with diaeresis
            0x1E98 -> 0xEA77 //  small w with ring above
            0x1E99 -> 0xEA79 //  small y with ring above
            0x1EA0 -> 0xF241 //  capital a with dot below
            0x1EA1 -> 0xF261 //  small a with dot below
            0x1EA2 -> 0xE041 //  capital a with hook above
            0x1EA3 -> 0xE061 //  small a with hook above
            0x1EB8 -> 0xF245 //  capital e with dot below
            0x1EB9 -> 0xF265 //  small e with dot below
            0x1EBA -> 0xE045 //  capital e with hook above
            0x1EBB -> 0xE065 //  small e with hook above
            0x1EBC -> 0xE445 //  capital e with tilde
            0x1EBD -> 0xE465 //  small e with tilde
            0x1EC8 -> 0xE049 //  capital i with hook above
            0x1EC9 -> 0xE069 //  small i with hook above
            0x1ECA -> 0xF249 //  capital i with dot below
            0x1ECB -> 0xF269 //  small i with dot below
            0x1ECC -> 0xF24F //  capital o with dot below
            0x1ECD -> 0xF26F //  small o with dot below
            0x1ECE -> 0xE04F //  capital o with hook above
            0x1ECF -> 0xE06F //  small o with hook above
            0x1EE4 -> 0xF255 //  capital u with dot below
            0x1EE5 -> 0xF275 //  small u with dot below
            0x1EE6 -> 0xE055 //  capital u with hook above
            0x1EE7 -> 0xE075 //  small u with hook above
            0x1EF2 -> 0xE159 //  capital y with grave
            0x1EF3 -> 0xE179 //  small y with grave
            0x1EF4 -> 0xF259 //  capital y with dot below
            0x1EF5 -> 0xF279 //  small y with dot below
            0x1EF6 -> 0xE059 //  capital y with hook above
            0x1EF7 -> 0xE079 //  small y with hook above
            0x1EF8 -> 0xE459 //  capital y with tilde
            0x1EF9 -> 0xE479 //  small y with tilde
            0x200C -> 0x8E //  zero width non-joiner
            0x200D -> 0x8D //  zero width joiner
            0x2017 -> 0xF520 //  space with double line below
            0x2113 -> 0xC1 //  script small l
            0x2117 -> 0xC2 //  sound recording copyright
            0x266D -> 0xA9 //  music flat sign
            0x266F -> 0xC4 //  music sharp sign
            0xFE20 -> 0xEB //  ligature left half
            0xFE21 -> 0xEC //  ligature right half
            0xFE22 -> 0xFA //  double tilde left half
            0xFE23 -> 0xFB //  double tilde right half
            0x0098 -> 0x88 // non-sort begin
            0x009C -> 0x89 // non-sort end
            0x0027 -> 0x92 // apostrophe
            0x201C -> 0x93 // left double quotation mark (not in the standard)
            0x201D -> 0x94 // right double quotation mark (not in the standard)
            0x20AC -> 0xC8 //  euro sign
            0x0308 -> 0xE8 //  umlaut, diaeresis
            0x0338 -> 0xFC20 // space with diagonal stroke
            0x00A0 -> 0xD959 // no-break space
            0x00A2 -> 0xD921 // cent sign
            0x00A4 -> 0xD925 // currency sign
            0x00A5 -> 0xD922 // yen sign
            0x00A6 -> 0xD945 // broken vertical bar
            0x00A7 -> 0xD947 // section sign
            0x00AA -> 0xD941 // feminine ordinal indicator
            0x00AB -> 0xD943 // left-pointing double angle quotation mark
            0x00AC -> 0xD968 // not sign
            0x00AD -> 0xD977 // soft hyphen
            0x00B2 -> 0xD932 // superscript two
            0x00B3 -> 0xD933 // superscript three
            0x00B5 -> 0xD95A // micro sign
            0x00B6 -> 0xD946 // pilcrow/paragraph sign
            0x00B9 -> 0xD931 // superscript one
            0x00BA -> 0xD942 // masculine ordinal indicator
            0x00BB -> 0xD944 // right-pointing double angle quotation mark
            0x00BC -> 0xD962 // vulgar fraction one quarter
            0x00BD -> 0xD961 // vulgar fraction one half
            0x00BE -> 0xD963 // vulgar fraction three quarters
            0x00D7 -> 0xD96A // multiplication sign
            0x00F7 -> 0xD96D // division sign
            0x0192 -> 0xD924 // florin currency sign
            0x023A -> 0xFC41 // capital A with diagonal stroke
            0x023B -> 0xFC43 // capital C with diagonal stroke
            0x023C -> 0xFC63 // small c with diagonal stroke
            0x023E -> 0xFC54 // capital T with diagonal stroke
            0x0246 -> 0xFC45 // capital E with diagonal stroke
            0x0247 -> 0xFC65 // small e with diagonal stroke
            0x0391 -> 0xD841 // Greek capital letter alpha
            0x0392 -> 0xD842 // Greek capital letter beta
            0x0393 -> 0xD847 // Greek capital letter gamma
            0x0394 -> 0xD844 // Greek capital letter delta
            0x0395 -> 0xD845 // Greek capital letter epsilon
            0x0396 -> 0xD85A // Greek capital letter zeta
            0x0397 -> 0xD857 // Greek capital letter eta
            0x0398 -> 0xD856 // Greek capital letter theta
            0x0399 -> 0xD849 // Greek capital letter iota
            0x039A -> 0xD84B // Greek capital letter kappa
            0x039B -> 0xD84C // Greek capital letter lamda
            0x039C -> 0xD84D // Greek capital letter mu
            0x039D -> 0xD84E // Greek capital letter nu
            0x039E -> 0xD858 // Greek capital letter xi
            0x039F -> 0xD84F // Greek capital letter omicron
            0x03A0 -> 0xD850 // Greek capital letter pi
            0x03A1 -> 0xD852 // Greek capital letter rho
            0x03A3 -> 0xD853 // Greek capital letter sigma
            0x03A4 -> 0xD854 // Greek capital letter tau
            0x03A5 -> 0xD843 // Greek capital letter upsilon
            0x03A6 -> 0xD846 // Greek capital letter phi
            0x03A7 -> 0xD851 // Greek capital letter chi
            0x03A8 -> 0xD848 // Greek capital letter psi
            0x03A9 -> 0xD855 // Greek capital letter omega
            0x03B1 -> 0xD861 // Greek small letter alpha
            0x03B2 -> 0xD862 // Greek small letter beta
            0x03B3 -> 0xD867 // Greek small letter gamma
            0x03B4 -> 0xD864 // Greek small letter delta
            0x03B5 -> 0xD865 // Greek small letter epsilon
            0x03B6 -> 0xD87A // Greek small letter zeta
            0x03B7 -> 0xD877 // Greek small letter eta
            0x03B8 -> 0xD876 // Greek small letter theta
            0x03B9 -> 0xD869 // Greek small letter iota
            0x03BA -> 0xD86B // Greek small letter kappa
            0x03BB -> 0xD86C // Greek small letter lamda
            0x03BC -> 0xD86D // Greek small letter mu
            0x03BD -> 0xD86E // Greek small letter nu
            0x03BE -> 0xD878 // Greek small letter xi
            0x03BF -> 0xD86F // Greek small letter omicron
            0x03C0 -> 0xD870 // Greek small letter pi
            0x03C1 -> 0xD872 // Greek small letter rho
            0x03C2 -> 0xD824 // Greek small letter final sigma
            0x03C3 -> 0xD873 // Greek small letter sigma
            0x03C4 -> 0xD874 // Greek small letter tau
            0x03C5 -> 0xD863 // Greek small letter upsilon
            0x03C6 -> 0xD866 // Greek small letter phi
            0x03C7 -> 0xD871 // Greek small letter chi
            0x03C8 -> 0xD868 // Greek small letter psi
            0x03C9 -> 0xD875 // Greek small letter omega
            0x0E3F -> 0xD920 // Thai currency symbol baht
            0x2013 -> 0xD958 // en dash
            0x2014 -> 0xD976 // em dash
            0x2018 -> 0xD974 // left single quotation mark
            0x2019 -> 0xD975 // right single quotation mark
            0x201A -> 0xD955 // single low-9 quotation mark
            0x201E -> 0xD956 // double low-9 quotation mark
            0x2020 -> 0xD949 // dagger
            0x2021 -> 0xD94A // double dagger
            0x2022 -> 0xD957 // bullet
            0x2026 -> 0xD952 // horizontal ellipsis
            0x2030 -> 0xD951 // per mille sign
            0x2039 -> 0xD94F // left-pointing single angle quotation mark
            0x203A -> 0xD950 // right-pointing single angle quotation mark
            0x2044 -> 0xD973 // fraction/division slash
            0x20A0 -> 0xD927 // Euro-currency sign
            0x20A1 -> 0xD928 // colon sign
            0x20A2 -> 0xD929 // cruzeiro sign
            0x20A3 -> 0xD92A // French franc sign
            0x20A4 -> 0xD926 // lira sign
            0x20A5 -> 0xD92B // mill sign
            0x20A6 -> 0xD92C // naira sign
            0x20A7 -> 0xD923 // peseta sign
            0x20A8 -> 0xD92D // rupee sign
            0x20A9 -> 0xD92E // won sign
            0x20AA -> 0xD92F // new sheqel sign
            0x20AB -> 0xD97C // dong sign
            0x20AD -> 0xD979 // kip sign
            0x20AE -> 0xD97A // tugrik sign
            0x20AF -> 0xD97B // drachma sign
            0x2122 -> 0xD94C // trade mark sign
            0x2205 -> 0xD965 // empty set
            0x2208 -> 0xD966 // element of
            0x221A -> 0xD972 // square root
            0x221E -> 0xD964 // infinity
            0x2229 -> 0xD967 // intersection
            0x2248 -> 0xD96E // almost equal to
            0x2261 -> 0xD969 // identical to
            0x2264 -> 0xD96C // less-than or equal to
            0x2265 -> 0xD96B // greater-than or equal to
            0x22C5 -> 0xD96F // dot operator
            0x2310 -> 0xD948 // reversed not sign
            0x2320 -> 0xD970 // top half integral
            0x2321 -> 0xD971 // bottom half integral
            0x2500 -> 0xD761 // box drawings light horizontal
            0x2502 -> 0xD762 // box drawings light vertical
            0x250C -> 0xD766 // box drawings light down and right
            0x2510 -> 0xD763 // box drawings light down and left
            0x2514 -> 0xD765 // box drawings light up and right
            0x2518 -> 0xD764 // box drawings light up and left
            0x251C -> 0xD769 // box drawings light vertical and right
            0x2524 -> 0xD767 // box drawings light vertical and left
            0x252C -> 0xD76A // box drawings light down and horizontal
            0x2534 -> 0xD768 // box drawings light up and horizontal
            0x253C -> 0xD76B // box drawings light vertical and horizontal
            0x2550 -> 0xD741 // box drawings double horizontal
            0x2551 -> 0xD742 // box drawings double vertical
            0x2552 -> 0xD76F // box drawings light single down and double right
            0x2553 -> 0xD773 // box drawings light double down and single right
            0x2554 -> 0xD746 // box drawings double down and right
            0x2555 -> 0xD76C // box drawings light single down and double left
            0x2556 -> 0xD770 // box drawings light double down and single left
            0x2557 -> 0xD743 // box drawings double down and left
            0x2558 -> 0xD76E // box drawings light single up and double right
            0x2559 -> 0xD772 // box drawings light double up and single right
            0x255A -> 0xD745 // box drawings double up and right
            0x255B -> 0xD76D // box drawings light single up and double left
            0x255C -> 0xD771 // box drawings light double up and single left
            0x255D -> 0xD744 // box drawings double up and left
            0x255E -> 0xD752 // box drawings single vertical and double right
            0x255F -> 0xD74E // box drawings double vertical and single right
            0x2560 -> 0xD749 // box drawings double vertical and right
            0x2561 -> 0xD750 // box drawings single vertical and double left
            0x2562 -> 0xD74C // box drawings double vertical and single left
            0x2563 -> 0xD747 // box drawings double vertical and left
            0x2564 -> 0xD74F // box drawings single down and double horizontal
            0x2565 -> 0xD753 // box drawings double down and single horizontal
            0x2566 -> 0xD74A // box drawings double down and horizontal
            0x2567 -> 0xD74D // box drawings single up and double horizontal
            0x2568 -> 0xD751 // box drawings double up and single horizontal
            0x2569 -> 0xD748 // box drawings double up and horizontal
            0x256A -> 0xD754 // box drawings single vertical and double horizontal
            0x256B -> 0xD774 // box drawings light double vertical and single horizontal
            0x256C -> 0xD74B // box drawings double vertical and horizontal
            0x2580 -> 0xD776 // upper half block
            0x2584 -> 0xD778 // lower half block
            0x2588 -> 0xD758 // full block
            0x258C -> 0xD775 // left half block
            0x2590 -> 0xD777 // right half block
            0x2591 -> 0xD755 // light shade - 25%
            0x2592 -> 0xD756 // medium shade - 50%
            0x2593 -> 0xD757 // dark shade - 75%
            0x25AA -> 0xD779 // black small square
            0x25AE -> 0xBF // black vertical rectangle
            0x25AF -> 0xBE // white vertical rectangle
            0x2C65 -> 0xFC61 // small a with diagonal stroke
            0x2C66 -> 0xFC74 // small t with diagonal stroke
            0xA742 -> 0xFC4B // capital K with diagonal stroke
            0xA743 -> 0xFC6B // small k with diagonal stroke
            0xA758 -> 0xFC51 // capital Q with diagonal stroke
            0xA759 -> 0xFC71 // small q with diagonal stroke
            0xA75E -> 0xFC56 // capital V with diagonal stroke
            0xA75F -> 0xFC76 // small v with diagonal stroke
            0xFB01 -> 0xD94D // Latin small ligature fi
            0xFB02 -> 0xD94E // Latin small ligature fl

            else -> 0xC5 // if no match, use inverted '?'
        }      /* Note: this conversion table is currently the exact inverse of that used in
      * ANSELInputStreamReader. Ideally it should also provide fallback conversion for
      * UNICODE characters that are never generated by ANSELInputStreamReader, e.g.
      * free-standing accents. For future work.
      */
    }
}
