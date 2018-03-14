import com.github.weery28.json.JsonParser
import com.google.gson.Gson

class GsonParser(
        val gson : Gson
) : JsonParser{

    override fun <T> encode(json: String, pClass: Class<T>): T {
        return gson.fromJson(json, pClass)
    }
}