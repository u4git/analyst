package org.wangli.tools.analyst.pagewalk.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.wangli.tools.analyst.pagewalk.entity.ExecTRslt;
import org.wangli.tools.analyst.pagewalk.entity.NodeRslt;
import org.wangli.tools.analyst.pagewalk.entity.OpTRslt;
import org.wangli.tools.analyst.pagewalk.entity.ParamRslt;
import org.wangli.tools.analyst.pagewalk.entity.WorkloadRslt;
import org.wangli.tools.analyst.pagewalk.entity.WorkloadRsltSet;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class WorkloadRsltSetUtil {

	public static void showWorkloadRsltSet(WorkloadRsltSet workloadRsltSet, String excelPath) {
		OutputStream os = null;
		try {
			/*
			 * 创建excel文件
			 */
			os = new FileOutputStream(excelPath);
			WritableWorkbook wwb = Workbook.createWorkbook(os);

			/*
			 * 创建sheet
			 */
			WritableSheet wsh = wwb.createSheet("Sheet0", 0);

			// 行号
			int rowId = 0;

			WritableFont colTitleFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
			WritableCellFormat colTitleFormat = new WritableCellFormat(colTitleFont);

			List<String> workloadNames = workloadRsltSet.getKeys();
			Collections.sort(workloadNames);
			for (String workloadName : workloadNames) {
				int wlColId = 0;

				Label wl_lb = new Label(wlColId++, rowId++, workloadName, colTitleFormat);
				wsh.addCell(wl_lb);

				/*
				 * WorkloadRslt
				 */
				WorkloadRslt workloadRslt = workloadRsltSet.getWorkloadRslt(workloadName);
				List<String> params = workloadRslt.getKeys();
				Collections.sort(params);

				for (String param : params) {
					int paramColId = wlColId;

					Label param_lb = new Label(paramColId++, rowId++, param, colTitleFormat);
					wsh.addCell(param_lb);

					/*
					 * ParamRslt
					 */
					ParamRslt paramRslt = workloadRslt.getParamRslt(param);
					List<String> execTKeys = paramRslt.getKeys();
					Collections.sort(execTKeys);

					for (String execTKey : execTKeys) {
						int execTColId = paramColId;

						Label exect_lb = new Label(execTColId++, rowId++, execTKey, colTitleFormat);
						wsh.addCell(exect_lb);

						/*
						 * ExecTRslt
						 */
						ExecTRslt execTRslt = paramRslt.getExecTRslt(execTKey);
						List<String> nodeKeys = execTRslt.getKeys();
						Collections.sort(nodeKeys);

						for (String nodeKey : nodeKeys) {
							int nodeColId = execTColId;

							Label node_lb = new Label(nodeColId++, rowId++, nodeKey, colTitleFormat);
							wsh.addCell(node_lb);

							/*
							 * NodeRslt
							 */
							NodeRslt nodeRslt = execTRslt.getNodeRslt(nodeKey);
							List<String> opTKeys = nodeRslt.getKeys();
							Collections.sort(opTKeys);

							for (String opTKey : opTKeys) {
								int opTColId = nodeColId;

								Label opt_lb = new Label(opTColId++, rowId++, opTKey, colTitleFormat);
								wsh.addCell(opt_lb);

								/*
								 * OpTRslt
								 */
								OpTRslt opTRslt = nodeRslt.getOpTRslt(opTKey);
								List<String> keys = opTRslt.getKeys();
								Collections.sort(keys);
								for (String key : keys) {
									int valueColId = opTColId;

									// Key
									Label key_lb = new Label(valueColId++, rowId, key, colTitleFormat);
									wsh.addCell(key_lb);

									// Value
									Number value_num = new Number(valueColId++, rowId++, opTRslt.getRslt(key));
									wsh.addCell(value_num);
								}
							}
						}
					}
				}
			}

			/*
			 * excel写出
			 */
			wwb.write();
			wwb.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

}
