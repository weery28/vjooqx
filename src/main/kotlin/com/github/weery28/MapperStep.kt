package com.github.weery28

import com.github.weery28.exceptions.FlatMappingException
import com.github.weery28.json.JsonParser
import io.reactivex.Single
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet
import org.jooq.Field

class MapperStep(
        private val jsonParser: JsonParser,
        private val resultSingle: Single<ResultSet>) {


    fun <T> to(pClass: Class<T>): Single<T?> {

        return resultSingle.flatMap {
            val jsonResult = it.rows.firstOrNull()?.encode()
            if (jsonResult != null) {
                Single.just((jsonParser.encode(jsonResult, pClass)))
            } else {
                Single.just(null)
            }
        }
    }

    fun <T> toListOf(pClass: Class<T>): Single<List<T>> {
        return resultSingle.map {
            it.rows.map {
                jsonParser.encode(it.encode(), pClass)
            }
        }
    }

    fun <T> toFlatFields(pClass: Class<T>, vararg fields: Field<*>): Single<T> {

        return resultSingle.map {

            val fieldNames = fields.map { it.name }
            val resultJson = JsonObject()
            it.rows.forEach {
                it.forEach { jsonField ->
                    val key = jsonField.key
                    if (fieldNames.contains(key)) {
                        val value = resultJson.getValue(jsonField.key)
                        if (value != null && value != jsonField.value) {
                            throw FlatMappingException("%s field contained different values".format(key))
                        } else {
                            resultJson.put(key, jsonField.value)
                        }
                    } else {
                        val value = resultJson.getValue(jsonField.key)
                        if (value == null) {
                            resultJson.put(key, JsonArray(mutableListOf(jsonField.value)))
                        } else {
                            val array = resultJson.getJsonArray(key)
                            if (jsonField.value == null ){
                                array.addNull()
                            }
                            else{
                                array.add(jsonField.value)
                            }
                        }
                    }
                }
            }
            return@map jsonParser.encode(resultJson.encode(), pClass)
        }
    }
}


