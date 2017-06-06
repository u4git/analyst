package org.wangli.tools.analyst.pagewalk.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamRslt {

	// 记录每一次执行的结果
	private Map<String, ExecTRslt> execTRslts = new HashMap<String, ExecTRslt>();

	// 所有ExecTRslt的key
	private List<String> keys = new LinkedList<String>();

	// 合并后的执行结果
	private ExecTRslt mergedExecTRslt;

	public void putExecTRslt(String key, ExecTRslt value) {
		this.execTRslts.put(key, value);
		if (!this.keys.contains(key)) {
			this.keys.add(key);
		}
	}

	public ExecTRslt getExecTRslt(String key) {
		return this.execTRslts.get(key);
	}

	public List<String> getKeys() {
		return keys;
	}

	public void mergeExecTRslts() {
		/*
		 * 每一个ExecTRslt进行合并，然后再合并成一个ExecTRslt
		 */
		Set<String> keys = this.execTRslts.keySet();
		for (String key : keys) {
			ExecTRslt execTRslt = this.execTRslts.get(key);
			execTRslt.mergeNodeRslts();
			if (this.mergedExecTRslt == null) {
				this.mergedExecTRslt = execTRslt;
			} else {
				this.mergedExecTRslt.addExecTRslt4Merge(execTRslt);
			}
		}
		this.mergedExecTRslt.doMergeExecTRslts();
	}

	public ExecTRslt getMergedExecTRslt() {
		return mergedExecTRslt;
	}

}
