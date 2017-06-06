package org.wangli.tools.analyst.perf;

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

import org.wangli.tools.analyst.log.Logger;
import org.wangli.tools.analyst.perf.entity.PerfRslt;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class Analyst {

	// 根目录，其下每一个子目录为一个Execution结果
	private static String basePath = "D:\\wangli\\experiment\\perf\\thp_lr_200w_3_md_perf_5";

	// 所读文件的正则表达式
	private static Pattern fileNamePattern = Pattern.compile("start_perf__.+__.+");

	// 读取内容正则表达式
	private static Pattern perfRsltPtn = Pattern.compile(
			"\\s*([0-9,]+)\\s+(dTLB-load-misses|dTLB-loads|dTLB-store-misses|dTLB-stores|iTLB-load-misses|iTLB-loads)\\s+.*\\s*");
	private static int gpId_value = 1;
	private static int gpId_key = 2;

	public static void main(String[] args) throws Exception {
		try {
			File baseDirFile = new File(basePath);
			// 所有执行目录
			File[] execDirs = baseDirFile.listFiles();
			// 每一个执行
			for (int i = 0; i < execDirs.length; ++i) {
				// 执行目录
				File execDir = execDirs[i];
				if (execDir.isDirectory()) {
					// 执行结果
					Map<String, PerfRslt> execRslt = new HashMap<String, PerfRslt>();
					// 所有执行次目录
					File[] timeDirs = execDir.listFiles();
					// 每一次执行
					for (int j = 0; j < timeDirs.length; ++j) {
						// 执行次目录
						File timeDir = timeDirs[j];
						if (timeDir.isDirectory()) {
							String ftracePath = timeDir.getAbsolutePath();
							// 得到本次结果
							Logger.logSysInfo(Analyst.class, "Make time result " + ftracePath + "...");
							Map<String, PerfRslt> timeRslt = mkTimeResult(ftracePath);
							Logger.logSysInfo(Analyst.class, "Make time result " + ftracePath + "...Done.");

							// 合并到执行结果中
							Logger.logSysInfo(Analyst.class, "Merge time result " + ftracePath + "...");
							mergeTimeRslt(execRslt, timeRslt);
							Logger.logSysInfo(Analyst.class, "Merge time result " + ftracePath + "...Done.");
						}
					}

					// 将执行结果写到excel
					String excelPath = basePath + File.separator + "rslt_" + execDir.getName() + ".xls";
					write2Excel(excelPath, execRslt);
				}
			}
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while counting " + basePath + ".", e);
			throw e;
		}

	}

	/**
	 * 获取结果
	 * 
	 * @param perfRsltPath
	 * @return
	 * @throws Exception
	 */
	private static Map<String, PerfRslt> mkTimeResult(String perfRsltPath) throws Exception {
		Map<String, PerfRslt> results = new HashMap<String, PerfRslt>();

		File ftraceDirFile = new File(perfRsltPath);

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
						br = new BufferedReader(new FileReader(file.getAbsolutePath()));
						String line = br.readLine();
						while (line != null) {
							Matcher perfRsltMatcher = perfRsltPtn.matcher(line);

							if (perfRsltMatcher.matches()) {
								String key = perfRsltMatcher.group(gpId_key);
								if (results.get(key) != null) {
									throw new Exception("The value is existed by key " + key + ".");
								}

								String value_s = perfRsltMatcher.group(gpId_value).replaceAll(",", "");
								long value = Long.valueOf(value_s);

								PerfRslt perfRslt = new PerfRslt();
								perfRslt.setName(key);
								perfRslt.setTimes(value);

								results.put(perfRslt.getName(), perfRslt);
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
	private static void mergeTimeRslt(Map<String, PerfRslt> execRslt, Map<String, PerfRslt> timeRslt) throws Exception {
		try {
			Set<String> keys = timeRslt.keySet();
			for (String key : keys) {
				PerfRslt perfRslt_time = timeRslt.get(key);
				PerfRslt perfRslt_exec = execRslt.get(key);
				if (perfRslt_exec != null) {
					perfRslt_exec.merge(perfRslt_time);
				} else {
					execRslt.put(key, perfRslt_time);
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
	private static void write2Excel(String excelPath, Map<String, PerfRslt> results) throws Exception {
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
			// 指标名
			Label nameLabel = new Label(colNum++, rowNum, "Name", colTitleFormat);
			wsh.addCell(nameLabel);
			// 指标值
			Label valueLabel = new Label(colNum++, rowNum, "Value", colTitleFormat);
			wsh.addCell(valueLabel);

			/*
			 * 写数据
			 */
			Set<String> keys = results.keySet();
			Iterator<String> names = keys.iterator();

			while (names.hasNext()) {
				colNum = 0;
				rowNum++;

				String name = names.next();
				PerfRslt perfRslt = results.get(name);

				// 指标名
				WritableCell nameCell = new Label(colNum++, rowNum, perfRslt.getName());
				wsh.addCell(nameCell);
				// 指标值
				Number valueCell = new Number(colNum++, rowNum, perfRslt.getTimes());
				wsh.addCell(valueCell);
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
