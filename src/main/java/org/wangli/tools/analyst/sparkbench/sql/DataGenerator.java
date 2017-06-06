package org.wangli.tools.analyst.sparkbench.sql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wangli.tools.analyst.log.Logger;

public class DataGenerator {

	/*
	 * OS_ORDER_ITEM
	 */

	private static final String ooItemPath = "F:\\wangli1\\tmp\\OS_ORDER_ITEM.txt";

	private static final Pattern ooItemPtn = Pattern
			.compile("\\s*([0-9]+)\\|([0-9E\\.]+)\\|([0-9]+)\\|([0-9]+)\\|([0-9\\.]+)\\|([0-9\\.]+)\\s*");
	private static final int ooItemPtn_gid_1 = 1;
	private static final int ooItemPtn_gid_2 = 2;
	private static final int ooItemPtn_gid_3 = 3;

	private static final long ooItemNewItemNum = 50000000;

	private static final NumberFormat ooItemPart5Format = new DecimalFormat(".00");

	private static final NumberFormat ooItemPart6Format = new DecimalFormat(".00");

	/*
	 * OS_ORDER
	 */

	private static final String ooPath = "F:\\wangli1\\tmp\\OS_ORDER.txt";

	private static final Pattern ooPtn = Pattern.compile("\\s*([0-9]+)\\|([0-9]+)\\|([0-9\\-]+)\\s*");
	private static final int ooPtn_gid_1 = 1;
	private static final int ooPtn_gid_2 = 2;

	private static final int ooNewItemNum = 50000000;

	private static final SimpleDateFormat ooPart3Format = new SimpleDateFormat("yyyy-MM-dd");

	public static void main(String[] args) {
		try {
			Logger.logSysInfo(DataGenerator.class, "Start to generate data...");

			/*
			 * 读取OS_ORDER_ITEM源文件，生成后续内容
			 */
			Logger.logSysInfo(DataGenerator.class, "Generate data for " + ooItemPath + "...");
			BufferedReader ooiBr = null;
			BufferedWriter ooiBw = null;
			try {
				/*
				 * 读取原有条目
				 */
				ooiBr = new BufferedReader(new FileReader(ooItemPath));
				String line = ooiBr.readLine();
				String lastPart1_s = null;
				String lastPart2_s = null;
				String lastPart3_s = null;
				while (line != null) {
					Matcher ooItemMt = ooItemPtn.matcher(line);
					if (ooItemMt.matches()) {
						lastPart1_s = ooItemMt.group(ooItemPtn_gid_1);
						lastPart2_s = ooItemMt.group(ooItemPtn_gid_2);
						lastPart3_s = ooItemMt.group(ooItemPtn_gid_3);
					}

					line = ooiBr.readLine();
				}

				/*
				 * 生成新的条目
				 */
				ooiBw = new BufferedWriter(new FileWriter(ooItemPath, true));
				int lastPart1 = Integer.valueOf(lastPart1_s);
				double lastPart2 = Double.valueOf(lastPart2_s);
				int lastPart3 = Integer.valueOf(lastPart3_s);
				for (int i = 0; i < ooItemNewItemNum; i++) {
					Random random = new Random();

					int newPart1 = lastPart1 + 1;
					double newPart2 = lastPart2 + 1;
					int newPart3 = lastPart3 + 1;
					int newPart4 = random.nextInt(newPart1);
					float newPart5 = random.nextFloat() * 1000f;
					float newPart6 = random.nextFloat() * 100000f;

					String newItem = newPart1 + "|" + newPart2 + "|" + newPart3 + "|" + newPart4 + "|"
							+ ooItemPart5Format.format(newPart5) + "|" + ooItemPart6Format.format(newPart6);

					ooiBw.write(newItem);
					ooiBw.newLine();

					lastPart1 = newPart1;
					lastPart2 = newPart2;
					lastPart3 = newPart3;
				}

				Logger.logSysInfo(DataGenerator.class, "Generate data for " + ooItemPath + "...Done.");
			} catch (Exception e) {
				Logger.logErrInfo(DataGenerator.class, "Error while generating data for " + ooItemPath + ".", e);
				throw e;
			} finally {
				if (ooiBr != null) {
					ooiBr.close();
				}

				if (ooiBw != null) {
					ooiBw.close();
				}
			}

			/*
			 * 读取OS_ORDER源文件，生成后续内容
			 */
			Logger.logSysInfo(DataGenerator.class, "Generate data for " + ooPath + "...");
			BufferedReader ooBr = null;
			BufferedWriter ooBw = null;

			try {
				/*
				 * 读取原有条目
				 */
				ooBr = new BufferedReader(new FileReader(ooPath));
				String line = ooBr.readLine();
				String lastPart1_s = null;
				String lastPart2_s = null;
				while (line != null) {
					Matcher ooItemMt = ooPtn.matcher(line);
					if (ooItemMt.matches()) {
						lastPart1_s = ooItemMt.group(ooPtn_gid_1);
						lastPart2_s = ooItemMt.group(ooPtn_gid_2);
					}

					line = ooBr.readLine();
				}

				/*
				 * 生成新的条目
				 */
				ooBw = new BufferedWriter(new FileWriter(ooPath, true));
				int lastPart1 = Integer.valueOf(lastPart1_s);
				int lastPart2 = Integer.valueOf(lastPart2_s);
				for (int i = 0; i < ooNewItemNum; i++) {
					int newPart1 = lastPart1 + 1;
					int newPart2 = lastPart2 + 1;
					Date newPart3 = randomDate("2000-01-01", "2018-01-01");

					String newItem = newPart1 + "|" + newPart2 + "|" + ooPart3Format.format(newPart3);

					ooBw.write(newItem);
					ooBw.newLine();

					lastPart1 = newPart1;
					lastPart2 = newPart2;
				}

				Logger.logSysInfo(DataGenerator.class, "Generate data for " + ooPath + "...Done.");
			} catch (Exception e) {
				Logger.logErrInfo(DataGenerator.class, "Error while generating data for " + ooPath + ".", e);
				throw e;
			} finally {
				if (ooBr != null) {
					ooBr.close();
				}

				if (ooBw != null) {
					ooBw.close();
				}
			}

			Logger.logSysInfo(DataGenerator.class, "End to generate data.");
		} catch (Exception e) {
			Logger.logErrInfo(DataGenerator.class, "Error while generating data.", e);
		}
	}

	private static Date randomDate(String beginDate, String endDate) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date start = format.parse(beginDate);// 构造开始日期
			Date end = format.parse(endDate);// 构造结束日期
			// getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
			if (start.getTime() >= end.getTime()) {
				return null;
			}
			long date = random(start.getTime(), end.getTime());

			return new Date(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static long random(long begin, long end) {
		long rtn = begin + (long) (Math.random() * (end - begin));
		// 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
		if (rtn == begin || rtn == end) {
			return random(begin, end);
		}
		return rtn;
	}

}
