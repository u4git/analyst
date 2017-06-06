package org.wangli.tools.analyst.pagewalk.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkloadRslt {

	// 所有ParamRslt
	private Map<String, ParamRslt> paramRslts = new HashMap<String, ParamRslt>();

	// 所有ParamRslt的key（用于排序遍历）
	private List<String> keys = new LinkedList<String>();

	public void putParamRslt(String key, ParamRslt value) {
		this.paramRslts.put(key, value);
		if (!this.keys.contains(key)) {
			this.keys.add(key);
		}
	}

	public ParamRslt getParamRslt(String key) {
		return this.paramRslts.get(key);
	}

	public List<String> getKeys() {
		return this.keys;
	}

	public void merge() {
		Set<String> keys = this.paramRslts.keySet();
		for (String key : keys) {
			this.paramRslts.get(key).mergeExecTRslts();
		}
	}

}
