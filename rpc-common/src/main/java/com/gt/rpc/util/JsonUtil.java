package com.gt.rpc.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Based on Jackson
 * @author GTsung
 * @date 2022/1/15
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(format);
        // 屬性為null不序列化
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        mapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        mapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        // 發現空beans不抛異常
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 未知屬性不抛異常
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 忽略未定義的
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    public static <T> byte[] serialize(T obj) {
        byte[] bytes = new byte[0];
        try {
            bytes = mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.info("{}序列化失敗，異常信息:{}", obj, e);
            throw new IllegalStateException(e.getMessage(), e);
        }
        return bytes;
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        T obj = null;
        try {
            obj = mapper.readValue(bytes, clazz);
        } catch (IOException e) {
            log.info("{}-反序列化失敗", bytes);
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static <T> T json2Obj(String json, Class<?> clazz) {
        T obj = null;
        JavaType type = mapper.getTypeFactory().constructType(clazz);
        try {
            obj = mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static <T> T json2ObjList(String json, Class<?> collectionClass, Class<?> elementClass) {
        T obj = null;
        JavaType type = mapper.getTypeFactory().constructParametricType(collectionClass, elementClass);
        try {
            obj = mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.info("{}-反序列化失敗", json);
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static <T> T json2ObjMap(String json, Class<?> keyClass, Class<?> valueClass) {
        T obj = null;
        JavaType type = mapper.getTypeFactory().constructParametricType(HashMap.class, keyClass, valueClass);
        try {
            obj = mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static String obj2Json(Object obj) {
        String json = "";
        try {
            mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return json;
    }

}
