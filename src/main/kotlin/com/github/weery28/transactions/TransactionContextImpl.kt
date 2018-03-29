package com.github.weery28.transactions

import com.github.weery28.LoggingInterceptor
import com.github.weery28.json.JsonParser
import io.reactivex.Single
import io.vertx.ext.sql.ResultSet
import io.vertx.reactivex.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.conf.ParamType


class TransactionContextImpl(
		private val connectionProvider: Single<SQLConnection>,
		private val jsonParser: JsonParser,
		private val loggingInterceptor: LoggingInterceptor?,
		private val dslContext: DSLContext
) : TransactionContext {

	override fun fetch(query: (DSLContext) -> Query): MapperStepTransaction {

		return MapperStepTransactionImpl(jsonParser, connectionProvider.flatMap { connection ->
			connection.rxSetAutoCommit(false)
					.toSingle { true }
					.flatMap {
						connection.rxQuery(
								query(dslContext).getSQL(ParamType.NAMED_OR_INLINED).apply {
									loggingInterceptor?.log("Database <----- : " + this)
								}
						).map {
							Pair(it, connection)
						}
					}
		}, loggingInterceptor, this)
	}

	override fun execute(query: (DSLContext) -> Query): TransactionStep<Int> {
		return TransactionStepImpl(
				connectionProvider.flatMap { connection ->
					connection.rxSetAutoCommit(false)
							.toSingle { true }
							.flatMap {
								connection
										.rxUpdate(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED).apply {
											loggingInterceptor?.log("Database <----- : " + this)
										})
										.map {
											it.updated
										}
							}
				}, connectionProvider, this
		)
	}

	private fun setAutoCommitFalse(connection: io.vertx.reactivex.ext.sql.SQLConnection, isTransaction: Boolean): Single<io.vertx.reactivex.ext.sql.SQLConnection> {
		return if (isTransaction) {
			connection.rxSetAutoCommit(false).toSingle { connection }
		} else {
			return Single.just(connection)
		}
	}

	private fun setAutoClose(single: Single<ResultSet>, connection: io.vertx.reactivex.ext.sql.SQLConnection, isTransaction: Boolean)
			: Single<ResultSet> {
		return if (!isTransaction) {
			single.doAfterTerminate {
				connection.close()
			}
		} else {
			single
		}
	}
}