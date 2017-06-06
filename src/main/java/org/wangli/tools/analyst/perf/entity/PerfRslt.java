package org.wangli.tools.analyst.perf.entity;

public class PerfRslt {

	private String name;

	private long times = 0;
	private long times_sum = 0;
	private int times_num = 1;

	public void merge(PerfRslt perfRslt) {
		if (this.name.equals(perfRslt.getName())) {
			this.times_sum = this.times_sum + perfRslt.getTimes();
			this.times_num++;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTimes() {
		this.times = this.times_sum / this.times_num;
		return times;
	}

	public void setTimes(long times) {
		this.times = times;
		this.times_sum = this.times;
		this.times_num = 1;
	}

}
