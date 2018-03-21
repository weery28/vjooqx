package com.github.weery28

import com.github.weery28.json.JsonParserFactory
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import org.jooq.DSLContext

interface Builder {

	fun setupDelegate(delegate: AsyncSQLClient): Builder

	fun dsl(dslContext: DSLContext): Builder

	fun create(): Vjooqx

	fun jsonFactory(jsonParserFactory: JsonParserFactory): Builder

	fun addLoggingInterceptor(loggingInterceptor: LoggingInterceptor): Builder
}
