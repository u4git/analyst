package org.wangli.tools.analyst.pagewalk.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkloadRsltSet {

	// 所有WorkloadRslt
	private Map<String, WorkloadRslt> workloadRslts = new HashMap<String, WorkloadRslt>();

	// 所有负载名称（用于排序遍历）
	private List<String> keys = new LinkedList<String>();

	public void putWorkloadRslt(String key, WorkloadRslt value) {
		this.workloadRslts.put(key, value);
		if (!this.keys.contains(key)) {
			this.keys.add(key);
		}
	}

	public WorkloadRslt getWorkloadRslt(String key) {
		return this.workloadRslts.get(key);
	}

	public List<String> getKeys() {
		return this.keys;
	}

	public void merge() {
		Set<String> keys = this.workloadRslts.keySet();
		for (String key : keys) {
			this.workloadRslts.get(key).merge();
		}
	}

}
