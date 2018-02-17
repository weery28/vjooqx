package com.github.weery28

import com.google.gson.GsonBuilder
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import org.jooq.DSLContext

interface Builder{

    fun setupDelegate (delegate : AsyncSQLClient) : SetupDSLStep
}

interface SetupDSLStep{

    fun dsl(dslContext: DSLContext) : Builder
}

interface Creator {

    fun create() : Vjooqx
}

interface SetupJsonFactory{

    fun jsonFactory(gsonBuilder: GsonBuilder) : Creator
}



class VjooqxBuilder : Builder, SetupDSLStep, Creator, SetupJsonFactory{

    private lateinit var dslContext: DSLContext

    private lateinit var asyncSQLClient: AsyncSQLClient

    private lateinit var gsonBuilder: GsonBuilder

    override fun setupDelegate(delegate: AsyncSQLClient): VjooqxBuilder {
        asyncSQLClient = delegate
        return this
    }

    override fun dsl(dslContext: DSLContext) : VjooqxBuilder {
        this.dslContext = dslContext
        return this
    }

    override fun create() : Vjooqx {
        return Vjooqx(asyncSQLClient, dslContext, gsonBuilder)
    }

    override fun jsonFactory(gsonBuilder: GsonBuilder) : VjooqxBuilder{
        this.gsonBuilder = gsonBuilder
        return this
    }
}
