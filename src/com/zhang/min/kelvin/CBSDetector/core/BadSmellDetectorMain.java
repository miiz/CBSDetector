package com.zhang.min.kelvin.CBSDetector.core;

import java.io.*;
import com.zhang.min.kelvin.CBSDetector.output.*;
import com.zhang.min.kelvin.CBSDetector.output.xml.XMLOutput;
import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

public class BadSmellDetectorMain {

	/**
	 * @param args
	 */
	private static DetectorOutput output=null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length<1){
			log("Usage: BadSmellDetector SourceDirectory [-options]");
			log("\nwhere options include:");
			log("\t-x\texport results into XML file");
			log("\t-d:<detector class name>\texecute a single CBS detector");
			log("\t-lib:<jar file location>\tadd third party library into analysis class path");
			return;
		}
		
		String sourceDir=args[0];
		String detectorClassName=null;
		if(args.length>1){
			for(int j=1;j<args.length;j++){
				if(args[j].equalsIgnoreCase("-x")){
					output=new XMLOutput("result.xml");
				}
				if(args[j].startsWith("-d")){
					detectorClassName=args[j].substring(3);
				}
				if(args[j].startsWith("-lib")){
					String libLocation=args[j].substring(5);
					sourceDir=sourceDir+java.io.File.pathSeparator+libLocation;
				}
			}
		}
		
		if(output==null){
			output=new SimpleTextOutput();
		}
		
		log("Libs:"+sourceDir);
		
		//Redirect Recoder output to log file
		try{
			PrintStream fo=new PrintStream("recoder.log");
			recoder.util.Debug.setOutput(fo);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		if(detectorClassName==null){
			output=BSDetector.executeAllDetection(output,sourceDir);
		}else{
			try{
				output=BSDetector.executeSingleDectection(detectorClassName, output, sourceDir);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		output.output();
	}

}
