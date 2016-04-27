package com.zhang.min.kelvin.CBSDetector.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zhang.min.kelvin.CBSDetector.core.detectors.DataClumpBSDetector;
import com.zhang.min.kelvin.CBSDetector.core.detectors.MessageChainBSDetector;
import com.zhang.min.kelvin.CBSDetector.core.detectors.MiddleManBSDetector;
import com.zhang.min.kelvin.CBSDetector.core.detectors.SpeculativeGeneralityBSDetector;
import com.zhang.min.kelvin.CBSDetector.core.detectors.SwitchStatementBSDetector;
import com.zhang.min.kelvin.CBSDetector.core.detectors.TrueMessageChainBSDetector;
import com.zhang.min.kelvin.CBSDetector.output.DetectorOutput;

import recoder.CrossReferenceServiceConfiguration;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;

public abstract class BSDetector {
	public List<DetectResult> executeDetection(String projectPath){
		List<DetectResult> returnList=new ArrayList<DetectResult>();
		
		CrossReferenceServiceConfiguration sc=new CrossReferenceServiceConfiguration();
		sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH , projectPath);
		sc.getProjectSettings().ensureSystemClassesAreInPath();
		try{
			List<CompilationUnit> cuList=sc.getSourceFileRepository().getAllCompilationUnitsFromPath();
			Iterator<CompilationUnit> ic=cuList.iterator();
			while(ic.hasNext()){
				CompilationUnit cu=ic.next();
				String fileName=cu.getName();
				//Process each file
				DetectorMessages dm=processEachFile(sc,cu);
				if(dm!=null){
					if(!dm.isEmpty()){
						DetectResult dr=new DetectResult(fileName);
						dr.setMessages(dm);
						returnList.add(dr);
					}
				}
			}
			
			//Post action after processing each file
			postProcess(sc,returnList);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return returnList;
	}
	
	public static List<BSDetector> getAllDetectors(){
		List<BSDetector> returnList=new ArrayList<BSDetector>();
		
		returnList.add(new SwitchStatementBSDetector());
		returnList.add(new SpeculativeGeneralityBSDetector());
		returnList.add(new MiddleManBSDetector());
		returnList.add(new MessageChainBSDetector());
		returnList.add(new TrueMessageChainBSDetector());
		returnList.add(new DataClumpBSDetector());
		
		return returnList;
	}

	
	public static DetectorOutput executeAllDetection(DetectorOutput output,String projectLocation){
		List<BSDetector> detectors=BSDetector.getAllDetectors();
		for(BSDetector tmpDetector : detectors){
			List<DetectResult> results=tmpDetector.executeDetection(projectLocation);
			output.addDetectorResults(tmpDetector.getName(), results);
		}
		return output;
	}
	
	public static DetectorOutput executeSingleDectection(String detectorClassName,DetectorOutput output,
			String projectLocation) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		BSDetector currentDetector=(BSDetector)Class.forName(detectorClassName).newInstance();
		List<DetectResult> results=currentDetector.executeDetection(projectLocation);
		output.addDetectorResults(currentDetector.getName(), results);
		return output;
	}
	
	public abstract String getName();
	protected abstract DetectorMessages processEachFile(CrossReferenceServiceConfiguration sc,CompilationUnit cu);
	
	protected void postProcess(CrossReferenceServiceConfiguration sc,List<DetectResult> resultList){}
}
