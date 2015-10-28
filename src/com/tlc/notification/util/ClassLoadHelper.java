package com.tlc.notification.util;

import java.lang.reflect.Constructor;

import android.util.Log;

/**
 * A collection functions of class operations
 *
 */
public class ClassLoadHelper {
	
	private final static String TAG="ClassLoadHelper";
	
	public static Class<?> getClass(String cname) throws Exception {
			return Class.forName(cname);
	}
	public static Object createObject(String cname, Class<?> [] types,Object[] args) {
		try {
			Class<?> c = getClass(cname);
			if(args != null){
				Constructor<?> s = c.getConstructor(types);
				return s.newInstance(args);
			}
			else {
				return c.newInstance();
			}
		}
		catch (Exception e){
			e.printStackTrace();
			Log.e(TAG, "create object failed for class:"+cname);
		}
		return null;
	}
}