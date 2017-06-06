package org.wangli.tools.analyst.smtt;

import java.io.File;

public class Analyst {

	private static String baseDir = "D:\\wangli\\experiment\\smtt\\thp_lr_200w_3_md_smtt_1\\smtt\\lr_smtt_thp=never_hps=2m_numa=on_numabala=1\\1\\smttresult";

	public static void main(String[] args) {
		try {
			// 数据文件根目录
			File baseDirFile = new File(baseDir);
			// 所有的数据文件
			File[] dataFiles = baseDirFile.listFiles();
			// 逐一处理每一个数据文件
			for (int i = 0; i < dataFiles.length; i++) {
				File dataFile = dataFiles[i];
				if (dataFile.isFile()) {
					//
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
