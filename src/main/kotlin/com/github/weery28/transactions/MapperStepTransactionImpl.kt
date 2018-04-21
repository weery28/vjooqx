package com.github.weery28.transactions

import com.github.weery28.LoggingInterceptor
import com.github.weery28.MapperStepImpl
import com.github.weery28.json.JsonParser
import io.reactivex.Single
import io.vertx.ext.sql.ResultSet
import io.vertx.reactivex.ext.sql.SQLConnection


class MapperStepTransactionImpl(
		private val resultSingle: Single<ResultSet>,
		private val transactionContext: TransactionContext
) : MapperStepTransaction {

	override fun <T> to(pClass: Class<T>): TransactionStep<T> {
		return TransactionStepImpl(MapperStepImpl(
				transactionContext.getJsonParser(),
				resultSingle,
				transactionContext.getLoggingInterceptor()
		).to(pClass), transactionContext)
	}

	override fun <T> toListOf(pClass: Class<T>): TransactionStep<List<T>> {
		return TransactionStepImpl(MapperStepImpl(
				transactionContext.getJsonParser(),
				resultSingle,
				transactionContext.getLoggingInterceptor()
		).toListOf(pClass), transactionContext)
	}

	override fun <T> toTree(pClass: Class<T>, listAliases: List<String>): TransactionStep<T> {
		return TransactionStepImpl(MapperStepImpl(
				transactionContext.getJsonParser(),
				resultSingle,
				transactionContext.getLoggingInterceptor()
		).toTree(pClass, listAliases), transactionContext)
	}

	override fun <T> toTreeList(pClass: Class<T>, listAliases: List<String>): TransactionStep<List<T>> {
		return TransactionStepImpl(MapperStepImpl(
				transactionContext.getJsonParser(),
				resultSingle,
				transactionContext.getLoggingInterceptor()
		).toTreeList(pClass, listAliases), transactionContext)
	}
}