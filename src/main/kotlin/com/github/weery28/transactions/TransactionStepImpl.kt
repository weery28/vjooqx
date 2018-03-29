package com.github.weery28.transactions

import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.reactivex.ext.sql.SQLConnection


class TransactionStepImpl<T>(
		private val result: Single<T>,
		private val connectionSingle: Single<SQLConnection>,
		private val transactionContext: TransactionContext
) : TransactionStep<T> {

	override fun commit(): Single<T> {
		return connectionSingle.flatMap {
			it.rxCommit()
					.toSingle { 1 }
					.flatMap { result }
					.doAfterTerminate { it.close() }
		}

	}

	override fun <E> then(action: (T, TransactionContext) -> Execution<E>): TransactionStep<E> {
		return TransactionStepImpl(
				connectionSingle
						.flatMap { connection ->
							result.flatMap {
								action(it, transactionContext).result()
							}.doOnError {
										connection.rxRollback().andThen { connection.close() }.subscribe()
									}
						}, connectionSingle, transactionContext)
	}

	override fun thenCommit(action: (T) -> T): Single<T> {
		return connectionSingle.flatMap { connection ->
			result.flatMap {
				action(it)
				connection
						.rxCommit()
						.andThen { connection.close() }
						.toSingle { action(it) }
			}
		}

	}

	override fun rollBackIf(action: (T) -> Boolean): Completable {
		return connectionSingle.flatMapCompletable { connection ->
			result.flatMapCompletable {
				if (action(it)) {
					connection.rxRollback().andThen { connection.close() }
				} else {
					connection.rxCommit()
				}
			}
		}


	}

	override fun rollBackOnError(): Single<T> {
		return connectionSingle.flatMap { connection ->
			result.doOnError { connection.rxRollback().andThen { connection.close() }.subscribe() }
		}
	}

	override fun result(): Single<T> {
		return result
	}
}