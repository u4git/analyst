package org.wangli.tools.analyst.pagewalk.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeRslt {

	// 所有操作执行结果
	private Map<String, OpTRslt> opTRslts = new HashMap<String, OpTRslt>();

	// 所有OpTRslt的key
	private List<String> keys = new LinkedList<String>();

	// 合并后的操作结果
	private OpTRslt mergedOpTRslt;

	// 合并的中间结果
	private List<NodeRslt> nodeRslts2merge = new LinkedList<NodeRslt>();

	// 权值
	private double weight;

	public void putOpTRslt(String key, OpTRslt value) {
		this.opTRslts.put(key, value);
		if (!this.keys.contains(key)) {
			this.keys.add(key);
		}
	}

	public OpTRslt getOpTRslt(String key) {
		return this.opTRslts.get(key);
	}

	public List<String> getKeys() {
		return keys;
	}

	public void mergeOpTRslts() {
		Set<String> keys = this.opTRslts.keySet();
		for (String key : keys) {
			OpTRslt opTRslt = this.opTRslts.get(key);
			if (this.mergedOpTRslt == null) {
				this.mergedOpTRslt = opTRslt;
			} else {
				this.mergedOpTRslt.addOpTRslt4Merge(opTRslt);
			}
		}
		this.mergedOpTRslt.doMergeOpTRslts();
	}

	public void addNodeRslt4Merge(NodeRslt nodeRslt) {
		this.nodeRslts2merge.add(nodeRslt);
	}

	public void doMergeNodeRslts() {
		/*
		 * 先把当前对象的结果加权
		 */
		for (String key : this.mergedOpTRslt.getKeys()) {
			double value = this.mergedOpTRslt.getRslt(key);
			value = value * this.weight;
			this.mergedOpTRslt.putRslt(key, value);
		}

		/*
		 * 合并其他NodeRslt
		 */
		for (NodeRslt nodeRslt : this.nodeRslts2merge) {
			OpTRslt opTRslt = nodeRslt.getMergedOpTRslt();
			List<String> keys = opTRslt.getKeys();
			for (String key : keys) {
				double value = opTRslt.getRslt(key);
				value = value * nodeRslt.getWeight();

				// 已有的值
				Double existedValue = this.mergedOpTRslt.getRslt(key);
				if (existedValue == null) {
					existedValue = new Double(0);
				}
				existedValue = existedValue + value;
				this.mergedOpTRslt.putRslt(key, existedValue);
			}
		}
	}

	public OpTRslt getMergedOpTRslt() {
		return mergedOpTRslt;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

}
