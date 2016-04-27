package com.zhang.min.kelvin.CBSDetector.core.detectors;

import java.util.Iterator;
import java.util.List;

import com.zhang.min.kelvin.CBSDetector.core.BSDetector;
import com.zhang.min.kelvin.CBSDetector.core.DetectorMessages;

import recoder.*;
import recoder.convenience.TreeWalker;
import recoder.java.*;
import recoder.java.reference.*;
import recoder.java.declaration.*;
import recoder.abstraction.*;

import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

public class MiddleManBSDetector extends BSDetector {
	public final static int MIN_LOC=4;
	
	public String getName() {return "Middle Man";}
	
	protected DetectorMessages processEachFile(CrossReferenceServiceConfiguration sc,CompilationUnit cu){
		DetectorMessages dm=new DetectorMessages();
		
		TreeWalker wkr=new TreeWalker(cu);
		while(wkr.next()){
			ProgramElement pe=wkr.getProgramElement();
			if(pe instanceof ClassDeclaration){
				ClassDeclaration cd=(ClassDeclaration)pe;
				
				try{
					List<Method> mList=cd.getMethods();//not getAllMethods. getAllMethods will include subclass methods
					
					int methodNum=mList.size();
					int delegateMethodNum=0;
					//System.out.println("Class<"+cd.getName()+"> has "+methodNum+" methods");
					
					Iterator<Method> mIterator=mList.iterator();
					while(mIterator.hasNext()){
						Method mAbstract=mIterator.next();
						MethodDeclaration md=null;
						try{
							md=sc.getCrossReferenceSourceInfo().getMethodDeclaration(mAbstract);
						}catch(Exception e1){
							log(e1.getMessage());
						}
						
						if(md!=null){
							//Number of line of a method
							int loc=md.getEndPosition().getLine()-md.getStartPosition().getLine();
							//System.out.println(md.getName()+" get "+loc+" statements");
							
							if(loc<=MiddleManBSDetector.MIN_LOC){
								TreeWalker wkrMd=new TreeWalker(md);
								while(wkrMd.next()){
									ProgramElement mdPe=wkrMd.getProgramElement();
									if(mdPe instanceof MethodReference){
										MethodReference mr=(MethodReference)mdPe;
										try{
											Method calledMethod=sc.getCrossReferenceSourceInfo().getMethod(mr);
											if(!isSameContainer(calledMethod,mAbstract)){
												delegateMethodNum++;
												break;
											}
										}catch(Exception e2){
											log(e2.getMessage());
										}
									}
								}
							}
						}
					}
					//System.out.println("delegate method:"+delegateMethodNum+" methodNum/2:"+methodNum/2);
					if(isMoreThanHalf(delegateMethodNum,methodNum)&&(delegateMethodNum>0)){
						dm.addMessage(DetectorMessages.messagePatternOne(getName(), cd.getStartPosition().getLine()));
					}
				}catch(Exception e){
					log(e.getMessage());
				}
				
			}
		}
		return dm;
	}
	
	private boolean isMoreThanHalf(int data,int compareTo){
		double vData=(double)data;
		double vCompareTo=(double)compareTo;
		if(vData>=vCompareTo/2){
			return true;
		}
		return false;
	}
	
	private boolean isSameContainer(Method methodA, Method methodB){
		String containerAFullName=methodA.getContainer().getFullName();
		String containerBFullName=methodB.getContainer().getFullName();
		if(containerAFullName.equals(containerBFullName)){
			return true;
		}
		return false;
	}

}
