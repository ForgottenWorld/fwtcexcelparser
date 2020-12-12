package me.muffin.fwtcexcelparser

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream

@Serializable data class Config(val Cards: Map<String, Map<String, Card>>)
@Serializable data class Card(val Series: String, val Type: String, val `Has-Shiny-Version`: Boolean, val Info: String)
val String.noWhitespace get() = replace(" ", "_")

fun main(args: Array<String>) = WorkbookFactory
    .create(FileInputStream(args[0]))
    .getSheetAt(0)
    .drop(1)
    .groupBy({ "${it.getCell(0)}".noWhitespace }) { r -> r.drop(1).take(5).map { "$it" } }
    .mapValues { (_,v) -> v.associate { it[0].noWhitespace to Card(it[1], it[2].noWhitespace, it[3].toBoolean(), it.getOrElse(4) { "" }) } }
    .let(::Config)
    .let(Yaml()::encodeToString)
    .replace(""""(.+)":""".toRegex()) { "${it.groupValues[1]}:" }
    .let(File("cards.yml")::writeText)