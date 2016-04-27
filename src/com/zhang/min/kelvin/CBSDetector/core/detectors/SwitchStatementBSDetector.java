package com.zhang.min.kelvin.CBSDetector.core.detectors;

import java.util.*;

import com.zhang.min.kelvin.CBSDetector.core.BSDetector;
import com.zhang.min.kelvin.CBSDetector.core.DetectorMessages;

import recoder.*;
import recoder.convenience.TreeWalker;
import recoder.java.*;
import recoder.java.statement.*;
import recoder.list.generic.ASTList;
import recoder.java.expression.operator.*;
import static com.zhang.min.kelvin.CBSDetector.core.RecoderUtil.statementCount;

public class SwitchStatementBSDetector extends BSDetector {

	public final static int MIN_BRANCH=2;
	public final static int MIN_LOC=3;
	private List<If> checkedIfStatements=null;
	
	public String getName() {return "Switch Statement";}
	
	protected DetectorMessages processEachFile(CrossReferenceServiceConfiguration sc,CompilationUnit cu){
		DetectorMessages dm=new DetectorMessages();
		checkedIfStatements=new ArrayList<If>();
		
		TreeWalker wkr=new TreeWalker(cu);
		while(wkr.next()){
			ProgramElement pe=wkr.getProgramElement();
			checkSwitchStructure(dm, pe);
			checkIfElseStructure(dm, pe);
		}
		return dm;
	}

	private void checkSwitchStructure(DetectorMessages dm, ProgramElement pe) {
		if(pe instanceof Switch){
			Switch sws=(Switch)pe;
			ASTList<Branch> blist=sws.getBranchList();
			
			int maxBranchSize=0;
			if(blist.size()>=SwitchStatementBSDetector.MIN_BRANCH){
				Iterator<Branch> bi=blist.iterator();
				while(bi.hasNext()){
					Branch branch=bi.next();
					if(branch.getStatementCount()>maxBranchSize){
						maxBranchSize=branch.getStatementCount();
					}
				}
				//System.out.println("MaxBranchSize"+maxBranchSize);
				if(maxBranchSize>=SwitchStatementBSDetector.MIN_LOC){
					dm.addMessage(DetectorMessages.messagePatternOne(getName(), sws.getStartPosition().getLine()));
				}
			}
		}
	}
	
	private void checkIfElseStructure(DetectorMessages dm, ProgramElement pe) {
		if(pe instanceof If){
			If ifs=(If)pe;
			
			//If if already checked ignores
			if(checkedIfStatements.contains(ifs)) return;
				
			int branches=0;
			int maxBranchSize=0;
			while(ifs!=null){
				branches++;
				
				checkedIfStatements.add(ifs);
				if(!(ifs.getExpression() instanceof Instanceof)){
					return;
				}
				
				int bsize1=statementCount((Then)ifs.getBranchAt(0));
				if(bsize1>maxBranchSize) maxBranchSize=bsize1;
				//System.out.println("branch size:"+bsize1);
				
				if(ifs.getBranchCount()>1){
					if(ifs.getElse().getBody() instanceof If){
						ifs=(If)ifs.getElse().getBody();
					}else{
						branches++;
						
						int bsize2=statementCount((Else)ifs.getBranchAt(1));
						if(bsize2>maxBranchSize) maxBranchSize=bsize2;
						//System.out.println("branch size:"+bsize2);
						
						ifs=null;
					}
				}else{
					ifs=null;
				}
			}
			
			if((branches>=SwitchStatementBSDetector.MIN_BRANCH)
					&&(maxBranchSize>=SwitchStatementBSDetector.MIN_LOC)){
				dm.addMessage(DetectorMessages.messagePatternOne(getName(), pe.getStartPosition().getLine()));
			}
			//System.out.println("If statement found: it has "+branches+" branches. "+pe.toSource());
		}
	}

}
