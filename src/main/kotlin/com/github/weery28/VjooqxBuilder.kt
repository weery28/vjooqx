package com.github.weery28

import com.github.weery28.json.JsonParserFactory
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import io.vertx.reactivex.ext.jdbc.JDBCClient
import org.jooq.DSLContext


class VjooqxBuilder : Builder {

    private lateinit var dslContext: DSLContext

    private lateinit var asyncSQLClient: JDBCClient

    private lateinit var jsonParserFactory: JsonParserFactory

    private var loggingInterceptor: LoggingInterceptor? = null

    override fun setupDelegate(delegate: JDBCClient): VjooqxBuilder {
        asyncSQLClient = delegate
        return this
    }

    override fun dsl(dslContext: DSLContext): VjooqxBuilder {
        this.dslContext = dslContext
        return this
    }

    override fun create(): Vjooqx {
        return Vjooqx(asyncSQLClient, dslContext, jsonParserFactory.buildJsonParser(), loggingInterceptor)
    }

    override fun jsonFactory(jsonParserFactory: JsonParserFactory): VjooqxBuilder {
        this.jsonParserFactory = jsonParserFactory
        return this
    }

    override fun addLoggingInterceptor(loggingInterceptor: LoggingInterceptor): Builder {
        this.loggingInterceptor = loggingInterceptor
        return this
    }
}
