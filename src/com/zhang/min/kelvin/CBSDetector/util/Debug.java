package com.zhang.min.kelvin.CBSDetector.util;

import java.io.*;

public class Debug {
	public static final int DEBUG=1;
	public static final int LOG=0;
	
	private static PrintStream o=System.out;
	private static int level=DEBUG;
	
	public static void setOutput(PrintStream output){
		o=output;
	}
	
	public static void setLogLevel(int newLevel){
		level=newLevel;
	}
	
	public static void log(String info){
		o.println(info);
	}
	
	public static void debug(String info){
		if(level==DEBUG){
			o.println(info);
		}
	}
}
