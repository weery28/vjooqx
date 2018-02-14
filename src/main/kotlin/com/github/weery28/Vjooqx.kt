package com.github.weery28

import com.google.gson.GsonBuilder
import io.reactivex.Single
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import io.vertx.reactivex.ext.asyncsql.PostgreSQLClient
import io.vertx.reactivex.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.conf.ParamType

class Vjooqx(
        val vertx: Vertx
) {
    @Volatile
    private var sqlClient: AsyncSQLClient? = null

    @Volatile
    private var dslContext: DSLContext? = null

    val gsonBuilder: GsonBuilder = GsonBuilder()

    fun initializePostgres(config: JsonObject): Vjooqx {

        if (sqlClient != null) {
            throw IllegalStateException("PostgresSQL client already initialized")
        } else {
            sqlClient = PostgreSQLClient.createNonShared(vertx, config)
            return this
        }
    }

    fun fetch(query: (DSLContext) -> Query): MapperStep {
        return MapperStep(
                getConnection()
                        .flatMap {
                            it.rxCall(query(dslContext!!).getSQL(ParamType.NAMED_OR_INLINED))
                        }, gsonBuilder
        )
    }


    private fun getConnection(): Single<SQLConnection> {
        return sqlClient!!.rxGetConnection()
    }


}

class MapperStep(val result: Single<ResultSet>, val gsonBuilder: GsonBuilder) {

    fun <T> to(pClass: Class<T>): Single<T?> {
        return result.map {
            val jsonResult = it.rows.firstOrNull()?.encode()
            jsonResult?.let {
                return@map gsonBuilder.create().fromJson(it, pClass)
            }
            return@map null
        }
    }

    fun <T> toListOf(pClass: Class<T>): Single<List<T>> {
        return result.map {
            it.rows.map {
                gsonBuilder.create().fromJson(it.encode(), pClass)
            }
        }
    }
}
