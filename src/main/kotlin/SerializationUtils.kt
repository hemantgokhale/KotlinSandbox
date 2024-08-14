import kotlinx.serialization.json.*

// Get object property
internal operator fun JsonElement.get(name: String): JsonElement = if (this is JsonObject) this[name] ?: JsonNull else JsonNull
internal fun JsonElement.getOrNull(name: String): JsonElement? = if (this is JsonObject) this[name] else null

// Get array element
internal operator fun JsonElement.get(index: Int): JsonElement = if ((this is JsonArray) && (index in 0 until size)) this[index] else JsonNull

// Get a list of elements in an array. If the element is not an array, you get an empty list
internal fun JsonElement.list(): List<JsonElement> = if (this is JsonArray) this else listOf()

internal val JsonElement.stringOrNull: String?
    get() = if ((this is JsonPrimitive) && isString) content else null
internal val JsonElement.booleanOrNull: Boolean?
    get() = (this as? JsonPrimitive)?.content?.toBoolean()
internal val JsonElement.doubleOrNull: Double?
    get() = (this as? JsonPrimitive)?.content?.toDoubleOrNull()
internal val JsonElement.intOrNull: Int?
    get() = (this as? JsonPrimitive)?.content?.toIntOrNull()
internal val JsonElement.longOrNull: Long?
    get() = (this as? JsonPrimitive)?.content?.toLongOrNull()

internal val JsonElement.string: String
    get() = stringOrNull ?: ""
internal val JsonElement.boolean: Boolean
    get() = booleanOrNull ?: false
internal val JsonElement.double: Double
    get() = doubleOrNull ?: 0.0
internal val JsonElement.int: Int
    get() = intOrNull ?: 0
internal val JsonElement.long: Long
    get() = longOrNull ?: 0L
