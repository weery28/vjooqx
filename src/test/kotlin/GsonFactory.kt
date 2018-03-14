import com.github.weery28.json.JsonParser
import com.github.weery28.json.JsonParserFactory
import com.google.gson.Gson

class GsonFactory : JsonParserFactory{

    override fun buildJsonParser(): JsonParser {
        return GsonParser(Gson())
    }
}