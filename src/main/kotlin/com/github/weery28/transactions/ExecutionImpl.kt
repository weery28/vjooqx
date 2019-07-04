package com.github.weery28.transactions

import io.reactivex.Single


class ExecutionImpl<E>(
        val result: Single<E>
) : Execution<E> {
    override fun result(): Single<E> {
        return result
    }
}