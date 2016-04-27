package com.zhang.min.kelvin.CBSDetector.output;

import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

import java.util.Iterator;
import java.util.List;

import com.zhang.min.kelvin.CBSDetector.core.DetectResult;

public class SimpleTextOutput implements DetectorOutput {

	private StringBuffer info=null;
	
	public SimpleTextOutput(){
		info=new StringBuffer();
	}
	
	public void addDetectorResults(String name, List<DetectResult> results) {
		Iterator<DetectResult> resultI=results.iterator();
		info.append(name+" Bad Smell was found in:\n");
		while(resultI.hasNext()){
			DetectResult dr=resultI.next();
			info.append("\t"+dr.getClassName()+"\n");
			info.append("\t\t"+dr.getMessages().getAllMessages()+"\n");
		}
	}

	public void output() {
		log(info.toString());	
	}

	public void clear(){}
}
