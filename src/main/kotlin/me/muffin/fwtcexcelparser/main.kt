package me.muffin.fwtcexcelparser

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream

@Serializable data class Config(val Cards: Map<String, Map<String, Card>>)
@Serializable data class Card(val Series: String, val Type: String, @SerialName("Has-Shiny-Version") val HasShiny: Boolean, val Info: String)
fun main(args: Array<String>) = WorkbookFactory
    .create(FileInputStream(args[0]))
    .getSheetAt(0)
    .drop(1)
    .map { r -> r.take(6).map { "$it" } }
    .groupBy { it[0] }
    .mapValues { (_,v) -> v.associate { it[1] to Card(it[2], it[3], it[4].toBoolean(), it.getOrElse(5) { "" }) } }
    .let(::Config)
    .let(Yaml()::encodeToString)
    .replace(""""(.+)":""".toRegex()) { "${it.groupValues[1]}:" }
    .let(File("output.yml")::writeText)