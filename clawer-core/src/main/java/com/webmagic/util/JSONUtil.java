package com.webmagic.util;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JSONUtil {
    public static final String YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss"; // 日期格式: yyyy-MM-dd HH:mm:ss
    
    public static enum QuoteType{
    	DOUBLE, SINGLE
    }
    
    /**
     * 将JSON字符串转换为Java对象.
     * 
     * @param jsonStr
     * @param clazz 目标对象的Class类.
     * @return Java对象
     */
    @SuppressWarnings("unchecked")
	public static <T> T json2Object(String jsonStr, Class<?> clazz) {
        try {
            return (T) JSON.parseObject(jsonStr, clazz);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Java对象转换为json字符串.
     * <p>
     * 引号类型，如双引号可以使用<pre>JSONUtil.QuoteType.DOUBLE</pre>
     * 如果引号类型传入null，则使用双引号，并格式化json字符串.
     * <b>注意：</b>使用单引号时，禁用循环引用检测.
     * 
     * @param obj
     * @param quoteType 引号类型
     * @return
     */
    public static String object2Json(Object obj, QuoteType quoteType) {
        String jsonString = null;
        try {
            if (QuoteType.DOUBLE == quoteType) { // 双引号
                jsonString = JSON.toJSONString(obj);
            } else if (QuoteType.SINGLE == quoteType) { // 单引号
                jsonString = JSON.toJSONString(obj, SerializerFeature.UseSingleQuotes,
                    SerializerFeature.DisableCircularReferenceDetect);
            } else {
                jsonString = JSON.toJSONString(obj, true);
            }
            return jsonString;
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 将Java对象转换为json字符串.
     * 禁用循环引用检测.
     * 
     * @param obj
     * @return json字符串.
     */
    public static String toJson(Object obj){
    	try{
    		return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
    	}catch(Exception e){
    		throw e;
    	}
    }
    
    /**
     * 将Java对象转换为json字符串，并格式化json字符串.
     * 禁用循环引用检测.
     * 
     * @param obj
     * @return
     */
    public static String toPrettyJason(Object obj) {
        try {
            return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.PrettyFormat);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 对象转为json字符串.
     * 对于日期使用格式(yyyy-MM-dd HH:mm:ss).
     * 禁用循环引用检测.
     * 
     * @param obj
     * @return
     */
    public static String object2DateFormatJson(Object obj) {
    	return object2DateFormatJson(obj, YYYYMMDDHHMMSS);
    }
    
    /**
     * 对象转为json字符串.
     * 对于日期的格式化使用传入的日期格式：如"yyyy-MM-dd".
     * 禁用循环引用检测.
     * 
     * @param obj
     * @param dateFmt
     * @return
     */
    public static String object2DateFormatJson(Object obj, String dateFmt) {
    	try {
            return JSON.toJSONStringWithDateFormat(obj, dateFmt,
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.DisableCircularReferenceDetect);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * json字符串转对象集（List）.
     * 将json数组，转换成对象列表.
     * 
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> List<T> json2ObjectList(String jsonString, Class<T> clazz) {
        try {
            return JSON.parseArray(jsonString, clazz);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * json字符串转List字符串.
     * 即将json数组转成List，List中的每个元素是一个json字符串.
     * 
     * @param jsonString
     * @return
     */
    public static List<String> json2StringList(String jsonString) {
        try {
            return JSON.parseObject(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw e;
        }
    }
    
	/**
	 * json字符串转Map对象.
	 * 
	 * @param jsonString json字符串
	 * @return Map对象
	 */
    public static Map<String, Object> json2Map(String jsonString) {
		try {
			return JSON.parseObject(jsonString,
					new TypeReference<Map<String, Object>>() {
					});
		} catch (Exception e) {
			throw e;
		}
    }
    
    /**
     * json字符串转Map对象集.
     * 
     * @param jsonString json字符串
     * @return Map对象集合
     */
    public static List<Map<String, Object>> json2MapList(String jsonString) {
        try {
            return JSON.parseObject(jsonString, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * json字符串转对象数组.
     * 
     * @param jsonString json字符串
     * @return 对象数组
     */
    public static Object[] json2Array(String jsonString) {
        try {
            return JSON.parseArray(jsonString).toArray();
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * json字符串转JSONArray对象.
     * 
     * @param jsonString json字符串
     * @return JSONArray对象
     */
    public static JSONArray json2JSONArray(String jsonString) {
        try {
            return JSON.parseArray(jsonString);
        } catch (Exception e) {
        	throw e;
        }
    }
    
    /**
     * 字符串数组转json字符串.
     * 
     * @param strs 字符串数组
     * @param quoteType 引号类型
     * @return json字符串
     * @see #object2Json(Object, QuoteType)
     */
    public static String array2Json(String[] strs, QuoteType quoteType) {
        String jsonString = null;
        try {
            if (QuoteType.DOUBLE == quoteType) {
                jsonString = JSON.toJSONString(strs);
            } else if (QuoteType.SINGLE == quoteType) {
                jsonString = JSON.toJSONString(strs, SerializerFeature.UseSingleQuotes);
            } else {
                jsonString = JSON.toJSONString(strs, true);
            }
            return jsonString;
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 数组(Array)转json字符串.
     * 
     * @param ary
     *            数组
     * @param quotesType
     *            : 0:为双引号 1：单引号
     * @return json字符串
     */
    /**
     * 数组(Array)转json字符串.
     * 
     * @param ary Array对象
     * @param quoteType 引号类型
     * @return json字符串
     * @see #object2Json(Object, QuoteType)
     */
    public static String array2Json(Array ary, QuoteType quoteType) {
        String jsonString = null;
        try {
            if (QuoteType.DOUBLE == quoteType) {
                jsonString = JSON.toJSONString(ary);
            } else if (QuoteType.SINGLE == quoteType) {
                jsonString = JSON.toJSONString(ary, SerializerFeature.UseSingleQuotes);
            } else {
                jsonString = JSON.toJSONString(ary, true);
            }
            return jsonString;
        } catch (Exception e) {
            throw e;
        }
    }
}
