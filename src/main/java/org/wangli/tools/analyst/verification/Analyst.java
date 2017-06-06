package org.wangli.tools.analyst.verification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wangli.tools.analyst.log.Logger;
import org.wangli.tools.analyst.verification.entity.ErrorInfo;

public class Analyst {

	// 根目录，其下每一个子目录是一个Execution
	private static String basePath = "F:\\wangli1\\experiment2\\ftrace_3\\runinfo\\ftrace_3";

	// 检查结果文件路径
	private static String verificationFile = basePath + File.separator + "verification";

	// 检查参数文件正则表达式
	private static Pattern chkParamPtn = Pattern.compile("chk_params__.+__.+");
	// 运行文件正则表达式
	private static Pattern runPtn = Pattern.compile("run_.+__.+__.+");

	// 执行名称的正则表达式
	private static Pattern execNamePtn = Pattern
			.compile(".+_.+_thp=(always|madvise|never)_hps=(4k|2m|1g)_numa=(on|off)_numabala=(0|1)");
	private static int execNamePtn_gid_thp = 1;
	private static int execNamePtn_gid_hps = 2;
	private static int execNamePtn_gid_numa = 3;
	private static int execNamePtn_gid_numabala = 4;

	// THP的正则表达式
	private static Pattern thpPtn = Pattern.compile(".*\\[(always|madvise|never)\\].*");
	private static int thpPtn_gid_thp = 1;

	// Huge page size的正则表达式
	private static Pattern hpsPtn = Pattern.compile("\\s*Hugepagesize:\\s*([0-9]+)\\s*kB\\s*");
	private static int hpsPtn_gid_hps = 1;

	// NUMA的正则表达式
	private static Pattern numaPtn = Pattern.compile(".*node[0-9]+.*");

	// Numa-balancing的正则表达式
	private static Pattern numaBalaPtn = Pattern.compile(".*kernel.numa_balancing\\s+=\\s+(0|1).*");
	private static int numaBalaPtn_gid_numaBala = 1;

	// 匹配文件有异常的正则表达式
	private static Pattern err1Ptn = Pattern.compile("\\s*Exception in thread\\s+\\\"\\s*[a-zA-Z0-9]+\\s*\\\"\\s+.*");
	private static Pattern err2Ptn = Pattern.compile("\\s+at\\s+[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)+.*");
	private static Pattern err3Ptn = Pattern
			.compile(".*ERROR TaskSchedulerImpl: Lost executor [0-9]+ on .*: worker lost.*");
	private static Pattern err4Ptn = Pattern.compile("\\s*=====End with error.=====\\s*");

