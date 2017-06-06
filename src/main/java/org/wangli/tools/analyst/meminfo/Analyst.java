package org.wangli.tools.analyst.meminfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wangli.tools.analyst.log.Logger;
import org.wangli.tools.analyst.meminfo.entity.ExecMeminfo;
import org.wangli.tools.analyst.meminfo.entity.WorkloadMeminfo;
import org.wangli.tools.analyst.meminfo.entity.WorkloadMeminfoSet;

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
	private static String basePath = "D:\\wangli\\experiment2\\meminfo_1";

	// 执行名称的正则表达式
	private static Pattern execNamePtn = Pattern.compile(
			"([a-zA-Z0-9]+)_([a-zA-Z0-9]+)_thp=(always|madvise|never)_hps=(4k|2m|1g)_numa=(on|off)_numabala=(0|1)");
	private static int execNamePtn_gid_wl = 1;
	private static int execNamePtn_gid_thp = 3;
	private static int execNamePtn_gid_hps = 4;

	// 文件名正则表达式
	private static Pattern fileNamePtn = Pattern.compile("start_meminfo__.+__.+");

	// 采样分隔符正则表达式
	private static Pattern sampleSepPtn = Pattern.compile("--------------------");

	// 内容正则表达式
	private static Pattern keyValuePtn = Pattern.compile("([a-zA-Z0-9_\\(\\)]+):\\s+([0-9\\.]+)($|\\s+kB$)");
	private static int keyValuePtn_gid_key = 1;
	private static int keyValuePtn_gid_value = 2;

	public static void main(String[] args) {
		try {
			Logger.logSysInfo(Analyst.class, "Start to count meminfo...");

			// 记录所有负载的结果
			WorkloadMeminfoSet workloadMeminfoSet = new WorkloadMeminfoSet();

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
					String hps = execNameMt.group(execNamePtn_gid_hps);
					if ("never".equalsIgnoreCase(thpEnabled)) {
						hps = "4K";
					}

					// 本次执行结果
					ExecMeminfo execMeminfo = new ExecMeminfo();
					// 当前负载结果
					WorkloadMeminfo workloadMeminfo = workloadMeminfoSet.getWorkloadMeminfo(workloadName);
					if (workloadMeminfo == null) {
						workloadMeminfo = new WorkloadMeminfo();
						workloadMeminfoSet.putWorkloadMeminfo(workloadName, workloadMeminfo);
					}
					// 将本次执行结果添加到负载结果中
					workloadMeminfo.putExecMeminfo(hps, execMeminfo);

					// 采样次数
					int sampleNum = 0;

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
							File[] rsltFiles = execTDir.listFiles();
							for (int k = 0; k < rsltFiles.length; ++k) {
								File rsltFile = rsltFiles[k];
								Matcher fileNameMt = fileNamePtn.matcher(rsltFile.getName());
								if (rsltFile.isFile() && fileNameMt.matches()) {
									/*
									 * 逐行读取文件
									 */
									BufferedReader br = null;
									try {
										br = new BufferedReader(new FileReader(rsltFile));
										String line = br.readLine();
										while (line != null) {
											Matcher sampleSepMt = sampleSepPtn.matcher(line);
											Matcher keyValueMt = keyValuePtn.matcher(line);

											if (keyValueMt.matches()) {
												// Key
												String key = keyValueMt.group(keyValuePtn_gid_key);
												// Value
												String value_s = keyValueMt.group(keyValuePtn_gid_value);

												Double value = execMeminfo.getMeminfo(key);
												if (value == null) {
													value = new Double(value_s);
												} else {
													Double thisValue = new Double(value_s);
													value = value + thisValue;
												}
												execMeminfo.putMeminfo(key, value);
											} else if (sampleSepMt.matches()) {
												sampleNum++;
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

					/*
					 * 将执行结果写道Excel
					 */
					String excelPath = baseDir + File.separator + execDir.getName() + File.separator + "meminfo.xls";
					OutputStream os = null;
					try {
						Logger.logSysInfo(Analyst.class, "Write result of " + execDir.getName() + " to excel...");

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
						// Memory
						Label execIdLb = new Label(titleCol++, 0, "Memory", colTitleFormat);
						wsh.addCell(execIdLb);
						// Size
						Label avgExecTimeLb = new Label(titleCol++, 0, "Size", colTitleFormat);
						wsh.addCell(avgExecTimeLb);

						/*
						 * 写内容
						 */
						Set<String> keys = execMeminfo.getKeySet();
						int row = 0;
						for (String key : keys) {
							// 计算平均值
							double value = execMeminfo.getMeminfo(key);
							value = value / sampleNum;
							// 更新
							execMeminfo.putMeminfo(key, value);

							// 行号
							row++;
							// 列号
							int col = 0;

							// Memory
							WritableCell memoryCell = new Label(col++, row, key);
							wsh.addCell(memoryCell);
							// Size
							Number sizeNumber = new Number(col++, row, value);
							wsh.addCell(sizeNumber);
						}

						/*
						 * excel写出
						 */
						wwb.write();
						wwb.close();

						Logger.logSysInfo(Analyst.class,
								"Write result of " + execDir.getName() + " to excel...Finished.");
					} catch (Exception e) {
						Logger.logErrInfo(Analyst.class,
								"Error while writing result of " + execDir.getName() + " to excel " + excelPath + ".",
								e);
						throw e;
					} finally {
						if (os != null) {
							os.close();
						}
					}
				}
			}

			/*
			 * 写负载结果到Excel
			 */
			String meminfoPath = baseDir + File.separator + "meminfo.xls";
			OutputStream os = null;
			try {
				Logger.logSysInfo(Analyst.class, "Write meminfo to excel...");

				/*
				 * 创建excel文件
				 */
				os = new FileOutputStream(meminfoPath);
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

				// Active Anonymous Pages / Active Pages
				Label actAno_actPages_Lb = new Label(colId++, rowId, "Active Anonymous Pages / Active Pages",
						colTitleFormat);
				wsh.addCell(actAno_actPages_Lb);

				// Active Anonymous Pages / Anonymous Pages
				Label actAno_anoPages_Lb = new Label(colId++, rowId, "Active Anonymous Pages / Anonymous Pages",
						colTitleFormat);
				wsh.addCell(actAno_anoPages_Lb);

				// Anonymous Huge Pages / Anonymous Pages
				Label anoHuge_anoPages_Lb = new Label(colId++, rowId, "Anonymous Huge Pages / Anonymous Pages",
						colTitleFormat);
				wsh.addCell(anoHuge_anoPages_Lb);

				/*
				 * 写内容
				 */
				List<String> workloadNames = workloadMeminfoSet.getWorkloadNames();
				// 排序
				Collections.sort(workloadNames);
				// 遍历
				for (String workloadName : workloadNames) {
					// 负载结果
					WorkloadMeminfo workloadMeminfo = workloadMeminfoSet.getWorkloadMeminfo(workloadName);

					colId = 0;
					rowId++;

					List<String> pagesizes = workloadMeminfo.getPageSizes();

					// 负载名称
					wsh.mergeCells(colId, rowId, colId, rowId + pagesizes.size() - 1);
					WritableCell wlNameLab = new Label(colId++, rowId, workloadName);
					wsh.addCell(wlNameLab);

					// 排序
					Collections.sort(pagesizes);
					Collections.reverse(pagesizes);
					// 遍历
					for (String pagesize : pagesizes) {
						// 执行结果
						ExecMeminfo execMeminfo = workloadMeminfo.getExecMeminfo(pagesize);

						colId = 1;

						// 页大小
						WritableCell pagesizeCell = new Label(colId++, rowId, pagesize);
						wsh.addCell(pagesizeCell);

						// 活跃页
						double activePages = execMeminfo.getMeminfo("Active");
						// 活跃匿名页（包含Shmem）
						double activeAnoShmemPages = execMeminfo.getMeminfo("Active(anon)");
						// Shmem
						double shmemPages = execMeminfo.getMeminfo("Shmem");
						// 活跃匿名页（不包含Shmem）
						double activeAnoPages = activeAnoShmemPages - shmemPages;
						// 匿名页
						double anonPages = execMeminfo.getMeminfo("AnonPages");
						// 匿名大页
						double anonHugePages = execMeminfo.getMeminfo("AnonHugePages");

						// 活跃匿名页占活跃页比例
						double actAno_actPages = activeAnoPages / activePages;
						Number actAno_actPages_num = new Number(colId++, rowId, actAno_actPages);
						wsh.addCell(actAno_actPages_num);

						// 活跃匿名页占匿名页比例
						double actAno_anoPages = activeAnoPages / anonPages;
						Number actAno_anoPages_num = new Number(colId++, rowId, actAno_anoPages);
						wsh.addCell(actAno_anoPages_num);

						// 匿名大页占匿名页比例
						double anoHuge_anoPages = anonHugePages / anonPages;
						Number anoHuge_anoPages_num = new Number(colId++, rowId, anoHuge_anoPages);
						wsh.addCell(anoHuge_anoPages_num);

						rowId++;
					}
				}

				/*
				 * excel写出
				 */
				wwb.write();
				wwb.close();

				Logger.logSysInfo(Analyst.class, "Write meminfo to excel...Finished.");
			} catch (Exception e) {
				Logger.logErrInfo(Analyst.class, "Error while writing meminfo to excel " + meminfoPath + ".", e);
				throw e;
			} finally {
				if (os != null) {
					os.close();
				}
			}

			Logger.logSysInfo(Analyst.class, "End to count meminfo.");
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while counting meminfo.", e);
		}
	}

}
