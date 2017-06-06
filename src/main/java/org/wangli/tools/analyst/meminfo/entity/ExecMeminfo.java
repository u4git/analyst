package org.wangli.tools.analyst.meminfo.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExecMeminfo {

	private Map<String, Double> meminfos = new HashMap<String, Double>();

	public void putMeminfo(String key, Double value) {
		this.meminfos.put(key, value);
	}

	public Double getMeminfo(String key) {
		return this.meminfos.get(key);
	}

	public Set<String> getKeySet() {
		return this.meminfos.keySet();
	}

}
