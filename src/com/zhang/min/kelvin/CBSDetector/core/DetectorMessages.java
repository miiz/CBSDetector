package com.zhang.min.kelvin.CBSDetector.core;

import java.util.*;

public class DetectorMessages {
	List<String> messages;
	
	public DetectorMessages(){
		messages=new ArrayList<String>();
	}
	
	public void addMessage(String msg){
		messages.add(msg);
	}
	
	public List<String> getMessageList(){
		return messages;
	}
	
	public String getAllMessages(){
		StringBuffer sb=new StringBuffer();
		Iterator<String> i=messages.iterator();
		while(i.hasNext()){
			sb.append(i.next()+"\n");
		}
		return sb.toString();
	}
	
	public boolean isEmpty(){
		if(messages.size()>0){
			return false;
		}
		return true;
	}
	
	public static String messagePatternOne(String prefix,int lineNum){
		return messagePatternOne(prefix,lineNum,"");
	}
	
	public static String messagePatternOne(String prefix,int lineNum,String postfix){
		StringBuffer sb=new StringBuffer();
		sb.append(prefix);
		sb.append(" was found in line ("+lineNum+") ");
		sb.append(postfix);
		return sb.toString();
	}
	
	public static String messagePatternTwo(String prefix,String postfix){
		StringBuffer sb=new StringBuffer();
		sb.append(prefix);
		sb.append(" was found ");
		sb.append(postfix);
		return sb.toString();
	}
	
	public int size(){
		if(messages!=null){
			return messages.size();
		}
		return 0;
	}
}
