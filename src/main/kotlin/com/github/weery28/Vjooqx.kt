package com.github.weery28

import com.github.weery28.json.JsonParser
import io.reactivex.Single
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import io.vertx.reactivex.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.conf.ParamType

class Vjooqx(
        private val delegate: AsyncSQLClient,
        private val dslContext: DSLContext,
        private val jsonParser: JsonParser) {


    fun fetch(query: (DSLContext) -> Query): MapperStep {
        return MapperStepImpl(jsonParser, getConnection()
                .flatMap { connection ->
                    return@flatMap connection
                            .rxQuery(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED))
                            .doAfterTerminate {
                                connection.close()
                            }
                })

    }

    fun execute(query: (DSLContext) -> Query): Single<Int> {
        return getConnection()
                .flatMap { sqlConnection ->
                    sqlConnection
                            .rxUpdate(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED))
                            .map {
                                it.updated
                            }
                }
    }

    private fun getConnection(): Single<SQLConnection> {
        return delegate.rxGetConnection()
    }

}