	public static void main(String[] args) {
		try {
			Logger.logSysInfo(Analyst.class, "Start to verify executions...");

			// 错误列表
			List<ErrorInfo> errorInfos = new LinkedList<ErrorInfo>();

			File baseDir = new File(basePath);
			File[] execDirs = baseDir.listFiles();
			for (int i = 0; i < execDirs.length; ++i) {
				File execDir = execDirs[i];
				if (execDir.isDirectory()) {
					// 执行名称
					String execName = execDir.getName();

					Matcher execNameMt = execNamePtn.matcher(execName);
					if (!execNameMt.matches()) {
						throw new Exception("The name of execution " + execName + " is illegal.");
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
								if (rsltFile.isFile()) {
									Matcher chkParamMt = chkParamPtn.matcher(rsltFile.getName());
									Matcher runMt = runPtn.matcher(rsltFile.getName());
									if (chkParamMt.matches()) {
										String execNameThp = execNameMt.group(execNamePtn_gid_thp);
										String execNameHps = execNameMt.group(execNamePtn_gid_hps);
										String execNameNuma = execNameMt.group(execNamePtn_gid_numa);
										String execNameNumaBala = execNameMt.group(execNamePtn_gid_numabala);
										/*
										 * 检查参数设置，逐行读取文件
										 */
										BufferedReader br = null;
										try {
											br = new BufferedReader(new FileReader(rsltFile));
											String line = br.readLine();
											while (line != null) {
												Matcher thpMt = thpPtn.matcher(line);
												Matcher hpsMt = hpsPtn.matcher(line);
												Matcher numaMt = numaPtn.matcher(line);
												Matcher numaBalaMt = numaBalaPtn.matcher(line);

												boolean error = false;
												String information = "";

												if (thpMt.matches()) {
													String thp = thpMt.group(thpPtn_gid_thp);
													if (!thp.equals(execNameThp)) {
														error = true;
														information = "THP should be " + execNameThp + ", but " + thp
																+ ".";
													}
												} else if (hpsMt.matches()) {
													String hps = hpsMt.group(hpsPtn_gid_hps);

													int execNameHps_int = Integer.valueOf(
															execNameHps.substring(0, execNameHps.length() - 1));
													if (execNameHps.endsWith("m")) {
														execNameHps_int = execNameHps_int * 1024;
													} else if (execNameHps.endsWith("g")) {
														execNameHps_int = execNameHps_int * 1024 * 1024;
													}

													String execNameHps_kB = String.valueOf(execNameHps_int);

													if (!hps.equals(execNameHps_kB)) {
														error = true;
														information = "Huge page size should be " + execNameHps_kB
																+ "KB, but " + hps + "KB.";
													}
												} else if (numaMt.matches()) {
													int nodeNum = 0;
													while (nodeNum < 2) {
														if (line.contains("node" + nodeNum)) {
															nodeNum++;
														} else {
															break;
														}
													}

													if (execNameNuma.equals("on") && nodeNum < 2) {
														error = true;
														information = "Numa should be on, but off.";
													} else if (!execNameNuma.equals("on") && nodeNum >= 2) {
														error = true;
														information = "Numa should be off, but on.";
													}
												} else if (numaBalaMt.matches()) {
													String numaBala = numaBalaMt.group(numaBalaPtn_gid_numaBala);
													if (!numaBala.equals(execNameNumaBala)) {
														error = true;
														information = "Numa-balancing should be " + execNameNumaBala
																+ ", but " + numaBala + ".";
													}
												}

												if (error) {
													ErrorInfo errorInfo = new ErrorInfo();
													errorInfo.setExecName(execName);
													errorInfo.setExecTName(execTDir.getName());
													errorInfo.setFileName(rsltFile.getName());
													errorInfo.setInformation(information);

													errorInfos.add(errorInfo);
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
									} else if (runMt.matches()) {
										/*
										 * 检查运行信息，逐行读取文件
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

												boolean error = false;
												String information = "";

												if (err1Mt.matches() || err2Mt.matches() || err3Mt.matches()
														|| err4Mt.matches()) {
													error = true;
													information = line;
												}

												if (error) {
													ErrorInfo errorInfo = new ErrorInfo();
													errorInfo.setExecName(execName);
													errorInfo.setExecTName(execTDir.getName());
													errorInfo.setInformation(information);

													errorInfos.add(errorInfo);
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
			}

			/*
			 * 将错误信息写到文件
			 */
			if (errorInfos.size() > 0) {
				Logger.logSysInfo(Analyst.class, "Error(s) appeared in " + errorInfos.size()
						+ " files. For Details to see " + verificationFile + ".");
				BufferedWriter bw = null;
				try {
					bw = new BufferedWriter(new FileWriter(verificationFile));
					for (int i = 0; i < errorInfos.size(); ++i) {
						bw.write(errorInfos.get(i).toString());
						bw.newLine();
					}
				} catch (Exception e) {
					Logger.logErrInfo(Analyst.class,
							"Error while writing path(s) of file(s) with error(s) to " + verificationFile + ".", e);
					throw e;
				} finally {
					if (bw != null) {
						bw.close();
					}
				}
			}

			Logger.logSysInfo(Analyst.class, "End to verify executions.");
		} catch (Exception e) {
			Logger.logErrInfo(Analyst.class, "Error while verifying executions.", e);
		}
	}

}
