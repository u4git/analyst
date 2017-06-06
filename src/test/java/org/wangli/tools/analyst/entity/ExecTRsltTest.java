package org.wangli.tools.analyst.entity;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.wangli.tools.analyst.pagewalk.config.Const;
import org.wangli.tools.analyst.pagewalk.entity.ExecTRslt;
import org.wangli.tools.analyst.pagewalk.entity.NodeRslt;
import org.wangli.tools.analyst.pagewalk.entity.OpTRslt;

public class ExecTRsltTest {

	private static double level = 100d;

	@Test
	public void testMergeNodeRslts() {
		/*
		 * 准备数据
		 */
		ExecTRslt execTRslt = new ExecTRslt();

		// NodeRslt数量
		int numNodeRslts = 2;
		fillExecTRsltWithNodeRslts(execTRslt, numNodeRslts);

		/*
		 * 显示初始数据
		 */
		showExecTRslt(execTRslt);

		/*
		 * 合并
		 */
		execTRslt.mergeNodeRslts();

		/*
		 * 显示合并后数据
		 */
		System.out.println("=== Merged OpTRslt ===");
		OpTRslt opTRslt = execTRslt.getMergedNodeRslt().getMergedOpTRslt();
		List<String> keys = opTRslt.getKeys();
		for (String key : keys) {
			System.out.println(key + ", " + opTRslt.getRslt(key));
		}
	}

	private void fillExecTRsltWithNodeRslts(ExecTRslt execTRslt, int numNodeRslts) {
		for (int i = 0; i < numNodeRslts; i++) {
			NodeRslt nodeRslt = new NodeRslt();

			// OpTRslt数量
			int numOpTRslts = 4;
			fillNodeRsltWithOpTRslts(nodeRslt, numOpTRslts);

			execTRslt.putNodeRslt(String.valueOf(i), nodeRslt);
		}
	}

	private void fillNodeRsltWithOpTRslts(NodeRslt nodeRslt, int numOpTRslts) {
		for (int i = 0; i < numOpTRslts; i++) {
			OpTRslt opTRslt = new OpTRslt();

			// Value的数量
			int numValues = 3;
			fillOpTRsltWithValues(opTRslt, numValues);

			nodeRslt.putOpTRslt(String.valueOf(i), opTRslt);
		}
	}

	private void fillOpTRsltWithValues(OpTRslt opTRslt, int numValues) {
		Random random = new Random();
		for (int i = 0; i < numValues - 1; i++) {
			opTRslt.putRslt(String.valueOf(i), random.nextDouble() * level);
		}
		opTRslt.putRslt(Const.weightName, random.nextDouble() * level);
	}

	private void showExecTRslt(ExecTRslt execTRslt) {
		List<String> keys = execTRslt.getKeys();
		Collections.sort(keys);
		for (String key : keys) {
			NodeRslt nodeRslt = execTRslt.getNodeRslt(key);
			System.out.println("=== NodeRslt " + key + " ===");
			showNodeRslt(nodeRslt);
		}
	}

	private void showNodeRslt(NodeRslt nodeRslt) {
		List<String> keys = nodeRslt.getKeys();
		Collections.sort(keys);
		for (String key : keys) {
			OpTRslt opTRslt = nodeRslt.getOpTRslt(key);
			System.out.println("=== OpTRslt " + key + " ===");
			showOpTRslt(opTRslt);
		}
	}

	private void showOpTRslt(OpTRslt opTRslt) {
		List<String> keys = opTRslt.getKeys();
		Collections.sort(keys);
		for (String key : keys) {
			System.out.println(key + ", " + opTRslt.getRslt(key));
		}
	}

}
