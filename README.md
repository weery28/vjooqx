# vjooqx
Asynchronus JOOQ wrap for Vert.x

[![](https://jitpack.io/v/weery28/vjooqx.svg)](https://jitpack.io/#weery28/vjooqx)

This wrap provide JOOQ with vertx SQLClient and SQLConnection. 
Last version containes only RxJava implementation.
Besides, from the box you will take mapping futures.
1. Alias-mapping like standart Jooq.
2. Tree-mapping. You can map table result set on Json object without repetition. All fields, that you indicate as foldable, will be JsonArray. 

You can provide your own Json parser by implemented JsonFactory and JsonParser interfaces.

## How work with it?
1. Create Vjooqx instance with DSL, JsonParser, Vertx-SqlClient

```kotlin

val vjooqx = VjooqxBuilder()
                .dsl(dslContext)
                .setupDelegate(sqlClient)
                .jsonFactory(GsonParserFactory())
                .create()
```
2. Usage Vjooqx! 

### vjooqx.execute 
This function execute SQL query and return count of edited rows.
### vjooqx.fetch
This function pepared vjooqx to execute SQL query and return result set.
#### to
  Execute query and map result into your class.
#### toList
  Execute query and map result into list of your class.
#### toTree
  Execute query and map result into your class with folding idicated fields.
#### toTreeList
  Execute query and map result into list of your class with folding idicated fields.
  
