package org.wangli.tools.analyst.ftrace.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExecRslt {

	private Map<String, NodeRslt> nodeRslts = new HashMap<String, NodeRslt>();

	public void putNodeRslt(String key, NodeRslt value) {
		this.nodeRslts.put(key, value);
	}

	public Set<String> getKeys() {
		return this.nodeRslts.keySet();
	}

	public NodeRslt getNodeRslt(String key) {
		return this.nodeRslts.get(key);
	}

}
