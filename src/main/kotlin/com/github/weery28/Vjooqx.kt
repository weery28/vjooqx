package com.github.weery28

import com.google.gson.GsonBuilder
import io.reactivex.Single
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import io.vertx.reactivex.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.conf.ParamType

class Vjooqx(
        private val delegate: AsyncSQLClient,
        private val dslContext: DSLContext,
        private val gsonBuilder: GsonBuilder) {


    fun fetch(query: (DSLContext) -> Query): MapperStep {
        return MapperStep(gsonBuilder, getConnection()
                .flatMap { connection ->
                    return@flatMap connection
                            .rxQuery(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED))
                            .doAfterTerminate {
                                connection.close()
                            }
                })

    }

    private fun getConnection(): Single<SQLConnection> {
        return delegate.rxGetConnection()
    }

}

