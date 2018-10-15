package com.yuan.rpcx.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.animation.KeyValue;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class JsonUtil {

	private JsonUtil(){
	}

	public static Object toBean(String text) {
		return JSON.parse(text);
	}

	public static <T> T toBean(String text, Class<T> clazz) {
		return JSON.parseObject(text, clazz);
	}

	// 转换为数组
	public static <T> Object[] toArray(String text) {
		return toArray(text, null);
	}

	// 转换为数组
	public static <T> Object[] toArray(String text, Class<T> clazz) {
		return JSON.parseArray(text, clazz).toArray();
	}

	// 转换为List
	public static <T> List<T> toList(String text, Class<T> clazz) {
		return JSON.parseArray(text, clazz);
	}

	public static<T> List<T> jsonToArray(final String json,Class<T> clz){

		List<Object> objects = JSON.parseArray(json);
		List<T> result = new ArrayList<>();

		if (objects == null){
			return null;
		}
		else{
			for(Object object: objects){
				result.add(JSON.parseObject(JSON.toJSONString(object), clz));
			}
		}

		return result;
	}

	/**
	 * 将javabean转化为序列化的json字符串
	 * @param keyvalue
	 * @return
	 */
	public static Object beanToJson(KeyValue keyvalue) {
		String textJson = JSON.toJSONString(keyvalue);
		Object objectJson  = JSON.parse(textJson);
		return objectJson;
	}

	/**
	 * 将string转化为序列化的json字符串
	 * @param text
	 * @return
	 */
	public static Object textToJson(String text) {
		Object objectJson  = JSON.parse(text);
		return objectJson;
	}

	/**
	 * json字符串转化为map
	 * @param s
	 * @return
	 */
	public static Map stringToCollect(String s) {
		Map m = JSONObject.parseObject(s);
		return m;
	}

	/**
	 * 将map转化为string
	 * @param m
	 * @return
	 */
	public static String collectToString(Map m) {
		String s = JSONObject.toJSONString(m);
		return s;
	}

	/**
	 * 将JSON对象转换成 URL参数拼接(a=a&b=b)
	 * 
	 * @param jsonObj
	 * @return
	 */
	public static String toUrlParam(JSONObject jsonObj) {
		List<String> params = new ArrayList<String>();

		Iterator iter = jsonObj.keySet().iterator();
		while (iter.hasNext()) {
			String key = Strings.parseString(iter.next());
			Object value = jsonObj.get(key);
			if (value instanceof Date) {
				/** 将日期格式转换成 yyyy-MM-dd **/
				value = Dates.getDateTime((Date) value, Dates.getDefaultDateFormat());
			} else {
				value = Strings.parseString(value);
			}
			params.add(key + "=" + value);
		}
		return StringUtils.join(params.toArray(), "&");
	}

	public static void main(String[] args) {

		Map<String,String> map = new HashMap<>();
		map.put("name","yuanqing");
		map.put("age","37");

		String str = collectToString(map);

		System.out.println(str);
	}

}
