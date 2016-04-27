package com.zhang.min.kelvin.CBSDetector.core.detectors;

import java.util.*;

import com.zhang.min.kelvin.CBSDetector.core.BSDetector;
import com.zhang.min.kelvin.CBSDetector.core.DetectorMessages;

import recoder.*;
import recoder.convenience.TreeWalker;
import recoder.java.*;
import recoder.java.reference.*;
import recoder.java.declaration.*;
import recoder.kit.VariableKit;
import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

public class SpeculativeGeneralityBSDetector extends BSDetector {

	public String getName() {return "Speculative Generality";}
	
	protected DetectorMessages processEachFile(CrossReferenceServiceConfiguration sc,CompilationUnit cu){
		DetectorMessages dm=new DetectorMessages();
		TreeWalker wkr=new TreeWalker(cu);
		while(wkr.next()){
			ProgramElement pe=wkr.getProgramElement();
			unusedAbstractClass((CrossReferenceServiceConfiguration)sc,pe,dm);
			unusedParameter((CrossReferenceServiceConfiguration)sc,pe,dm);
		}
		return dm;
	}
	
	private boolean unusedAbstractClass(CrossReferenceServiceConfiguration sc,ProgramElement pe,DetectorMessages dm){
		if(pe instanceof InterfaceDeclaration){
			InterfaceDeclaration itrd=(InterfaceDeclaration)pe;
			int implementations=getInterfaceImplementedClasses(sc,itrd,0);
			if(implementations>1){
				return false;
			}
			dm.addMessage(DetectorMessages.messagePatternOne(getName()+" unused abstract class", itrd.getStartPosition().getLine()));
			return true;
		}else if(pe instanceof ClassDeclaration){
			ClassDeclaration cd=(ClassDeclaration)pe;
			if(cd.isAbstract()){
				//System.out.println(">>"+ cd.getName()+" is an abstract class");
				int implementations=getAbstractClassImplementedClasses(sc,cd,0);
				if(implementations>1){
					return false;
				}
				dm.addMessage(DetectorMessages.messagePatternOne(getName()+" unused abstract class", cd.getStartPosition().getLine()));
				return true;
			}
		}
		return false;
		
	}
	
	private int getInterfaceImplementedClasses(CrossReferenceServiceConfiguration sc,
			InterfaceDeclaration itrd,int implementations){
		int imp=implementations;
		try{
			List<TypeReference> rList=sc.getCrossReferenceSourceInfo().getReferences(itrd);
			Iterator<TypeReference> iRList=rList.iterator();
			while(iRList.hasNext()){
				TypeReference tr=iRList.next();
				if(tr.getASTParent() instanceof Implements){
					NonTerminalProgramElement ntpe=tr.getASTParent().getASTParent();
					if(ntpe instanceof ClassDeclaration){
						imp++;
					}else if(ntpe instanceof InterfaceDeclaration){
						imp=getInterfaceImplementedClasses(sc,(InterfaceDeclaration)ntpe,imp);
					}
				}
			}
		}catch(Exception e){
			//catch unresolved reference
			log(e.getMessage());
		}
		return imp;
	}
	
	private int getAbstractClassImplementedClasses(CrossReferenceServiceConfiguration sc,
			ClassDeclaration cd,int implementations){
		int imp=implementations;
		try{
			List<TypeReference> rList=sc.getCrossReferenceSourceInfo().getReferences(cd);
			Iterator<TypeReference> iRList=rList.iterator();
			while(iRList.hasNext()){
				TypeReference tr=iRList.next();
				if(tr.getASTParent() instanceof Extends){
					NonTerminalProgramElement ntpe=tr.getASTParent().getASTParent();
					if(ntpe instanceof ClassDeclaration){
						imp++;
					}
				}
			}
		}catch(Exception e){
			//catch unresolved reference
			log(e.getMessage());
		}
		return imp;
	}
	
	private boolean unusedParameter(CrossReferenceServiceConfiguration sc,ProgramElement pe,DetectorMessages dm){
		boolean flag=false;
		if(pe instanceof MethodDeclaration){
			MethodDeclaration md=(MethodDeclaration)pe;
			//Exclude abstract method because an abstract declaration
			//must have unused declaration
			if(!md.isAbstract()){
				List<ParameterDeclaration> pdl=md.getParameters();
				Iterator<ParameterDeclaration> pdlI=pdl.iterator();
				while(pdlI.hasNext()){
					ParameterDeclaration pd=pdlI.next();
					
					try{
						List<VariableReference> vr=VariableKit.getReferences(sc.getCrossReferenceSourceInfo(), pd.getVariableSpecification(),md, false);
						//System.out.println(vr.size());
						if(vr.size()==0){
							dm.addMessage(DetectorMessages.messagePatternOne(getName()+" unused parameter", md.getStartPosition().getLine(),
									" Variable '"+pd.getVariableSpecification().getName()+"' in method '"+md.getName()+"' is unused"));
							flag=true;
						}
					}catch(Exception e){
						//catch unresolved reference
						log(e.getMessage());
					}
					
				}
			}
		}
		return flag;
	}

}
