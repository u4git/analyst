package org.wangli.tools.analyst.entity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.wangli.tools.analyst.pagewalk.entity.OpTRslt;

public class OpTRsltTest {

	@Test
	public void testMergeOpTRslts() {
		/*
		 * 准备数据
		 */
		List<OpTRslt> opTRslts = new LinkedList<OpTRslt>();

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
			opTRslts.add(opTRslt);
			showOpTRslt(opTRslt, String.valueOf(i));
		}

		/*
		 * 合并
		 */
		OpTRslt mergedOpTRslt = null;
		for (OpTRslt opTRslt : opTRslts) {
			if (mergedOpTRslt == null) {
				mergedOpTRslt = opTRslt;
			} else {
				mergedOpTRslt.addOpTRslt4Merge(opTRslt);
			}
		}
		mergedOpTRslt.doMergeOpTRslts();

		/*
		 * 显示结果
		 */
		showOpTRslt(mergedOpTRslt, "mergedOpTRslt");
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
