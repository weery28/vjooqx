package com.github.weery28.transactions

import com.github.weery28.LoggingInterceptor
import com.github.weery28.json.JsonParser
import io.vertx.reactivex.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query

interface TransactionContext {

    fun getConnection(): SQLConnection

    fun getLoggingInterceptor(): LoggingInterceptor?

    fun getJsonParser(): JsonParser

    fun fetch(query: DSLContext.() -> Query): MapperStepTransaction

    fun execute(query: DSLContext.() -> Query): TransactionStep<Int>
}