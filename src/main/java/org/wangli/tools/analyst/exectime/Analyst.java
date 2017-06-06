package org.wangli.tools.analyst.exectime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wangli.tools.analyst.exectime.entity.ExecRslt;
import org.wangli.tools.analyst.exectime.entity.TimeRslt;
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

	// 根目录，其下每一个子目录为一个Execution结果
	private static String basePath = "F:\\wangli1\\experiment2\\exectime_1";

	// Execution文件的正则表达式
	private static Pattern rsltNamePtn = Pattern.compile("run_.+__.+__.+");

	// 匹配文件有异常的正则表达式
	private static Pattern err1Ptn = Pattern.compile("\\s*Exception in thread\\s+\\\"\\s*[a-zA-Z0-9]+\\s*\\\"\\s+.*");
	private static Pattern err2Ptn = Pattern.compile("\\s+at\\s+[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)+.*");
	private static Pattern err3Ptn = Pattern
			.compile(".*ERROR TaskSchedulerImpl: Lost executor [0-9]+ on .*: worker lost.*");
	private static Pattern err4Ptn = Pattern.compile("\\s*=====End with error.=====\\s*");

	// 执行时间的正则表达式
	private static Pattern execTimePtn = Pattern.compile("\\s*real\\s+([0-9]+)m([0-9]+)\\.([0-9]+)s\\s*");
	private static int execTimePtn_gid_m = 1;
	private static int execTimePtn_gid_s = 2;
	private static int execTimePtn_gid_ms = 3;

	public static void main(String[] args) {

		try {
			Logger.logSysInfo(Analyst.class, "Start to count execution time...");

			// 记录有错的文件路径
			List<String> errFilePaths = new LinkedList<String>();

			// 记录每个Execution的结果
			Map<String, ExecRslt> execRslts = new HashMap<String, ExecRslt>();

			/*
			 * 遍历每一个Execution目录
			 */
			Logger.logSysInfo(Analyst.class, "Count execution time...");

			File baseDir = new File(basePath);
			File[] execDirs = baseDir.listFiles();
			for (int i = 0; i < execDirs.length; ++i) {
				File execDir = execDirs[i];
				if (execDir.isDirectory()) {
					// 拿到结果项
					ExecRslt execRslt = execRslts.get(execDir.getName());
					if (execRslt == null) {
						// 向结果中添加一项
						execRslt = new ExecRslt();
						execRslts.put(execDir.getName(), execRslt);
					}

					/*
					 * 遍历当前Execution的每一次执行
					 */
					File[] execTDirs = execDir.listFiles();
					for (int j = 0; j < execTDirs.length; ++j) {
						File execTDir = execTDirs[j];
						if (execTDir.isDirectory()) {
							/*
							 * 遍历每一个文件
							 */
							File[] rslts = execTDir.listFiles();
							for (int k = 0; k < rslts.length; ++k) {
								File rsltFile = rslts[k];
								Matcher rsltNameMt = rsltNamePtn.matcher(rsltFile.getName());
								if (rsltFile.isFile() && rsltNameMt.matches()) {
									/*
									 * 逐行读取文件
									 */
									BufferedReader br = null;
									try {
										br = new BufferedReader(new FileReader(rsltFile));
										String line = br.readLine();
										while (line != null) {
											// 检查是否有错
											Matcher err1Mt = err1Ptn.matcher(line);
											Matcher err2Mt = err2Ptn.matcher(line);
											Matcher err3Mt = err3Ptn.matcher(line);
											Matcher err4Mt = err4Ptn.matcher(line);
											if (err1Mt.matches() || err2Mt.matches() || err3Mt.matches()
													|| err4Mt.matches()) {
												// 记下有错的文件路径
												errFilePaths.add(rsltFile.getAbsolutePath());
												// 错误执行次加1
												execRslt.addErrorTimes(1);
												// 退出该文件
												break;
											}

											// 找到执行时间一行
											Matcher execTimeMch = execTimePtn.matcher(line);
											if (execTimeMch.matches()) {
												// 分
												String m_s = execTimeMch.group(execTimePtn_gid_m);
												double m = Double.valueOf(m_s);

												// 秒
												String s_s = execTimeMch.group(execTimePtn_gid_s);
												double s = Double.valueOf(s_s);

												// 毫秒
												String ms_s = execTimeMch.group(execTimePtn_gid_ms);
												double ms = Double.valueOf(ms_s);

												// 执行时间
												double time = (m * 60 + s) * 1000 + ms;

												// 创建本次执行结果
												TimeRslt timeRslt = new TimeRslt();
												timeRslt.setCostTime(time);
												timeRslt.setCostTimeMinutes(m);
												timeRslt.setCostTimeSeconds(s);
												timeRslt.setCostTimeMs(ms);
												// 添加本次执行结果
												execRslt.addTimeRslt(timeRslt);

												// 默认一个文件只有一个执行时间
												break;
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

			Logger.logSysInfo(Analyst.class, "Count execution time...Finished");

			/*
			 * 写错误文件的结果
			 */
			if (errFilePaths.size() > 0) {
				String errRcdsPath = baseDir + File.separator + baseDir.getName() + "_errFilePaths";
				Logger.logSysInfo(Analyst.class, "Error(s) appeared in " + errFilePaths.size()
						+ " files. For Details to see " + errRcdsPath + ".");
				BufferedWriter bw = null;
				try {
					bw = new BufferedWriter(new FileWriter(errRcdsPath));
					for (int i = 0; i < errFilePaths.size(); ++i) {
						bw.write(errFilePaths.get(i));
						bw.newLine();
					}
				} catch (Exception e) {
					Logger.logErrInfo(Analyst.class,
							"Error while writing path(s) of file(s) with error(s) to " + errRcdsPath + ".", e);
					throw e;
				} finally {
					if (bw != null) {
						bw.close();
					}
				}
			}

			/*
			 * 将结果写道Excel
			 */
			String excelPath = baseDir + File.separator + baseDir.getName() + "_result.xls";
			OutputStream os = null;
			try {
				Logger.logSysInfo(Analyst.class, "Write result to excel...");

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
				// 标题列号
				int titleCol = 0;
				WritableFont colTitleFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
				WritableCellFormat colTitleFormat = new WritableCellFormat(colTitleFont);
				// Execution id
				Label execIdLb = new Label(titleCol++, 0, "ExecId", colTitleFormat);
				wsh.addCell(execIdLb);
				// 平均执行时间
				Label avgExecTimeLb = new Label(titleCol++, 0, "AvgExecTime", colTitleFormat);
				wsh.addCell(avgExecTimeLb);
				// 正确次数
				Label correctTimesLb = new Label(titleCol++, 0, "CorrectTimes", colTitleFormat);
				wsh.addCell(correctTimesLb);
				// 错误执行次数
				Label errorTimesLb = new Label(titleCol++, 0, "ErrorTimes", colTitleFormat);
				wsh.addCell(errorTimesLb);
				// 总的执行次数
				Label totalTimesLb = new Label(titleCol++, 0, "TotalTimes", colTitleFormat);
				wsh.addCell(totalTimesLb);
				// 出错率
				Label errorRatioLb = new Label(titleCol++, 0, "ErrorRatio", colTitleFormat);
				wsh.addCell(errorRatioLb);

				Set<String> execNames = execRslts.keySet();
				int row = 0;
				for (String execName : execNames) {
					ExecRslt execRslt = execRslts.get(execName);
					// 行号
					++row;
					// 列号
					int col = 0;
					// Execution id
					WritableCell execId_n = new Label(col++, row, execName);
					wsh.addCell(execId_n);
					// 平均执行时间
					Number avgExecTime_n = new Number(col++, row, execRslt.getAvgExecTime());
					wsh.addCell(avgExecTime_n);
					// 正确次数
					Number correctTimes_n = new Number(col++, row, execRslt.getCorrectTimes());
					wsh.addCell(correctTimes_n);
					// 错误执行次数
					Number errorTimes_n = new Number(col++, row, execRslt.getErrorTimes());
					wsh.addCell(errorTimes_n);
					// 总的执行次数
					Number totalTimes_n = new Number(col++, row, execRslt.getTotalTimes());
					wsh.addCell(totalTimes_n);
					// 出错率
					double totalTimes = execRslt.getTotalTimes();
					double errorTimes = execRslt.getErrorTimes();
					Number errorRatio = new Number(col++, row, errorTimes / totalTimes);
					wsh.addCell(errorRatio);
					// 每一次的执行结果
					while (execRslt.hasNextTimeRslt()) {
						TimeRslt timeRslt = execRslt.nextTimeRslt();
						// 当前次执行结果
						String timeRsltCost_s = timeRslt.getCostTimeMinutes() + "m" + timeRslt.getCostTimeSeconds()
								+ "s" + timeRslt.getCostTimeMs() + "ms";
						WritableCell timeRsltCost = new Label(col++, row, timeRsltCost_s);
						wsh.addCell(timeRsltCost);
					}
				}

				/*
				 * excel写出
				 */
				wwb.write();
				wwb.close();

				Logger.logSysInfo(Analyst.class, "Write result to excel...Finished.");
			} catch (Exception e) {
				Logger.logErrInfo(Analyst.class, "Error while writing result to excel " + excelPath + ".", e);
				throw e;
			} finally {
				if (os != null) {
					os.close();
				}
			}

			Logger.logSysInfo(Analyst.class, "End to count execution time.");
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while counting execution time.", e);
		}

	}

}
