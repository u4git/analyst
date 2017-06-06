package org.wangli.tools.analyst.ftrace.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeRslt {

	private Map<String, FuncItem> funcItems = new HashMap<String, FuncItem>();

	public void addFuncItem(String key, FuncItem funcItem) {
		FuncItem existedFuncItem = this.funcItems.get(key);
		if (existedFuncItem != null) {
			existedFuncItem.addFuncItem(funcItem);
		} else {
			this.funcItems.put(key, funcItem);
		}
	}

	public Set<String> getKeys() {
		return funcItems.keySet();
	}

	public FuncItem getFuncItem(String key) {
		return this.funcItems.get(key);
	}

}
