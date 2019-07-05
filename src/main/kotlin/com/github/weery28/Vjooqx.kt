package com.github.weery28

import com.github.weery28.json.JsonParser
import com.github.weery28.transactions.TransactionContext
import com.github.weery28.transactions.TransactionContextImpl
import io.reactivex.Single
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import io.vertx.reactivex.ext.jdbc.JDBCClient
import io.vertx.reactivex.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.conf.ParamType

class Vjooqx(
        private val delegate: JDBCClient,
        private val dslContext: DSLContext,
        private val jsonParser: JsonParser,
        private val loggingInterceptor: LoggingInterceptor?) {


    fun fetch(query: DSLContext.() -> Query): MapperStep {
        return MapperStepImpl(jsonParser, getConnection()
                .flatMap { connection ->
                    return@flatMap connection
                            .rxQuery(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED).apply {
                                loggingInterceptor?.log("Database <----- : " + this)
                            })
                            .doFinally {
                                connection.close()
                            }
                }, loggingInterceptor)

    }

    fun execute(query: DSLContext.() -> Query): Single<Int> {
        return getConnection()
                .flatMap { connection ->
                    connection
                            .rxUpdate(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED).apply {
                                loggingInterceptor?.log("Database <----- : " + this)
                            })
                            .map {
                                it.updated
                            }
                            .doFinally {
                                connection.close()
                            }
                }
    }

    fun transaction(): TransactionContext {
        return TransactionContextImpl(getConnection(), jsonParser, loggingInterceptor, dslContext)
    }

    private fun getConnection(): Single<SQLConnection> {
        return delegate.rxGetConnection()
    }
}

