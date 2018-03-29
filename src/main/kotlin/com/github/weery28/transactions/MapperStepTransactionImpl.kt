package com.github.weery28.transactions

import com.github.weery28.LoggingInterceptor
import com.github.weery28.MapperStepImpl
import com.github.weery28.json.JsonParser
import io.reactivex.Single
import io.vertx.ext.sql.ResultSet
import io.vertx.reactivex.ext.sql.SQLConnection


class MapperStepTransactionImpl(
		private val jsonParser: JsonParser,
		private val resultSingleConnection: Single<Pair<ResultSet, SQLConnection>>,
		private val loggingInterceptor: LoggingInterceptor?,
		private val transactionContext: TransactionContext
) : MapperStepTransaction {

	override fun <T> to(pClass: Class<T>): TransactionStep<T> {
		return TransactionStepImpl(
				MapperStepImpl(
						jsonParser, resultSingleConnection.map { it.first }, loggingInterceptor
				).to(pClass), resultSingleConnection.map { it.second }, transactionContext
		)
	}

	override fun <T> toListOf(pClass: Class<T>): TransactionStep<List<T>> {
		return TransactionStepImpl(
				MapperStepImpl(
						jsonParser, resultSingleConnection.map { it.first }, loggingInterceptor
				).toListOf(pClass), resultSingleConnection.map { it.second }, transactionContext
		)
	}

	override fun <T> toTree(pClass: Class<T>, listAliases: List<String>): TransactionStep<T> {
		return TransactionStepImpl(
				MapperStepImpl(
						jsonParser, resultSingleConnection.map { it.first }, loggingInterceptor
				).toTree(pClass, listAliases), resultSingleConnection.map { it.second }, transactionContext
		)
	}

	override fun <T> toTreeList(pClass: Class<T>, listAliases: List<String>): TransactionStep<List<T>> {
		return TransactionStepImpl(
				MapperStepImpl(
						jsonParser, resultSingleConnection.map { it.first }, loggingInterceptor
				).toTreeList(pClass, listAliases), resultSingleConnection.map { it.second }, transactionContext
		)
	}
}