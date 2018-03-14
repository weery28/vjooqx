package com.github.weery28

import io.reactivex.Single

interface MapperStep {

    fun <T> to(pClass: Class<T>): Single<T>

    fun <T> toListOf(pClass: Class<T>): Single<List<T>>

    fun <T> toTree(pClass: Class<T>, listAliases: List<String>): Single<T>

    fun <T> toTreeList(pClass: Class<T>, listAliases: List<String>): Single<List<T>>
}




