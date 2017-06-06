package org.wangli.tools.analyst.meminfo.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkloadMeminfo {

	private Map<String, ExecMeminfo> execMeminfos = new HashMap<String, ExecMeminfo>();

	// 所有页大小（用于排序）
	private List<String> pageSizes = new LinkedList<String>();

	public void putExecMeminfo(String key, ExecMeminfo value) {
		this.execMeminfos.put(key, value);
		this.pageSizes.add(key);
	}

	public ExecMeminfo getExecMeminfo(String key) {
		return this.execMeminfos.get(key);
	}

	public List<String> getPageSizes() {
		return this.pageSizes;
	}

}
