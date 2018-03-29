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

	@Volatile
	private var connection: SQLConnection? = null

	override fun getConnection(): SQLConnection {
		return connection!!
	}

	override fun getLoggingInterceptor(): LoggingInterceptor? {
		return loggingInterceptor
	}

	override fun getJsonParser(): JsonParser {
		return jsonParser
	}

	override fun fetch(query: (DSLContext) -> Query): MapperStepTransaction {

		return MapperStepTransactionImpl(fetchWithConnection(query), this)
	}

	override fun execute(query: (DSLContext) -> Query): TransactionStep<Int> {
		return TransactionStepImpl(executeWithConnection(query), this)
	}


	private fun fetchWithConnection(query: (DSLContext) -> Query): Single<ResultSet> {

		return if (connection != null) {
			connection!!.rxQuery(
					query(dslContext).getSQL(ParamType.NAMED_OR_INLINED).apply {
						loggingInterceptor?.log("Database <----- : " + this)
					}
			)
		} else {
			connectionProvider.flatMap { connection ->
				this.connection = connection
				connection.rxSetAutoCommit(false)
						.toSingle { true }
						.flatMap {
							connection.rxQuery(
									query(dslContext).getSQL(ParamType.NAMED_OR_INLINED).apply {
										loggingInterceptor?.log("Database <----- : " + this)
									}
							)
						}
			}
		}
	}

	private fun executeWithConnection(query: (DSLContext) -> Query): Single<Int> {

		return if (connection != null) {
			connection!!
					.rxUpdate(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED).apply {
						loggingInterceptor?.log("Database <----- : " + this)
					})
					.map {
						it.updated
					}
		} else {
			connectionProvider.flatMap { connection ->
				this.connection = connection
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
			}
		}

	}

}