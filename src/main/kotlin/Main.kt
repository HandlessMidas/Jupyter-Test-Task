import kotlinx.html.*
import kotlinx.html.dom.*
import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.max
import javax.xml.transform.TransformerFactory

import javax.xml.transform.stream.StreamResult

import java.io.FileWriter
import javax.xml.transform.Transformer

import javax.xml.transform.dom.DOMSource




fun lcs(s1: MutableList<String>, s2: MutableList<String>): MutableList<MutableList<Boolean>> {
    val lcsList: MutableList<MutableList<Int>> = MutableList(s1.size) {MutableList(s2.size) {0} }
    for (i in 1..s1.size) {
        for (j in 1..s2.size) {
            if (s1[i] == s2[j]) {
                lcsList[i][j] = lcsList[i-1][j-1] + 1
            } else {
                lcsList[i][j] = max(lcsList[i-1][j], lcsList[i][j-1])
            }
        }
    }
    var i = s1.size
    var j = s2.size
    var lcsId = lcsList[i][j]
    val result: MutableList<MutableList<Boolean>> = mutableListOf(MutableList(i) {false}, MutableList(j) {false})
    while (i > 0 && j > 0) {
        when {
            s1[i-1] == s2[j-1] -> {
                result[0][i-1] = true;
                result[1][j-1] = true;
                i--;
                j--;
                lcsId--;
            }
            lcsList[i-1][j] > lcsList[i][j-1] -> {
                i--;
            }
            else -> {
                j--;
            }
        }
    }
    return result
}

fun constructDiffLines(s1: MutableList<String>, s2: MutableList<String>, lcs: MutableList<MutableList<Boolean>>): MutableList<Pair<String, Int>> {
    val maxLineLen = s1.map {it.length}.maxOrNull()
    val diffLines: MutableList<Pair<String, Int>> = MutableList(0) {Pair("", 0)}
    var j = 0
    var i = 0
    while (i < s1.size && j < s2.size) {
        if (lcs[0][i]) {
            if (lcs[1][j]) {
                diffLines.add(Pair(s1[i] + "".repeat(maxLineLen!! - s1[i].length + 10) + s2[j], 0))
                i++
                j++
            } else {
                diffLines.add(Pair("".repeat(maxLineLen!! + 10) + s2[j], 1))
                j++
            }
        } else {
            if (lcs[1][j]) {
                diffLines.add(Pair(s1[i], 2))
                i++
            } else {
                diffLines.add(Pair(s1[i] + "".repeat(maxLineLen!! - s1[i].length + 10) + s2[j], 3))
                i++
                j++
            }
        }
    }
    return diffLines
}

fun constructHTML(diffLines:MutableList<Pair<String, Int>>): Document? {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    document.create.html {
        body {
            for (line in diffLines) {
                when (line.second) {
                    0 -> h2 {
                        +line.first
                    }
                    1 -> h2 {
                        style = "background-color:green"
                        +line.first
                    }
                    2 -> h2 {
                        style = "background-color:red"
                        +line.first
                    }
                    else -> h2 {
                        style = "background-color:blue"
                        +line.first
                    }
                }
            }
        }
    }
    return document
}

fun main(args: Array<String>) {
    val s1 = File(args[0]).readLines().toMutableList()
    val s2 = File(args[1]).readLines().toMutableList()
    val lcs = lcs(s1, s2)
    val diffLines = constructDiffLines(s1, s2, lcs)
    val doc = constructHTML(diffLines)
    val source = DOMSource(doc)
    val writer = FileWriter(File("/tmp/diff.html"))
    val result = StreamResult(writer)
    val transformerFactory = TransformerFactory.newInstance()
    val transformer: Transformer = transformerFactory.newTransformer()
    transformer.transform(source, result)
}