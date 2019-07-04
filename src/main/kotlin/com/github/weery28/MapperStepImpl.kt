package com.github.weery28

import com.github.weery28.exceptions.EmptyResultSet
import com.github.weery28.json.JsonParser
import io.reactivex.Single
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet

class MapperStepImpl(
        private val jsonParser: JsonParser,
        private val resultSingle: Single<ResultSet>,
        private val loggingInterceptor: LoggingInterceptor?) : MapperStep {

    private val loggedResultSingle: Single<ResultSet> = resultSingle.doOnSuccess {
        loggingInterceptor?.log("Database -----> : " + it.toJson().encode())
    }


    override fun <T> to(pClass: Class<T>): Single<T> {

        return loggedResultSingle.flatMap {
            val jsonResult = it.rows.firstOrNull()?.let {
                return@let unpackAlias(it).encode()
            }
            if (jsonResult != null) {
                Single.just((jsonParser.encode(jsonResult, pClass)))
            } else {
                Single.error(EmptyResultSet("Result is null"))
            }
        }
    }

    override fun <T> toListOf(pClass: Class<T>): Single<List<T>> {
        return loggedResultSingle.map {
            it.rows.map {
                jsonParser.encode(unpackAlias(it).encode(), pClass)
            }
        }
    }

    override fun <T> toTree(pClass: Class<T>, listAliases: List<String>): Single<T> {
        return loggedResultSingle.map {

            if (it.rows.size > 0) {
                jsonParser.encode(
                        folder(it.rows.map {
                            makeListFields(unpackAlias(it), listAliases)
                        }, listAliases).getJsonObject(0).encode(),
                        pClass)
            } else {
                throw NullPointerException()
            }
        }
    }

    override fun <T> toTreeList(pClass: Class<T>, listAliases: List<String>): Single<List<T>> {
        return loggedResultSingle.map {
            folder(it.rows.map {
                makeListFields(unpackAlias(it), listAliases)
            }, listAliases).map { jsonParser.encode((it as JsonObject).encode(), pClass) }
        }
    }

    private fun unpackAlias(jsonResult: JsonObject): JsonObject {

        val unpackingResult = JsonObject()

        jsonResult.forEach {
            val splitKeys = it.key.split(".")
            val topLevelKey = splitKeys[0]
            if (splitKeys.size != 1) {
                val currentNode = unpackingResult.getJsonObject(topLevelKey)
                val node = getNode(splitKeys.toMutableList(), it.value, currentNode?.let {
                    return@let JsonObject().put(topLevelKey, it)
                })
                unpackingResult.put(topLevelKey, node.getValue(topLevelKey))

            } else {
                unpackingResult.put(topLevelKey, it.value)
            }
        }
        return unpackingResult
    }

    private fun getNode(path: MutableList<String>, value: Any?, node: JsonObject? = null): JsonObject {

        if (path.size != 1) {
            if (node == null) {
                return JsonObject().put(path.removeAt(0), getNode(path, value))
            } else {

                val key = path.removeAt(0)
                if (node.containsKey(key)) {
                    return node
                            .put(key, getNode(path, value, node.getJsonObject(key)))
                } else {
                    return node

                            .put(key, getNode(path, value, null))
                }
            }
        } else {
            if (node == null) {
                return JsonObject().put(path.first(), value)
            } else {
                return node.put(path.first(), value)
            }

        }
    }

    private fun makeListFields(jsonObject: JsonObject,
                               listAliases: List<String>): JsonObject {

        val resultJsonObject = JsonObject()
        jsonObject.forEach {
            val value = it.value
            if (listAliases.contains(it.key)) {
                when (value) {
                    is JsonObject -> {
                        resultJsonObject.put(it.key, JsonArray(
                                listOf(makeListFields(value, listAliases))
                        ))
                    }
                    is JsonArray -> {
                        resultJsonObject.put(it.key, JsonArray(listOf(makeListFields(value, listAliases))))
                    }
                    null -> resultJsonObject.put(it.key, JsonArray())
                    else -> resultJsonObject.put(it.key, listOf(value))
                }
            } else {
                when (value) {
                    is JsonObject -> resultJsonObject.put(it.key,
                            makeListFields(value, listAliases)
                    )
                    is JsonArray -> resultJsonObject.put(it.key,
                            makeListFields(value, listAliases))
                    else -> resultJsonObject.put(it.key, value)
                }
            }

        }
        return resultJsonObject
    }

    private fun makeListFields(jsonArray: JsonArray,
                               listAliases: List<String>): JsonArray {

        val resultArray = JsonArray()
        jsonArray.forEach {
            when (it) {
                is JsonObject -> resultArray.add(makeListFields(it, listAliases))
                is JsonArray -> resultArray.add(makeListFields(it, listAliases))
                else -> resultArray.add(it)
            }
        }
        return resultArray
    }

    private fun folder(
            jsonObjects: List<JsonObject>,
            listAliases: List<String>): JsonArray {

        val jsonResult = JsonArray()
        jsonObjects.forEachIndexed { index, jsonObject ->
            if (index == 0) {
                jsonResult.add(jsonObject)
            } else {
                var stickyObject: JsonObject? = (jsonResult.firstOrNull {
                    isFolded(it as JsonObject, jsonObject, listAliases)
                } as JsonObject?)?.copy()
                if (stickyObject == null) {
                    jsonResult.add(jsonObject)
                } else {
                    jsonResult.remove(stickyObject)
                    stickyObject = stickyObject.copy()
                    val stickyFields = getStickyFields(jsonObject, listAliases)
                    stickyFields.forEach {
                        val tArray: JsonArray = stickyObject.getJsonArray(it).copy()
                        val tObject = jsonObject.getJsonArray(it).getValue(0) as JsonObject
                        tArray.add(tObject)
                        stickyObject.put(it, folder(
                                tArray.map { it as JsonObject }, listAliases
                        ))
                    }
                    jsonResult.add(stickyObject)
                }
            }
        }
        return jsonResult
    }

    private fun getStickyFields(
            jsonObject: JsonObject,
            listAliases: List<String>
    ): List<String> {
        return jsonObject.filter { listAliases.contains(it.key) }.map { it.key }
    }

    private fun isFolded(jsonObject1: JsonObject,
                         jsonObject2: JsonObject,
                         listAliases: List<String>): Boolean {
        jsonObject1.forEach {
            val isListAlias = listAliases.contains(it.key)
            val isFieldEquals = with(jsonObject2.getValue(it.key)) {
                if (this == null) {
                    return@with it.value == null
                } else {
                    return@with this.equals(it.value)
                }
            }
            if (!isListAlias && !isFieldEquals) {
                return false
            }
        }
        return true
    }
}