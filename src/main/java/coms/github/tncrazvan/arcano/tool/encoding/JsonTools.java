package com.github.tncrazvan.arcano.tool.encoding;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 *
 * @author Razvan Tanase
 */
public interface JsonTools {
    static final Gson JSON_PARSER = new Gson();
    
    //JsonObject
    static JsonObject jsonObject(final byte[] data) {
        return JSON_PARSER.fromJson(new String(data), JsonObject.class);
    }

    static JsonObject jsonObject(final String str) {
        return JSON_PARSER.fromJson(str, JsonObject.class);
    }

    static JsonObject jsonObject(final StringBuilder str) {
        return jsonObject(str.toString());
    }

    static JsonObject jsonObject(final Object o) {
        return JSON_PARSER.toJsonTree(o).getAsJsonObject();
    }

    // JsonArray
    static JsonArray jsonArray(final String str) {
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }

    static JsonArray jsonArray(final Reader str) {
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }

    static JsonArray jsonArray(final JsonReader str) {
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }

    static JsonArray jsonArray(final StringBuilder str) {
        return jsonArray(str.toString());
    }

    static JsonArray jsonArray(final Object[] o) {
        return JSON_PARSER.toJsonTree(o).getAsJsonArray();
    }

    // STRINGIFY
    static String jsonStringify(final Object o) {
        return jsonObject(o).toString();
    }

    static String jsonStringify(final Object[] o) {
        return jsonArray(o).toString();
    }

    // PARSE TO ?
    static <T> T jsonParse(final String o, final Class<T> cls) {
        return (T) JSON_PARSER.fromJson(o, cls);
    }

    static <T> T jsonParse(final Reader o, final Class<T> cls) {
        return (T) JSON_PARSER.fromJson(o, cls);
    }

    static <T> T jsonParse(final JsonReader o, final Class<T> cls) {
        return (T) JSON_PARSER.fromJson(o, cls);
    }

    static <T> T jsonParse(final StringBuilder o, final Class<T> cls) {
        return jsonParse(o.toString(), cls);
    }

    static <T> T jsonParse(final JsonElement o, final Class<T> cls) {
        return (T) JSON_PARSER.fromJson(o, cls);
    }
}
