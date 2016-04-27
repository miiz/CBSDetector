package com.zhang.min.kelvin.CBSDetector.core;

public class DetectResult {
	private String className;
	private DetectorMessages messages;
	
	public DetectResult(String className){
		this.className=className;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public DetectorMessages getMessages() {
		return messages;
	}
	public void setMessages(DetectorMessages messages) {
		this.messages = messages;
	}
	
	public String toString(){
		return getMessages().size()+" instance(s) in "+ getClassName();
	}
}
