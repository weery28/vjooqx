package com.github.weery28.transactions

import io.reactivex.Single
import org.jooq.DSLContext
import org.jooq.Query


interface TransactionContext{

	fun fetch(query: (DSLContext) -> Query): MapperStepTransaction

	fun execute(query: (DSLContext) -> Query): TransactionStep<Int>
}