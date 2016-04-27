package com.zhang.min.kelvin.CBSDetector.core.detectors;

import recoder.CrossReferenceServiceConfiguration;
import recoder.service.*;
import recoder.abstraction.*;
import recoder.convenience.TreeWalker;
import recoder.java.*;
import recoder.java.declaration.*;
import recoder.java.expression.operator.CopyAssignment;
import recoder.java.reference.*;
import recoder.kit.*;
import java.util.*;

import com.zhang.min.kelvin.CBSDetector.core.BSDetector;
import com.zhang.min.kelvin.CBSDetector.core.DetectorMessages;


import static com.zhang.min.kelvin.CBSDetector.core.RecoderUtil.getNextStatement;
import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

public class MessageChainBSDetector extends BSDetector {

	private List<ProgramElement> skippedElements=null;
	
	public MessageChainBSDetector(){
		skippedElements=new ArrayList<ProgramElement>();
	}
	
	
	public List<ProgramElement> getSkippedElements() {
		return skippedElements;
	}

	@Override
	public String getName() {
		return "Message Chains";
	}

	@Override
	protected DetectorMessages processEachFile(
			CrossReferenceServiceConfiguration sc, CompilationUnit cu) {
		
		DetectorMessages dm=new DetectorMessages();
		
		getSkippedElements().clear();
		
		TreeWalker wkr=new TreeWalker(cu);
		while(wkr.next()){
			ProgramElement pe=wkr.getProgramElement();
			if(pe instanceof MethodReference){
				MethodReference mr=(MethodReference)pe;
				if(isGetChain(mr,sc.getCrossReferenceSourceInfo())){
					dm.addMessage(DetectorMessages.messagePatternOne(getName(), 
							pe.getStartPosition().getLine(), "is a GetChain ["+pe.getASTParent().toSource()+"]"));
				}else{		
					String returnStr=checkTempMethodChain(sc.getCrossReferenceSourceInfo(), mr);
					if(returnStr!=null){
						dm.addMessage(DetectorMessages.messagePatternOne(getName(), 
								pe.getStartPosition().getLine(), "is a GetChain\n"+returnStr));
					}
				}
			}
		}
		return dm;
	}
	
	//Check in-line get chains.
	private boolean isGetChain(MethodReference mr,CrossReferenceSourceInfo si){
		if(!getSkippedElements().contains(mr)){
			if(doCallExternalGetMethod(mr,si)){
				NonTerminalProgramElement ntpe=mr.getASTParent();
				if(ntpe instanceof MethodReference){
					if(doCallExternalGetMethod((MethodReference)ntpe,si)){ 
						//Skip another getMembers in the chain;
						TreeWalker wkr=new TreeWalker(mr);
						while(wkr.next()){
							getSkippedElements().add(wkr.getProgramElement());
						}
						return true;
					}
				}
			}
		}
		return false;
	}
	
	//Check if it is a message chain through temps
	private String checkTempMethodChain(CrossReferenceSourceInfo si, MethodReference methodReference) {		
		MethodReference mr=null;
		List<Statement> messageTempChains= new ArrayList<Statement>();
		Queue<MethodReference> queue=new ArrayDeque<MethodReference>();
		queue.add(methodReference);
		while((mr=queue.poll())!=null){
			if(doCallExternalGetMethod(mr,si)&&!getSkippedElements().contains(mr)){
				//messageTempChains.add(mr);
				//System.out.println("Get Method Reference Parent:"+mr.getASTParent()+" : "+mr.getASTParent().toSource());
				Variable vs=null;
				Statement statement=null;
				
				if(mr.getASTParent() instanceof VariableSpecification){
					vs=(VariableSpecification)mr.getASTParent();
					VariableDeclaration vd=((VariableSpecification)mr.getASTParent()).getParent();
					if(vd instanceof LocalVariableDeclaration){
						statement=(LocalVariableDeclaration)vd;
					}
				}else if(mr.getASTParent() instanceof CopyAssignment){
					CopyAssignment cpAssignment=(CopyAssignment)mr.getASTParent();
					statement=cpAssignment;
					if(cpAssignment.getExpressionAt(0) instanceof Reference){
						Reference reference=(Reference)cpAssignment.getExpressionAt(0);
						if(reference instanceof UncollatedReferenceQualifier){
							try{
								reference=si.resolveURQ((UncollatedReferenceQualifier)reference);
							}catch(Exception e){
								log(e.getMessage());
							}
						}
						if(reference instanceof VariableReference){
							try{
								vs=si.getVariable((VariableReference)cpAssignment.getExpressionAt(0));
							}catch(Exception e){
								log(e.getMessage());
							}
						}
					}
				}
				
				if((vs!=null)&&(statement!=null)){
					messageTempChains.add(statement);
					Statement nextStatement=getNextStatement(statement);
					if(nextStatement!=null){
						//System.out.println(">>Next statement is"+nextStatement+" :"+nextStatement.toSource());
						try{
							List<VariableReference> vrList=VariableKit.getReferences(si, vs, (NonTerminalProgramElement)nextStatement,false);
							Iterator<VariableReference> iVrList=vrList.iterator();
							while(iVrList.hasNext()){
								VariableReference vr=iVrList.next();
								if(vr.getASTParent() instanceof MethodReference){
									queue.add((MethodReference)vr.getASTParent());
								}
							}
						}catch(Exception e){
							log(e.getMessage());
						}
					}
				}else{
					messageTempChains.add(mr);
				}
				getSkippedElements().add(mr);
			}
		}
		
		if(messageTempChains.size()>1){
			StringBuffer returnString=new StringBuffer();
			Iterator<Statement> i=messageTempChains.iterator();
			while(i.hasNext()){
				Statement tempMr=i.next();
				returnString.append(tempMr.toSource());
			}
			return returnString.toString();
		}
		return null;
	}
	
	//Check whether a get method call is from a same class
	protected boolean doCallExternalGetMethod(MethodReference mr,CrossReferenceSourceInfo si){
		return isGetMethod(mr)&&!isDeclarationInSameClass(mr,si);
	}
	
	private boolean isDeclarationInSameClass(MethodReference mr,CrossReferenceSourceInfo si){
		NonTerminalProgramElement ntpe=mr.getASTParent();
		while(!(ntpe instanceof ClassDeclaration))ntpe=ntpe.getASTParent();
		ClassDeclaration containerClass=(ClassDeclaration)ntpe;
		
		try{
			Method calledMethod=si.getMethod(mr);
			ClassTypeContainer declarationContainer=calledMethod.getContainer();
			
			if(containerClass.getFullName().equals(declarationContainer.getFullName())) return true;
		}catch(Exception e){
			log(e.getMessage());
		}
		return false;
	}
	
	private boolean isGetMethod(MethodReference mr){
		String methodName=mr.getName().trim();
		//System.out.println(methodName+":"+methodName.length()+mr.toSource());
		if(methodName.startsWith("get")&&methodName.length()>3){
			return Character.isUpperCase(methodName.charAt(3));
		}
		return false;
	}
}
