package com.github.weery28.json

interface JsonParser {

    fun <T> encode(json: String, pClass: Class<T>): T
}