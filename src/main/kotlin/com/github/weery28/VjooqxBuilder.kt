package com.github.weery28

import com.github.weery28.json.JsonParserFactory
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

    fun jsonFactory(jsonParserFactory: JsonParserFactory) : VjooqxBuilder
}



class VjooqxBuilder : Builder, SetupDSLStep, Creator, SetupJsonFactory{

    private lateinit var dslContext: DSLContext

    private lateinit var asyncSQLClient: AsyncSQLClient

    private lateinit var jsonParserFactory: JsonParserFactory

    override fun setupDelegate(delegate: AsyncSQLClient): VjooqxBuilder {
        asyncSQLClient = delegate
        return this
    }

    override fun dsl(dslContext: DSLContext) : VjooqxBuilder {
        this.dslContext = dslContext
        return this
    }

    override fun create() : Vjooqx {
        return Vjooqx(asyncSQLClient, dslContext, jsonParserFactory.buildJsonParser())
    }

    override fun jsonFactory(jsonParserFactory: JsonParserFactory) : VjooqxBuilder{
        this.jsonParserFactory = jsonParserFactory
        return this
    }
}
