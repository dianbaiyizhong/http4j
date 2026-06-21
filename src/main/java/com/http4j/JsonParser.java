package com.http4j;

/**
 * Pluggable JSON parser interface.
 * <p>
 * Implement this interface with your preferred JSON library (Gson, Jackson, Fastjson, etc.)
 * and set it on {@link Http4jConfig#setJsonParser(JsonParser)}.
 * <p>
 * The parser is used by the SDK to extract business code/message from response bodies.
 */
public interface JsonParser {

    /**
     * Parse a JSON string into its Java representation.
     * <p>
     * The expected return types are:
     * <ul>
     *   <li>{@link java.util.Map} for JSON objects</li>
     *   <li>{@link java.util.List} for JSON arrays</li>
     *   <li>{@link String} for JSON strings</li>
     *   <li>{@link Number} for JSON numbers</li>
     *   <li>{@link Boolean} for JSON booleans</li>
     *   <li>{@code null} for JSON null</li>
     * </ul>
     *
     * @param json the JSON string to parse
     * @return the parsed Java object, or {@code null} if the input is not valid JSON
     */
    Object parse(String json);

    /**
     * Parse a JSON string into an instance of the specified class.
     * <p>
     * Used by {@link Http4jRequest#execute(Class)} to deserialize
     * HTTP response bodies directly into business objects.
     * <p>
     * Implementation examples:
     * <pre>{@code
     * // Gson
     * return new Gson().fromJson(json, clazz);
     *
     * // Jackson
     * return new ObjectMapper().readValue(json, clazz);
     *
     * // Fastjson
     * return JSON.parseObject(json, clazz);
     * }</pre>
     *
     * @param json  the JSON string to parse
     * @param clazz the target class
     * @param <T>   the target type
     * @return the deserialized instance, or {@code null} if the input is not valid JSON
     */
    <T> T parse(String json, Class<T> clazz);
}
