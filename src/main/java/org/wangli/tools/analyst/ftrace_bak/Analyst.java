package org.wangli.tools.analyst.ftrace_bak;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wangli.tools.analyst.ftrace_bak.entity.FuncItem;
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

	// 放待分析的文件的目录
	private static String baseDirPath = "D:\\wangli\\experiment\\ftrace\\thp_lr_200w_3_md_ftrace_5\\rslt_ftrace";

	// 文件名正则表达式
	private static Pattern fileNamePattern = Pattern.compile("\\s*function[0-9]+\\s*");

	// 统计信息正则表达式1
	private static Pattern item1Pattern = Pattern
			.compile("\\s*([0-9a-zA-Z_]+)\\s+([0-9]+)\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s*");

	// 统计信息正则表达式2
	private static Pattern item2Pattern = Pattern
			.compile("\\s+([0-9]+)\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s+([0-9.]+)\\s*us\\s*");

	public static void main(String[] args) throws Exception {
		Logger.logSysInfo(Analyst.class, "Start to count " + baseDirPath + "...");

		try {
			File baseDirFile = new File(baseDirPath);
			// 所有执行目录
			File[] execDirs = baseDirFile.listFiles();
			// 每一个执行
			for (int i = 0; i < execDirs.length; ++i) {
				// 执行目录
				File execDir = execDirs[i];
				if (execDir.isDirectory()) {
					// 执行结果
					Map<String, FuncItem> execRslt = new HashMap<String, FuncItem>();
					// 所有执行次目录
					File[] timeDirs = execDir.listFiles();
					// 每一次执行
					for (int j = 0; j < timeDirs.length; ++j) {
						// 执行次目录
						File timeDir = timeDirs[j];
						if (timeDir.isDirectory()) {
							String ftracePath = timeDir.getAbsolutePath() + File.separator + "trace_stat";
							// 得到本次结果
							Logger.logSysInfo(Analyst.class, "Make time result " + ftracePath + "...");
							Map<String, FuncItem> timeRslt = mkTimeResult(ftracePath);
							Logger.logSysInfo(Analyst.class, "Make time result " + ftracePath + "...Done.");

							// 合并到执行结果中
							Logger.logSysInfo(Analyst.class, "Merge time result " + ftracePath + "...");
							mergeTimeRslt(execRslt, timeRslt);
							Logger.logSysInfo(Analyst.class, "Merge time result " + ftracePath + "...Done.");
						}
					}

					// 将执行结果写到excel
					String excelPath = baseDirPath + File.separator + "rslt_" + execDir.getName() + ".xls";
					write2Excel(excelPath, execRslt);
				}
			}
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while counting " + baseDirPath + ".", e);
			throw e;
		}

		Logger.logSysInfo(Analyst.class, "Finish to count " + baseDirPath + ".");
	}

	/**
	 * 获取结果
	 * 
	 * @param ftracePath
	 * @return
	 * @throws Exception
	 */
	private static Map<String, FuncItem> mkTimeResult(String ftracePath) throws Exception {
		Map<String, FuncItem> results = new HashMap<String, FuncItem>();

		File ftraceDirFile = new File(ftracePath);

		File[] files = ftraceDirFile.listFiles();
		for (int i = 0; i < files.length; ++i) {
			File file = files[i];
			if (file.isFile()) {
				Matcher fileNameMatcher = fileNamePattern.matcher(file.getName());
				if (fileNameMatcher.matches()) {
					/*
					 * 读取文件内容进行统计
					 */
					BufferedReader br = null;
					try {
						// 记录最近一次的函数名
						String lastFuncName = null;

						br = new BufferedReader(new FileReader(file.getAbsolutePath()));
						String line = br.readLine();
						while (line != null) {
							Matcher item1Matcher = item1Pattern.matcher(line);
							Matcher item2Matcher = item2Pattern.matcher(line);
							FuncItem funcItem = null;

							if (item1Matcher.matches()) {
								String funcName = item1Matcher.group(1);
								String hit = item1Matcher.group(2);
								String time = item1Matcher.group(3);
								String avg = item1Matcher.group(4);
								String s2 = item1Matcher.group(5);

								funcItem = new FuncItem();
								funcItem.setFuncName(funcName);
								funcItem.setHit(Double.valueOf(hit));
								funcItem.setTime(Double.valueOf(time));
								funcItem.setAvg(Double.valueOf(avg));
								funcItem.setS2(Double.valueOf(s2));

								// 记下本次函数名
								lastFuncName = funcName;
							} else if (item2Matcher.matches()) {
								String funcName = lastFuncName;
								String hit = item2Matcher.group(1);
								String time = item2Matcher.group(2);
								String avg = item2Matcher.group(3);
								String s2 = item2Matcher.group(4);

								funcItem = new FuncItem();
								funcItem.setFuncName(funcName);
								funcItem.setHit(Double.valueOf(hit));
								funcItem.setTime(Double.valueOf(time));
								funcItem.setAvg(Double.valueOf(avg));
								funcItem.setS2(Double.valueOf(s2));
							}

							if (funcItem != null) {
								FuncItem existedItem = results.get(funcItem.getFuncName());
								if (existedItem == null) {
									results.put(funcItem.getFuncName(), funcItem);
								} else {
									existedItem.merge(funcItem);
								}
							}

							line = br.readLine();
						}
					} catch (Exception e) {
						Logger.logErrInfo(Analyst.class, "Error while counting from " + file.getAbsolutePath() + ".",
								e);
						throw e;
					} finally {
						if (br != null) {
							br.close();
						}
					}
				}
			}
		}

		return results;
	}

	/**
	 * 将一次执行结果合并到整个执行结果中
	 * 
	 * @param execRslt
	 * @param timeRslt
	 * @throws Exception
	 */
	private static void mergeTimeRslt(Map<String, FuncItem> execRslt, Map<String, FuncItem> timeRslt) throws Exception {
		try {
			Set<String> keys = timeRslt.keySet();
			for (String key : keys) {
				FuncItem funcItem_time = timeRslt.get(key);
				FuncItem funcItem_exec = execRslt.get(key);
				if (funcItem_exec != null) {
					funcItem_exec.merge(funcItem_time);
				} else {
					execRslt.put(key, funcItem_time);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 将结果写到excel
	 * 
	 * @param excelPath
	 * @param results
	 * @throws Exception
	 */
	private static void write2Excel(String excelPath, Map<String, FuncItem> results) throws Exception {
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

			int rowNum = 0;
			int colNum = 0;

			/*
			 * 写列标题
			 */
			WritableFont colTitleFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
			WritableCellFormat colTitleFormat = new WritableCellFormat(colTitleFont);
			// 函数名
			Label functionLabel = new Label(colNum++, rowNum, "Function", colTitleFormat);
			wsh.addCell(functionLabel);
			// 调用次数
			Label hitLabel = new Label(colNum++, rowNum, "Hit", colTitleFormat);
			wsh.addCell(hitLabel);
			// 执行时间
			Label timeLabel = new Label(colNum++, rowNum, "Time", colTitleFormat);
			wsh.addCell(timeLabel);
			// 平均执行时间
			Label avgLabel = new Label(colNum++, rowNum, "Avg", colTitleFormat);
			wsh.addCell(avgLabel);
			// 标准差
			Label s2Label = new Label(colNum++, rowNum, "s^2", colTitleFormat);
			wsh.addCell(s2Label);

			/*
			 * 写数据
			 */
			Set<String> keys = results.keySet();
			Iterator<String> funcNames = keys.iterator();

			while (funcNames.hasNext()) {
				colNum = 0;
				rowNum++;

				String funcName = funcNames.next();
				FuncItem funcItem = results.get(funcName);
				// 取平均值
				funcItem.doAverage();
				// 函数名
				WritableCell functionCell = new Label(colNum++, rowNum, funcItem.getFuncName());
				wsh.addCell(functionCell);
				// 调用次数
				Number hitCell = new Number(colNum++, rowNum, funcItem.getHit());
				wsh.addCell(hitCell);
				// 执行时间
				Number timeCell = new Number(colNum++, rowNum, funcItem.getTime());
				wsh.addCell(timeCell);
				// 平均执行时间
				Number avgCell = new Number(colNum++, rowNum, funcItem.getAvg());
				wsh.addCell(avgCell);
				// 标准差
				Number s2Cell = new Number(colNum++, rowNum, funcItem.getS2());
				wsh.addCell(s2Cell);
			}

			/*
			 * excel写出
			 */
			wwb.write();
			wwb.close();
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while writing results to " + excelPath, e);
			throw e;
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

}
