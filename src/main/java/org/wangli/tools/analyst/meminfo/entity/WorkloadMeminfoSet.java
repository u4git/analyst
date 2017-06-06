package org.wangli.tools.analyst.meminfo.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkloadMeminfoSet {

	// 记录所有负载的结果
	private Map<String, WorkloadMeminfo> workloadMeminfos = new HashMap<String, WorkloadMeminfo>();

	// 记录所有负载名称（为了排序）
	private List<String> workloadNames = new LinkedList<String>();

	public void putWorkloadMeminfo(String key, WorkloadMeminfo value) {
		this.workloadMeminfos.put(key, value);
		this.workloadNames.add(key);
	}

	public WorkloadMeminfo getWorkloadMeminfo(String key) {
		return this.workloadMeminfos.get(key);
	}

	public List<String> getWorkloadNames() {
		return this.workloadNames;
	}

}
