package org.wangli.tools.analyst.log;

import org.slf4j.LoggerFactory;

public class Logger {

	/**
	 * 记录系统日志
	 * 
	 * @param clazz
	 * @param info
	 */
	public static void logSysInfo(Class<?> clazz, String info) {
		LoggerFactory.getLogger(clazz).info(info);
	}

	/**
	 * 记录错误日志
	 * 
	 * @param clazz
	 * @param info
	 */
	public static void logErrInfo(Class<?> clazz, String info) {
		LoggerFactory.getLogger(clazz).error(info);
	}

	/**
	 * 记录错误日志
	 * 
	 * @param clazz
	 * @param info
	 * @param e
	 */
	public static void logErrInfo(Class<?> clazz, String info, Exception e) {
		LoggerFactory.getLogger(clazz).error(info, e);
	}

	/**
	 * 记录debug日志
	 * 
	 * @param clazz
	 * @param info
	 */
	public static void logDebugInfo(Class<?> clazz, String info) {
		LoggerFactory.getLogger(clazz).debug(info);
	}

}
