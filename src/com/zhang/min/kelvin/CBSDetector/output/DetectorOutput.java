package com.zhang.min.kelvin.CBSDetector.output;

import java.util.List;

import com.zhang.min.kelvin.CBSDetector.core.DetectResult;

public interface DetectorOutput {
	public void addDetectorResults(String name,List<DetectResult> results);
	public void output();
	public void clear();
}
