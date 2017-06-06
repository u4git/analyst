package org.wangli.tools.analyst.verification.entity;

public class ErrorInfo {

	private String execName;

	private String execTName;

	private String fileName;

	private String information;

	@Override
	public String toString() {
		return this.execName + "/" + this.execTName + "/" + this.fileName + ": " + this.information;
	}

	public String getExecName() {
		return execName;
	}

	public void setExecName(String execName) {
		this.execName = execName;
	}

	public String getExecTName() {
		return execTName;
	}

	public void setExecTName(String execTName) {
		this.execTName = execTName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

}
