package com.github.weery28.transactions

interface MapperStepTransaction {

    fun <T> to(pClass: Class<T>): TransactionStep<T>

    fun <T> toListOf(pClass: Class<T>): TransactionStep<List<T>>

    fun <T> toTree(pClass: Class<T>, listAliases: List<String>): TransactionStep<T>

    fun <T> toTreeList(pClass: Class<T>, listAliases: List<String>): TransactionStep<List<T>>
}




