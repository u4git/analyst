package org.wangli.tools.analyst.entity;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.wangli.tools.analyst.pagewalk.entity.NodeRslt;
import org.wangli.tools.analyst.pagewalk.entity.OpTRslt;

public class NodeRsltTest {

	@Test
	public void testMergeOpTRslts() {

		/*
		 * 准备数据
		 */
		NodeRslt nodeRslt = new NodeRslt();

		int numOpTRslts = 4;
		int numRslts = 3;

		String[] keys = new String[numRslts];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = "key_" + i;
		}

		for (int i = 0; i < numOpTRslts; i++) {
			OpTRslt opTRslt = new OpTRslt();
			for (int j = 0; j < keys.length; j++) {
				double value = i + j;
				opTRslt.putRslt(keys[j], value);
			}
			nodeRslt.putOpTRslt(String.valueOf(i), opTRslt);
			showOpTRslt(opTRslt, String.valueOf(i));
		}

		/*
		 * 合并
		 */
		nodeRslt.mergeOpTRslts();

		/*
		 * 显示合并后结果
		 */
		showOpTRslt(nodeRslt.getMergedOpTRslt(), "MergedOpTRslt");
	}

	private void showOpTRslt(OpTRslt opTRslt, String desc) {
		System.out.println("=== OpTRslt " + desc + " ===");
		List<String> keys = opTRslt.getKeys();
		Collections.sort(keys);
		for (String key : keys) {
			double value = opTRslt.getRslt(key);
			System.out.println("\t" + key + ", " + value);
		}
	}

}
