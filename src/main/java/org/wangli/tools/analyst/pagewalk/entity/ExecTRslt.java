package org.wangli.tools.analyst.pagewalk.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wangli.tools.analyst.pagewalk.config.Const;

public class ExecTRslt {

	// 所有节点的追踪数据
	private Map<String, NodeRslt> nodeRslts = new HashMap<String, NodeRslt>();

	// 所有NodeRslt的key
	private List<String> keys = new LinkedList<String>();

	// 合并后的节点结果
	private NodeRslt mergedNodeRslt;

	// 合并的中间结果
	private List<ExecTRslt> execTRslts2merge = new LinkedList<ExecTRslt>();

	public void putNodeRslt(String key, NodeRslt value) {
		this.nodeRslts.put(key, value);
		if (!this.keys.contains(key)) {
			this.keys.add(key);
		}
	}

	public NodeRslt getNodeRslt(String key) {
		return this.nodeRslts.get(key);
	}

	public List<String> getKeys() {
		return keys;
	}

	public void mergeNodeRslts() {
		// 合并OpTRslts，计算权值的分母
		double sum4weight = 0;
		Set<String> keys = this.nodeRslts.keySet();
		for (String key : keys) {
			NodeRslt nodeRslt = this.nodeRslts.get(key);
			nodeRslt.mergeOpTRslts();
			sum4weight = sum4weight + nodeRslt.getMergedOpTRslt().getRslt(Const.weightName);
		}

		// 计算权值
		for (String key : keys) {
			NodeRslt nodeRslt = this.nodeRslts.get(key);
			nodeRslt.setWeight(nodeRslt.getMergedOpTRslt().getRslt(Const.weightName) / sum4weight);
		}

		// 合并
		for (String key : keys) {
			NodeRslt nodeRslt = this.nodeRslts.get(key);
			if (this.mergedNodeRslt == null) {
				this.mergedNodeRslt = nodeRslt;
			} else {
				this.mergedNodeRslt.addNodeRslt4Merge(nodeRslt);
			}
		}
		this.mergedNodeRslt.doMergeNodeRslts();
	}

	public void addExecTRslt4Merge(ExecTRslt execTRslt) {
		this.execTRslts2merge.add(execTRslt);
	}

	public void doMergeExecTRslts() {
		// 合并结果用
		Map<String, List<Double>> values = new HashMap<String, List<Double>>();

		// 先放入当前结果
		for (String key : this.mergedNodeRslt.getMergedOpTRslt().getKeys()) {
			double value = this.mergedNodeRslt.getMergedOpTRslt().getRslt(key);
			List<Double> valueList = values.get(key);
			if (valueList == null) {
				valueList = new LinkedList<Double>();
				values.put(key, valueList);
			}
			valueList.add(value);
		}

		// 放入待合并结果
		for (ExecTRslt execTRslt : this.execTRslts2merge) {
			NodeRslt nodeRslt = execTRslt.getMergedNodeRslt();
			OpTRslt opTRslt = nodeRslt.getMergedOpTRslt();
			List<String> keys = opTRslt.getKeys();
			for (String key : keys) {
				double value = opTRslt.getRslt(key);
				List<Double> valueList = values.get(key);
				if (valueList == null) {
					valueList = new LinkedList<Double>();
					values.put(key, valueList);
				}
				valueList.add(value);
			}
		}

		// 求平均值
		for (String key : values.keySet()) {
			List<Double> valueList = values.get(key);
			double sum = 0;
			for (Double value : valueList) {
				sum = sum + value;
			}
			double value = sum / valueList.size();
			// 存入当前对象
			this.mergedNodeRslt.getMergedOpTRslt().putRslt(key, value);
		}
	}

	public NodeRslt getMergedNodeRslt() {
		return mergedNodeRslt;
	}

}
