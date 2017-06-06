package org.wangli.tools.analyst.pagewalk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wangli.tools.analyst.log.Logger;
import org.wangli.tools.analyst.pagewalk.entity.ExecTRslt;
import org.wangli.tools.analyst.pagewalk.entity.NodeRslt;
import org.wangli.tools.analyst.pagewalk.entity.OpTRslt;
import org.wangli.tools.analyst.pagewalk.entity.ParamRslt;
import org.wangli.tools.analyst.pagewalk.entity.WorkloadRslt;
import org.wangli.tools.analyst.pagewalk.entity.WorkloadRsltSet;
import org.wangli.tools.analyst.pagewalk.util.WorkloadRsltSetUtil;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class Analyst {

	// 根目录，其下每一个子目录是一个Execution
	private static String basePath = "F:\\wangli1\\experiment2\\pagewalk_3";

	// 执行名称的正则表达式
	private static Pattern execNamePtn = Pattern.compile(
			"([a-zA-Z0-9]+)_([a-zA-Z0-9]+)_thp=(always|madvise|never)_hps=(4k|2m|1g)_numa=(on|off)_numabala=(0|1)");
	private static int execNamePtn_gid_wl = 1;
	private static int execNamePtn_gid_thp = 3;
	private static int execNamePtn_gid_hps = 4;

	// 文件名正则表达式
	private static Pattern fileNamePtn = Pattern.compile("start_pagewalk__(.+)__(.+)");
	private static int fileNamePtn_gid_nodeName = 1;
	private static int fileNamePtn_gid_opTime = 2;

	// 内容正则表达式
	private static Pattern keyValuePtn = Pattern.compile("\\s*([0-9,]+)\\s+([a-zA-Z0-9:]+)\\s+(\\([0-9\\.%]+\\))\\s*");
	private static int keyValuePtn_gid_key = 2;
	private static int keyValuePtn_gid_value = 1;

	// 文本中key到展示key的映射
	private static Map<String, String> sKey2oKey = new HashMap<String, String>();

	static {
		sKey2oKey.put("DTLB_STORE_WALK_DURATION_K", "r0449:k");
		sKey2oKey.put("DTLB_LOAD_WALK_DURATION_K", "r0408:k");
		sKey2oKey.put("ITLB_WALK_DURATION_K", "r0485:k");
		sKey2oKey.put("DTLB_STORE_WALK_DURATION_U", "r0449:u");
		sKey2oKey.put("DTLB_LOAD_WALK_DURATION_U", "r0408:u");
		sKey2oKey.put("ITLB_WALK_DURATION_U", "r0485:u");
		sKey2oKey.put("DTLB_STORE_WALK_COMPLETED_K", "r0249:k");
		sKey2oKey.put("DTLB_LOAD_WALK_COMPLETED_K", "r0208:k");
		sKey2oKey.put("ITLB_WALK_COMPLETED_K", "r0285:k");
		sKey2oKey.put("DTLB_STORE_WALK_COMPLETED_U", "r0249:u");
		sKey2oKey.put("DTLB_LOAD_WALK_COMPLETED_U", "r0208:u");
		sKey2oKey.put("ITLB_WALK_COMPLETED_U", "r0285:u");
		sKey2oKey.put("CPU_CLK_UNHALTED", "r003c");
		sKey2oKey.put("INST_RETIRED", "r00c0");
	}

	public static void main(String[] args) {
		try {
			Logger.logSysInfo(Analyst.class, "Start to count page walks...");

			// 记录所有负载的结果
			WorkloadRsltSet workloadRslts = new WorkloadRsltSet();

			File baseDir = new File(basePath);
			File[] execDirs = baseDir.listFiles();
			for (int i = 0; i < execDirs.length; ++i) {
				File execDir = execDirs[i];
				if (execDir.isDirectory()) {
					// 执行名称
					String execName = execDir.getName();
					Matcher execNameMt = execNamePtn.matcher(execName);
					if (!execNameMt.matches()) {
						throw new Exception("Execution name " + execName + " is illegal.");
					}

					// 负载名称
					String workloadName = execNameMt.group(execNamePtn_gid_wl);
					// THP
					String thpEnabled = execNameMt.group(execNamePtn_gid_thp);
					// 页大小
					String pageSize = execNameMt.group(execNamePtn_gid_hps);
					if ("never".equalsIgnoreCase(thpEnabled)) {
						pageSize = "4K";
					}

					// 当前负载结果
					WorkloadRslt workloadRslt = workloadRslts.getWorkloadRslt(workloadName);
					if (workloadRslt == null) {
						workloadRslt = new WorkloadRslt();
						workloadRslts.putWorkloadRslt(workloadName, workloadRslt);
					}

					// 当前参数结果
					ParamRslt paramRslt = new ParamRslt();
					workloadRslt.putParamRslt(pageSize, paramRslt);

					/*
					 * 遍历当前Execution的每一次执行
					 */
					File[] execTDirs = execDir.listFiles();
					for (int j = 0; j < execTDirs.length; ++j) {
						File execTDir = execTDirs[j];
						if (execTDir.isDirectory()) {
							// 本次执行结果
							ExecTRslt execTRslt = new ExecTRslt();
							paramRslt.putExecTRslt(execTDir.getName(), execTRslt);

							/*
							 * 遍历每一个文件
							 */
							File[] rsltFiles = execTDir.listFiles();
							for (int k = 0; k < rsltFiles.length; ++k) {
								File rsltFile = rsltFiles[k];
								Matcher fileNameMt = fileNamePtn.matcher(rsltFile.getName());
								if (rsltFile.isFile() && fileNameMt.matches()) {
									// 节点名称
									String nodeName = fileNameMt.group(fileNamePtn_gid_nodeName);
									// 操作执行次名称
									String opTime = fileNameMt.group(fileNamePtn_gid_opTime);

									// 节点结果
									NodeRslt nodeRslt = execTRslt.getNodeRslt(nodeName);
									if (nodeRslt == null) {
										nodeRslt = new NodeRslt();
										execTRslt.putNodeRslt(nodeName, nodeRslt);
									}

									// 操作执行次结果
									OpTRslt opTRslt = new OpTRslt();
									nodeRslt.putOpTRslt(opTime, opTRslt);

									/*
									 * 逐行读取文件
									 */
									BufferedReader br = null;
									try {
										br = new BufferedReader(new FileReader(rsltFile));
										String line = br.readLine();
										while (line != null) {
											Matcher keyValueMt = keyValuePtn.matcher(line);

											if (keyValueMt.matches()) {
												// Key
												String key = keyValueMt.group(keyValuePtn_gid_key);
												// Value（包含逗号）
												String value_s_com = keyValueMt.group(keyValuePtn_gid_value);
												// Value（不包含逗号）
												String value_s = value_s_com.replaceAll(",", "");

												Double value = new Double(value_s);
												opTRslt.putRslt(key, value);
											}

											// 读取下一行
											line = br.readLine();
										}
									} catch (Exception e) {
										Logger.logErrInfo(Analyst.class,
												"Error while reading " + rsltFile.getAbsolutePath() + ".", e);
										throw e;
									} finally {
										if (br != null) {
											br.close();
										}
									}
								}
							}
						}
					}
				}
			}

			String pagewalksInitialPath = baseDir + File.separator + "pagewalks_initial.xls";
			WorkloadRsltSetUtil.showWorkloadRsltSet(workloadRslts, pagewalksInitialPath);

			/*
			 * 计算最终结果（取平均，或加权）
			 */
			workloadRslts.merge();

			/*
			 * 写负载结果到Excel
			 */
			String pagewalksPath = baseDir + File.separator + "pagewalks.xls";
			OutputStream os = null;
			try {
				Logger.logSysInfo(Analyst.class, "Write page walks to excel...");

				/*
				 * 创建excel文件
				 */
				os = new FileOutputStream(pagewalksPath);
				WritableWorkbook wwb = Workbook.createWorkbook(os);

				/*
				 * 创建sheet
				 */
				WritableSheet wsh = wwb.createSheet("Sheet0", 0);

				/*
				 * 写列标题
				 */
				// 列号
				int colId = 0;
				// 行号
				int rowId = 0;

				WritableFont colTitleFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
				WritableCellFormat colTitleFormat = new WritableCellFormat(colTitleFont);

				// Workload
				wsh.mergeCells(colId, rowId, colId + 1, rowId);
				Label wl_Lb = new Label(colId++, rowId, "Workload", colTitleFormat);
				wsh.addCell(wl_Lb);
				colId++;

				// DTLB_STORE_WALK_DURATION_K
				Label ds_wd_k_lb = new Label(colId++, rowId, "DTLB_STORE_WALK_DURATION_K", colTitleFormat);
				wsh.addCell(ds_wd_k_lb);

				// DTLB_LOAD_WALK_DURATION_K
				Label dl_wd_k_lb = new Label(colId++, rowId, "DTLB_LOAD_WALK_DURATION_K", colTitleFormat);
				wsh.addCell(dl_wd_k_lb);

				// ITLB_WALK_DURATION_K
				Label i_wd_k_lb = new Label(colId++, rowId, "ITLB_WALK_DURATION_K", colTitleFormat);
				wsh.addCell(i_wd_k_lb);

				// DTLB_STORE_WALK_DURATION_U
				Label ds_wd_u_lb = new Label(colId++, rowId, "DTLB_STORE_WALK_DURATION_U", colTitleFormat);
				wsh.addCell(ds_wd_u_lb);

				// DTLB_LOAD_WALK_DURATION_U
				Label dl_wd_u_lb = new Label(colId++, rowId, "DTLB_LOAD_WALK_DURATION_U", colTitleFormat);
				wsh.addCell(dl_wd_u_lb);

				// ITLB_WALK_DURATION_U
				Label i_wd_u_lb = new Label(colId++, rowId, "ITLB_WALK_DURATION_U", colTitleFormat);
				wsh.addCell(i_wd_u_lb);

				// DTLB_STORE_WALK_COMPLETED_K
				Label ds_wc_k_lb = new Label(colId++, rowId, "DTLB_STORE_WALK_COMPLETED_K", colTitleFormat);
				wsh.addCell(ds_wc_k_lb);

				// DTLB_LOAD_WALK_COMPLETED_K
				Label dl_wc_k_lb = new Label(colId++, rowId, "DTLB_LOAD_WALK_COMPLETED_K", colTitleFormat);
				wsh.addCell(dl_wc_k_lb);

				// ITLB_WALK_COMPLETED_K
				Label i_wc_k_lb = new Label(colId++, rowId, "ITLB_WALK_COMPLETED_K", colTitleFormat);
				wsh.addCell(i_wc_k_lb);

				// DTLB_STORE_WALK_COMPLETED_U
				Label ds_wc_u_lb = new Label(colId++, rowId, "DTLB_STORE_WALK_COMPLETED_U", colTitleFormat);
				wsh.addCell(ds_wc_u_lb);

				// DTLB_LOAD_WALK_COMPLETED_U
				Label dl_wc_u_lb = new Label(colId++, rowId, "DTLB_LOAD_WALK_COMPLETED_U", colTitleFormat);
				wsh.addCell(dl_wc_u_lb);

				// ITLB_WALK_COMPLETED_U
				Label i_wc_u_lb = new Label(colId++, rowId, "ITLB_WALK_COMPLETED_U", colTitleFormat);
				wsh.addCell(i_wc_u_lb);

				// CPU_CLK_UNHALTED
				Label ccu_lb = new Label(colId++, rowId, "CPU_CLK_UNHALTED", colTitleFormat);
				wsh.addCell(ccu_lb);

				// INST_RETIRED
				Label ir_lb = new Label(colId++, rowId, "INST_RETIRED", colTitleFormat);
				wsh.addCell(ir_lb);

				/*
				 * 写内容
				 */
				List<String> workloadNames = workloadRslts.getKeys();
				// 排序
				Collections.sort(workloadNames);
				// 遍历
				for (String workloadName : workloadNames) {
					// 负载结果
					WorkloadRslt workloadRslt = workloadRslts.getWorkloadRslt(workloadName);

					colId = 0;
					rowId++;

					List<String> params = workloadRslt.getKeys();

					// 负载名称
					wsh.mergeCells(colId, rowId, colId, rowId + params.size() - 1);
					WritableCell wlNameLab = new Label(colId++, rowId, workloadName);
					wsh.addCell(wlNameLab);

					// 排序
					Collections.sort(params);
					Collections.reverse(params);
					// 遍历
					for (String param : params) {
						// 当前参数结果
						ParamRslt paramRslt = workloadRslt.getParamRslt(param);

						// 当前合并后的结果
						OpTRslt opTRslt = paramRslt.getMergedExecTRslt().getMergedNodeRslt().getMergedOpTRslt();

						colId = 1;

						// 页大小
						WritableCell pagesizeCell = new Label(colId++, rowId, param);
						wsh.addCell(pagesizeCell);

						// DTLB_STORE_WALK_DURATION_K
						String key_ds_wd_k = sKey2oKey.get("DTLB_STORE_WALK_DURATION_K");
						double value_ds_wd_k = opTRslt.getRslt(key_ds_wd_k);
						Number ds_wd_k = new Number(colId++, rowId, value_ds_wd_k);
						wsh.addCell(ds_wd_k);

						// DTLB_LOAD_WALK_DURATION_K
						String key_dl_wd_k = sKey2oKey.get("DTLB_LOAD_WALK_DURATION_K");
						double value_dl_wd_k = opTRslt.getRslt(key_dl_wd_k);
						Number dl_wd_k = new Number(colId++, rowId, value_dl_wd_k);
						wsh.addCell(dl_wd_k);

						// ITLB_WALK_DURATION_K
						String key_i_wd_k = sKey2oKey.get("ITLB_WALK_DURATION_K");
						double value_i_wd_k = opTRslt.getRslt(key_i_wd_k);
						Number i_wd_k = new Number(colId++, rowId, value_i_wd_k);
						wsh.addCell(i_wd_k);

						// DTLB_STORE_WALK_DURATION_U
						String key_ds_wd_u = sKey2oKey.get("DTLB_STORE_WALK_DURATION_U");
						double value_ds_wd_u = opTRslt.getRslt(key_ds_wd_u);
						Number ds_wd_u = new Number(colId++, rowId, value_ds_wd_u);
						wsh.addCell(ds_wd_u);

						// DTLB_LOAD_WALK_DURATION_U
						String key_dl_wd_u = sKey2oKey.get("DTLB_LOAD_WALK_DURATION_U");
						double value_dl_wd_u = opTRslt.getRslt(key_dl_wd_u);
						Number dl_wd_u = new Number(colId++, rowId, value_dl_wd_u);
						wsh.addCell(dl_wd_u);

						// ITLB_WALK_DURATION_U
						String key_i_wd_u = sKey2oKey.get("ITLB_WALK_DURATION_U");
						double value_i_wd_u = opTRslt.getRslt(key_i_wd_u);
						Number i_wd_u = new Number(colId++, rowId, value_i_wd_u);
						wsh.addCell(i_wd_u);

						// DTLB_STORE_WALK_COMPLETED_K
						String key_ds_wc_k = sKey2oKey.get("DTLB_STORE_WALK_COMPLETED_K");
						double value_ds_wc_k = opTRslt.getRslt(key_ds_wc_k);
						Number ds_wc_k = new Number(colId++, rowId, value_ds_wc_k);
						wsh.addCell(ds_wc_k);

						// DTLB_LOAD_WALK_COMPLETED_K
						String key_dl_wc_k = sKey2oKey.get("DTLB_LOAD_WALK_COMPLETED_K");
						double value_dl_wc_k = opTRslt.getRslt(key_dl_wc_k);
						Number dl_wc_k = new Number(colId++, rowId, value_dl_wc_k);
						wsh.addCell(dl_wc_k);

						// ITLB_WALK_COMPLETED_K
						String key_i_wc_k = sKey2oKey.get("ITLB_WALK_COMPLETED_K");
						double value_i_wc_k = opTRslt.getRslt(key_i_wc_k);
						Number i_wc_k = new Number(colId++, rowId, value_i_wc_k);
						wsh.addCell(i_wc_k);

						// DTLB_STORE_WALK_COMPLETED_U
						String key_ds_wc_u = sKey2oKey.get("DTLB_STORE_WALK_COMPLETED_U");
						double value_ds_wc_u = opTRslt.getRslt(key_ds_wc_u);
						Number ds_wc_u = new Number(colId++, rowId, value_ds_wc_u);
						wsh.addCell(ds_wc_u);

						// DTLB_LOAD_WALK_COMPLETED_U
						String key_dl_wc_u = sKey2oKey.get("DTLB_LOAD_WALK_COMPLETED_U");
						double value_dl_wc_u = opTRslt.getRslt(key_dl_wc_u);
						Number dl_wc_u = new Number(colId++, rowId, value_dl_wc_u);
						wsh.addCell(dl_wc_u);

						// ITLB_WALK_COMPLETED_U
						String key_i_wc_u = sKey2oKey.get("ITLB_WALK_COMPLETED_U");
						double value_i_wc_u = opTRslt.getRslt(key_i_wc_u);
						Number i_wc_u = new Number(colId++, rowId, value_i_wc_u);
						wsh.addCell(i_wc_u);

						// CPU_CLK_UNHALTED
						String key_ccu = sKey2oKey.get("CPU_CLK_UNHALTED");
						double value_ccu = opTRslt.getRslt(key_ccu);
						Number ccu = new Number(colId++, rowId, value_ccu);
						wsh.addCell(ccu);

						// INST_RETIRED
						String key_ir = sKey2oKey.get("INST_RETIRED");
						double value_ir = opTRslt.getRslt(key_ir);
						Number ir = new Number(colId++, rowId, value_ir);
						wsh.addCell(ir);

						rowId++;
					}
				}

				/*
				 * excel写出
				 */
				wwb.write();
				wwb.close();

				Logger.logSysInfo(Analyst.class, "Write page walks to excel...Finished.");
			} catch (Exception e) {
				Logger.logErrInfo(Analyst.class, "Error while writing page walks to excel " + pagewalksPath + ".", e);
				throw e;
			} finally {
				if (os != null) {
					os.close();
				}
			}

			Logger.logSysInfo(Analyst.class, "End to count page walks.");
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while counting page walks.", e);
		}
	}

}
