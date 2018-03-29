package com.github.weery28.transactions

import io.reactivex.Single

interface Execution <E>{

	fun result() : Single<E>
}