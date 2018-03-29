package com.github.weery28.transactions

import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.reactivex.ext.sql.SQLConnection


class TransactionStepImpl<T>(
		private val result: Single<T>,
		val transactionContext: TransactionContext
) : TransactionStep<T> {

	override fun commit(): Single<T> {
		return transactionContext.getConnection().rxCommit()
				.toSingle { true }
				.flatMap { result }
				.doAfterTerminate { transactionContext.getConnection().close() }


	}

	override fun <E> then(action: (T, TransactionContext) -> Execution<E>): TransactionStep<E> {
		return TransactionStepImpl(result.flatMap {
			action(it, transactionContext).result()
		}.doOnError {
					transactionContext.getConnection().rxRollback().andThen {
						transactionContext.getConnection().close() }.subscribe()
				}, transactionContext)
	}


	override fun thenCommit(action: (T) -> T): Single<T> {
		return result.flatMap {
			action(it)
			transactionContext.getConnection()
					.rxCommit()
					.andThen { transactionContext.getConnection().close() }
					.toSingle { action(it) }
		}


	}

	override fun rollBackIf(action: (T) -> Boolean): Completable {
		return result.flatMapCompletable {
			if (action(it)) {
				transactionContext.getConnection().rxRollback().andThen {
					transactionContext.getConnection().close()
				}
			} else {
				transactionContext.getConnection().rxCommit().andThen {
					transactionContext.getConnection().close()
				}
			}
		}


	}

	override fun rollBackOnError(): Single<T> {
		return result.doOnError {
			transactionContext.getConnection().rxRollback()
					.andThen { transactionContext.getConnection().close() }.subscribe()
		}.doOnSuccess {
					transactionContext.getConnection().close()
				}

	}

	override fun result(): Single<T> {
		return result
	}
}