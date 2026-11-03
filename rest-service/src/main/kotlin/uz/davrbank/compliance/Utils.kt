package uz.davrbank.compliance

import mu.KotlinLogging

val logger = KotlinLogging.logger { }


object CharUtils {
    val ES_DROP_CHARS = setOf(
        '\'',
        '-',
        '\"',
        '.',
        ',',
        ' ',
        //'&',
        '(',
        ')'
    )
}
