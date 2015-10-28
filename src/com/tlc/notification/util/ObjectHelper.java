package com.tlc.notification.util;

import java.lang.Object;

import dalvik.system.DexClassLoader;

import android.util.Log;
import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import java.lang.ClassNotFoundException;
import java.lang.SecurityException;
import java.lang.Exception;
import java.lang.NoSuchMethodException;
import java.lang.NoSuchFieldException;
import java.util.Arrays;

/**
 * A collection functions of to dynamically invoking some method .
 *
 */
public class ObjectHelper {
	private final static String TAG="ObjectHelper";
	private Object obj = null;
	public static ObjectHelper get(Object ori_obj) {
		if (ori_obj == null) return null;
		return new ObjectHelper(ori_obj);
	}
	public static ObjectHelper[] get(Object[] ori_obj) {
		if (ori_obj == null) return null;
		ObjectHelper a[] = new ObjectHelper[ori_obj.length];
		for(int i=0;i<ori_obj.length; i++){
			a[i] = get(ori_obj[i]);
		}
		return a;
	}
	public ObjectHelper(Object ori_obj) {
		//Log.e(TAG,"create a helper obj for obj of class:"+ori_obj.getClass().getName());
		obj = ori_obj;
	}
	public Object getObj() {
		return obj;
	}
	public Object m(String method, Class[] types, Object []args) {
		try {
			Class<?> c = obj.getClass();
			Method m = c.getMethod(method, types);
			//Log.e(TAG,"now call obj method:"+m.toString());
			return m.invoke(obj, args);
		}
		catch (NoSuchMethodException e) {
			Log.e(TAG, "Can't find method:"+method);
		}
		catch (SecurityException e) {
			Log.e(TAG, "Can't get method:"+method+" for security issue");
		}
		catch (Exception e){
			e.printStackTrace();
			Log.e(TAG, "call method:"+method+" failed:"+e.getMessage());
		}
		return null;
	}
	public Object m(String method) {
		return m(method, (Object[])null);
	}
	public Object m(String method, Object obj) {
		return m(method, new Object[] {obj});
	}
	public Object m(String method, Class<?> type, Object obj) {
		return m(method, new Class[]{type}, new Object[] {obj});
	}
	public Object m(String method, Object[] args) {
		try {
			Class<?> c = obj.getClass();
			Method m=null;
			if(args != null){
				Class [] types = new Class [args.length];
				for(int i = 0; i<args.length; i++) {
					if(args[i] != null) {
						types[i] = args[i].getClass();
					}
					else types[i] = String.class;  //FIXME, use the string type as default 
				}
				m = c.getMethod(method, types);
			}
			else {
				m = c.getMethod(method, (Class [])null);
			}
			//Log.e(TAG,"now call obj method:"+m.toString());
			return m.invoke(obj, args);
		}
		catch (NoSuchMethodException e) {
			//e.printStackTrace();
			Log.e(TAG, "Can't find method:"+method);
		}
		catch (SecurityException e) {
			Log.e(TAG, "Can't get method:"+method+" for security issue");
		}
		catch (Exception e){
			Log.e(TAG, "call method:"+method+" failed:"+e.getMessage());
		}
		return null;
	}
	public Object f(String member){
		try {
			Class<?> c = obj.getClass();
			Field f=null;
			f = c.getField(member);
			return f.get(obj);
		}
		catch (NoSuchFieldException e) {
			Log.e(TAG, "Can't find member:"+member);
		}
		catch (SecurityException e) {
			Log.e(TAG, "Can't get member:"+member+" for security issue");
		}
		catch (Exception e){
			Log.e(TAG, "call member:"+member+" failed");
		}
		return null;
	}
	
	public Object getFieldValue(String member){
		try {
			Class<?> c = obj.getClass();
			Field field = c.getDeclaredField(member);
			field.setAccessible(true);
			return field.get(obj);
		}
		catch (NoSuchFieldException e) {
			Log.e(TAG, "Can't find member:"+member);
		}
		catch (SecurityException e) {
			Log.e(TAG, "Can't get member:"+member+" for security issue");
		}
		catch (Exception e){
			Log.e(TAG, "call member:"+member+" failed");
		}
		return null;
	}
}