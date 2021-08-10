package com.cavetale.tutor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.function.Supplier;

public final class Json {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private Json() { }

    public static String serialize(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T deserialize(String json, Class<T> type, Supplier<T> dfl) {
        try {
            return json != null ? GSON.fromJson(json, type) : dfl.get();
        } catch (Exception e) {
            e.printStackTrace();
            return dfl.get();
        }
    }
}
