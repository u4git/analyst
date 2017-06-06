package org.wangli.tools.analyst.pagewalk.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpTRslt {

	// 所有追踪数据
	private Map<String, Double> rslts = new HashMap<String, Double>();

	// 所有追踪数据的关键字（用于排序遍历）
	private List<String> keys = new LinkedList<String>();

	// 为合并累加的中间结果
	private Map<String, List<Double>> accRslts = new HashMap<String, List<Double>>();

	public void putRslt(String key, Double value) {
		this.rslts.put(key, value);
		if (!this.keys.contains(key)) {
			this.keys.add(key);
		}
	}

	public Double getRslt(String key) {
		return this.rslts.get(key);
	}

	public List<String> getKeys() {
		return this.keys;
	}

	public void addOpTRslt4Merge(OpTRslt opTRslt) {
		List<String> keys = opTRslt.getKeys();
		for (String key : keys) {
			Double value = opTRslt.getRslt(key);
			List<Double> valueList = this.accRslts.get(key);
			if (valueList == null) {
				valueList = new LinkedList<Double>();
				this.accRslts.put(key, valueList);
			}
			valueList.add(value);
		}
	}

	public void doMergeOpTRslts() {
		Set<String> keys = this.accRslts.keySet();
		for (String key : keys) {
			List<Double> valueList = this.accRslts.get(key);
			// 累加值
			double acc = 0;
			for (Double value : valueList) {
				acc = acc + value;
			}
			// 加上当前对象的值
			acc = acc + this.rslts.getOrDefault(key, Double.valueOf(0));
			// 算平均值
			acc = acc / (valueList.size() + 1);
			// 存入合并后的值
			this.putRslt(key, acc);
		}
	}

}
