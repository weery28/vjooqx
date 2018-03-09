import com.github.weery28.Vjooqx
import com.github.weery28.VjooqxBuilder
import io.vertx.core.json.JsonObject

import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.asyncsql.AsyncSQLClient
import io.vertx.reactivex.ext.asyncsql.PostgreSQLClient
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.Before
import org.junit.Test


class SimpleTest{

    lateinit var vertx : Vertx
    lateinit var vjooqx: Vjooqx
    lateinit var delegate : AsyncSQLClient

    @Before
    fun prepare(){

        vertx = Vertx.vertx()
        val config = JsonObject().apply {
            put("host", "localhost")
            put("username", "admin")
            put("password", "123456")
            put("database", "sportvisor")
        }

        delegate = PostgreSQLClient.createNonShared(vertx, config)

        vjooqx = VjooqxBuilder()
                .dsl(DSL.using(SQLDialect.POSTGRES))
                .setupDelegate(delegate)
                .jsonFactory(GsonFactory())
                .create()
    }

    @Test
    fun start(){

    }
}