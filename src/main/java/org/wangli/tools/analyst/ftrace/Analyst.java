package org.wangli.tools.analyst.ftrace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wangli.tools.analyst.ftrace.entity.ExecRslt;
import org.wangli.tools.analyst.ftrace.entity.FinalFuncItem;
import org.wangli.tools.analyst.ftrace.entity.FuncItem;
import org.wangli.tools.analyst.ftrace.entity.NodeRslt;
import org.wangli.tools.analyst.log.Logger;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class Analyst {

	// 根目录，其下每一个子目录是一个执行
	private static String baseDirPath = "F:\\wangli1\\tmp\\ftrace_3";

	private static final String lastDir = "trace_stat";

	// 结果文件的正则表达式
	private static final Pattern rsltFilePtn = Pattern.compile("\\s*(function[0-9]+)\\s*");

	// 统计信息正则表达式1
	private static Pattern item1Ptn = Pattern
			.compile("\\s*([0-9a-zA-Z_]+)\\s+([0-9]+)\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s*");

	// 统计信息正则表达式2
	private static Pattern item2Ptn = Pattern
			.compile("\\s+([0-9]+)\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s*");

	public static void main(String[] args) {
		Logger.logSysInfo(Analyst.class, "Start to count ftrace...");
		try {
			File baseDirFile = new File(baseDirPath);
			// 所有执行目录
			File[] execDirFiles = baseDirFile.listFiles();
			// 处理每一个执行结果
			for (File execDirFile : execDirFiles) {
				if (execDirFile.isDirectory()) {
					// 本次执行结果
					ExecRslt execRslt = new ExecRslt();

					// 所有节点目录
					File[] nodeDirFiles = execDirFile.listFiles();

					// 处理每一个节点结果
					for (File nodeDirFile : nodeDirFiles) {
						if (nodeDirFile.isDirectory()) {
							// 当前节点结果
							NodeRslt nodeRslt = new NodeRslt();
							// 添加到当前执行结果
							execRslt.putNodeRslt(nodeDirFile.getName(), nodeRslt);

							// 所有执行次目录
							File[] execTDirFiles = nodeDirFile.listFiles();
							// 处理每一个执行次结果
							for (File execTDirFile : execTDirFiles) {
								if (execTDirFile.isDirectory()) {
									// 结果文件所在路径
									File rsltDirFile = new File(
											execTDirFile.getAbsolutePath() + File.separator + lastDir);
									// 所有结果文件
									File[] rsltFiles = rsltDirFile.listFiles();
									// 处理每一个结果文件
									for (File rsltFile : rsltFiles) {
										Matcher rsltFileMt = rsltFilePtn.matcher(rsltFile.getName());
										if (rsltFile.isFile() && rsltFileMt.matches()) {
											/*
											 * 读取文件
											 */
											BufferedReader br = null;
											try {
												br = new BufferedReader(new FileReader(rsltFile));

												// 记录最近一次的函数名
												String lastFuncName = null;

												/*
												 * 处理每一行
												 */
												String line = br.readLine();
												while (line != null) {
													Matcher item1Mt = item1Ptn.matcher(line);
													Matcher item2Mt = item2Ptn.matcher(line);
													FuncItem funcItem = null;

													if (item1Mt.matches()) {
														String funcName = item1Mt.group(1);
														String hit = item1Mt.group(2);
														String time = item1Mt.group(3);

														funcItem = new FuncItem();
														funcItem.setFuncName(funcName);
														funcItem.setHit(Double.valueOf(hit));
														funcItem.setTime(Double.valueOf(time));

														// 记下本次函数名
														lastFuncName = funcName;
													} else if (item2Mt.matches()) {
														String funcName = lastFuncName;
														String hit = item2Mt.group(1);
														String time = item2Mt.group(2);

														funcItem = new FuncItem();
														funcItem.setFuncName(funcName);
														funcItem.setHit(Double.valueOf(hit));
														funcItem.setTime(Double.valueOf(time));
													}

													if (funcItem != null) {
														nodeRslt.addFuncItem(funcItem.getFuncName(), funcItem);
													}

													// 读取下一行
													line = br.readLine();
												}
											} catch (Exception e) {
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

					// 存放最终结果
					Map<String, FinalFuncItem> finalFuncItems = new HashMap<String, FinalFuncItem>();

					/*
					 * 计算各节点每个FuncItem的hit、time之和
					 */
					// 存放所有节点hit的和
					Map<String, Double> hitSums = new HashMap<String, Double>();
					// 存放所有节点time的和
					Map<String, Double> timeSums = new HashMap<String, Double>();

					Set<String> nodeKeys = execRslt.getKeys();
					for (String nodeKey : nodeKeys) {
						NodeRslt nodeRslt = execRslt.getNodeRslt(nodeKey);
						Set<String> itemKeys = nodeRslt.getKeys();
						for (String itemKey : itemKeys) {
							FuncItem funcItem = nodeRslt.getFuncItem(itemKey);

							// hit sum
							Double hitSum = hitSums.get(itemKey);
							if (hitSum == null) {
								hitSum = new Double(funcItem.getHitSum());
							} else {
								hitSum = hitSum + funcItem.getHitSum();
							}
							hitSums.put(itemKey, hitSum);

							// time sum
							Double timeSum = timeSums.get(itemKey);
							if (timeSum == null) {
								timeSum = new Double(funcItem.getTimeSum());
							} else {
								timeSum = timeSum + funcItem.getTimeSum();
							}
							timeSums.put(itemKey, timeSum);
						}
					}

					/*
					 * 每个节点的每个FuncItem加权
					 */
					for (String nodeKey : nodeKeys) {
						NodeRslt nodeRslt = execRslt.getNodeRslt(nodeKey);
						Set<String> itemKeys = nodeRslt.getKeys();
						for (String itemKey : itemKeys) {
							FuncItem funcItem = nodeRslt.getFuncItem(itemKey);
							funcItem.doAverage();

							double hitWeight = funcItem.getHitSum() / hitSums.get(itemKey);
							double timeWeight = funcItem.getTimeSum() / timeSums.get(itemKey);

							double hit = funcItem.getHit() * hitWeight;
							double time = funcItem.getTime() * timeWeight;

							FinalFuncItem finalFuncItem = new FinalFuncItem();
							finalFuncItem.setHit(hit);
							finalFuncItem.setTime(time);

							FinalFuncItem existedItem = finalFuncItems.get(itemKey);
							if (existedItem == null) {
								finalFuncItems.put(itemKey, finalFuncItem);
							} else {
								existedItem.mergeFinalFuncItem(finalFuncItem);
							}
						}
					}

					/*
					 * 写Excel文件
					 */
					String excelPath = execDirFile.getAbsolutePath() + File.separator + "funcitems.xls";
					OutputStream os = null;
					try {
						Logger.logSysInfo(Analyst.class, "Write function items to excel...");

						/*
						 * 创建excel文件
						 */
						os = new FileOutputStream(excelPath);
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

						// Function name.
						Label name_lb = new Label(colId++, rowId, "Name", colTitleFormat);
						wsh.addCell(name_lb);

						// Hit
						Label hit_lb = new Label(colId++, rowId, "Hit", colTitleFormat);
						wsh.addCell(hit_lb);

						// Time
						Label time_lb = new Label(colId++, rowId, "Time", colTitleFormat);
						wsh.addCell(time_lb);

						/*
						 * 写内容
						 */
						// 遍历
						Set<String> keys = finalFuncItems.keySet();
						for (String key : keys) {
							FinalFuncItem finalFuncItem = finalFuncItems.get(key);

							colId = 0;
							rowId++;

							// Name
							WritableCell name_cell = new Label(colId++, rowId, key);
							wsh.addCell(name_cell);

							// Hit
							Number hit_cell = new Number(colId++, rowId, finalFuncItem.getHit());
							wsh.addCell(hit_cell);

							// Time
							Number time_cell = new Number(colId++, rowId, finalFuncItem.getTime());
							wsh.addCell(time_cell);
						}

						/*
						 * excel写出
						 */
						wwb.write();
						wwb.close();

						Logger.logSysInfo(Analyst.class, "Write function items to excel...Done.");
					} catch (Exception e) {
						Logger.logErrInfo(Analyst.class,
								"Error while writing function items to excel " + excelPath + ".", e);
						throw e;
					} finally {
						if (os != null) {
							os.close();
						}
					}
				}
			}

			Logger.logSysInfo(Analyst.class, "End to count ftrace.");
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while counting ftrace.", e);
		}
	}

}
