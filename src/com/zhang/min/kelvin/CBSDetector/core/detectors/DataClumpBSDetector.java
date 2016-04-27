package com.zhang.min.kelvin.CBSDetector.core.detectors;

import java.util.*;

import com.zhang.min.kelvin.CBSDetector.core.BSDetector;
import com.zhang.min.kelvin.CBSDetector.core.DetectResult;
import com.zhang.min.kelvin.CBSDetector.core.DetectorMessages;

import static com.zhang.min.kelvin.CBSDetector.util.BooleanOperators.XOR;
import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.*;
import recoder.abstraction.*;
import recoder.service.*;

public class DataClumpBSDetector extends BSDetector {

	public static int MinNumOfSimilar=3;
	public static String JAVA_OBJECT_TYPE="java.lang.Object";
	
	//private Queue<List<FieldSpecification>> fieldsInClasses=null;
	//private Queue<ClassDeclaration> classesQueue=null;
	private Queue<MyClassInfo> classInfoQueue=null;
	
	public DataClumpBSDetector(){
		//fieldsInClasses=new ArrayDeque<List<FieldSpecification>>();
		//classesQueue=new ArrayDeque<ClassDeclaration>();
		classInfoQueue=new ArrayDeque<MyClassInfo>();
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Data Clumps";
	}

	@Override
	protected DetectorMessages processEachFile(
			CrossReferenceServiceConfiguration sc, CompilationUnit cu) {
		String fileName=cu.getName();
		TreeWalker wkr=new TreeWalker(cu);
		while(wkr.next()){
			ProgramElement pe=wkr.getProgramElement();
			searchClasses(pe,fileName);
		}
		return null;
	}
	
	protected void postProcess(CrossReferenceServiceConfiguration sc,List<DetectResult> resultList){
		//searchDuplicatedFields(resultList);
		//searchDuplications(sc.getCrossReferenceSourceInfo(),resultList);
		MyClassInfo currentMCI=null;
		while((currentMCI=classInfoQueue.poll())!=null){
			
			Iterator<MyClassInfo> i=classInfoQueue.iterator();
			while(i.hasNext()){
				MyClassInfo comparedMCI=i.next();
				searchDuplicatedFields(resultList, currentMCI, comparedMCI);
				searchDuplicatedParameters(sc.getCrossReferenceSourceInfo(), resultList, currentMCI, comparedMCI);
			}
		}
	}

	private void searchClasses(ProgramElement pe, String fileName){
		if(pe instanceof ClassDeclaration){
			ClassDeclaration cd=(ClassDeclaration)pe;
			try{
				MyClassInfo mci=new MyClassInfo();
				mci.setFileName(fileName);
				mci.setFields(cd.getFields());
				mci.setDeclaration(cd);
				classInfoQueue.add(mci);
				//fieldsInClasses.add(cd.getFields());
				//classesQueue.add(cd);
			}catch(Exception e){
				log(e.getMessage());
			}
		}
	}

	private void searchDuplicatedParameters(CrossReferenceSourceInfo info, List<DetectResult> resultList, MyClassInfo currentMCI, MyClassInfo comparedMCI) {
		ClassDeclaration currentCd=currentMCI.getDeclaration();
		ClassDeclaration comparedCd=comparedMCI.getDeclaration();
		
		if(!inSameHierarchy(currentCd,comparedCd)){
			List<Method> comparedMl=comparedCd.getMethods();
			
			Iterator<Method> iCM=currentCd.getMethods().iterator();
			while(iCM.hasNext()){
				try{
					MethodDeclaration currentMethod=info.getMethodDeclaration(iCM.next());
					
					Iterator<Method> iComM=comparedMl.iterator();
					while(iComM.hasNext()){
						try{
							MethodDeclaration comparedMethod=info.getMethodDeclaration(iComM.next());
							
							if(isParameterListDuplicated(currentMethod,comparedMethod)){
								DetectResult dr=new DetectResult(currentMCI.getFileName()+" & "+comparedMCI.getFileName());
								DetectorMessages dm=new DetectorMessages();
								
								StringBuffer results = new StringBuffer();
								results.append(currentCd.getFullName()+" and "+comparedCd.getFullName());
								results.append("\n");
								results.append(DetectorMessages.messagePatternTwo("Parameters in method "
										+currentMethod.getFullName()+" and "+comparedMethod.getFullName(), "duplicated"));
								
								dm.addMessage(results.toString());			
								dr.setMessages(dm);
								resultList.add(dr);
							}
						}catch(Exception e2){
							log(e2.getMessage());
						}
					}
				}catch(Exception e1){
					log(e1.getMessage());
				}
			}
		}
	}

	private void searchDuplicatedFields(List<DetectResult> resultList, MyClassInfo currentMCI, MyClassInfo comparedMCI) {
		List<FieldSpecification> currentList=currentMCI.getFields();
		List<FieldSpecification> tempList=comparedMCI.getFields();
		
		Iterator<FieldSpecification> icurrent=currentList.iterator();
		int numOfSimiliar=0;
		String fieldNames="";
		String currentClassName="";
		String comparedClassName="";
		while(icurrent.hasNext()){
			FieldSpecification cFS=icurrent.next();
			currentClassName=cFS.getContainingClassType().getFullName();
			
			Iterator<FieldSpecification> itemp=tempList.iterator();
			while(itemp.hasNext()){
				FieldSpecification cTP=itemp.next();
				comparedClassName=cTP.getContainingClassType().getFullName();
				
				try{
					if(cFS.getName().equals(cTP.getName())&&
							cFS.getType().getFullName().equals(cTP.getType().getFullName())){
						if(XOR(cFS.isPrivate(),cTP.isPrivate()) && XOR(cFS.isProtected(),cTP.isProtected())
								&& XOR(cFS.isPublic(),cTP.isPublic())){
							numOfSimiliar++;
							fieldNames=fieldNames+", "+cFS.getName();
						}
					}
				}catch(Exception e){
					log(e.getMessage());
				}
			}
		}
		
		if(numOfSimiliar>=MinNumOfSimilar){
			DetectResult dr=new DetectResult(currentMCI.getFileName()+" & "+comparedMCI.getFileName());
			DetectorMessages dm=new DetectorMessages();
			
			StringBuffer results=new StringBuffer();
			results.append(currentClassName+" and "+comparedClassName);
			results.append("\n");
			fieldNames=fieldNames.substring(1);
			results.append(DetectorMessages.messagePatternTwo("Fields "+fieldNames, "duplicated"));
			
			dm.addMessage(results.toString());
			dr.setMessages(dm);
			
			resultList.add(dr);
		}
	}
	
	/*private void searchDuplicatedFields(List<DetectResult> resultList) {
		List<FieldSpecification> currentList=null;
		while((currentList=fieldsInClasses.poll())!=null){
			Iterator<List<FieldSpecification>> i=fieldsInClasses.iterator();
			while(i.hasNext()){
				List<FieldSpecification> tempList=i.next();
				
				Iterator<FieldSpecification> icurrent=currentList.iterator();
				int numOfSimiliar=0;
				String fieldNames="";
				String currentClassName="";
				String comparedClassName="";
				while(icurrent.hasNext()){
					FieldSpecification cFS=icurrent.next();
					currentClassName=cFS.getContainingClassType().getFullName();
					
					Iterator<FieldSpecification> itemp=tempList.iterator();
					while(itemp.hasNext()){
						FieldSpecification cTP=itemp.next();
						comparedClassName=cTP.getContainingClassType().getFullName();
						
						try{
							if(cFS.getName().equals(cTP.getName())&&
									cFS.getType().getFullName().equals(cTP.getType().getFullName())){
								if(XOR(cFS.isPrivate(),cTP.isPrivate()) && XOR(cFS.isProtected(),cTP.isProtected())
										&& XOR(cFS.isPublic(),cTP.isPublic())){
									numOfSimiliar++;
									fieldNames=fieldNames+", "+cFS.getName();
								}
							}
						}catch(Exception e){
							log(e.getMessage());
						}
					}
				}
				
				if(numOfSimiliar>=MinNumOfSimilar){
					DetectResult dr=new DetectResult(currentClassName+" & "+comparedClassName);
					DetectorMessages dm=new DetectorMessages();
					fieldNames=fieldNames.substring(1);
					dm.addMessage(DetectorMessages.messagePatternTwo("Fields "+fieldNames, "duplicated"));
					dr.setMessages(dm);
					
					resultList.add(dr);
				}
			}
			
		}
	}
	
	private void searchDuplicatedParameters(CrossReferenceSourceInfo info,List<DetectResult> resultList) {
		ClassDeclaration currentCd=null;
		while((currentCd=classesQueue.poll())!=null){
			Iterator<ClassDeclaration> iCompared=classesQueue.iterator();
			List<Method> currentMl=currentCd.getMethods();
			
			while(iCompared.hasNext()){
				ClassDeclaration comparedCd=iCompared.next();
				
				//System.out.println("Current Class:"+currentCd.getFullName()
				//					+" Compared Class:"+comparedCd.getFullName());
				
				if(!inSameHierarchy(currentCd,comparedCd)){
					List<Method> comparedMl=comparedCd.getMethods();
					
					Iterator<Method> iCM=currentMl.iterator();
					while(iCM.hasNext()){
						try{
							MethodDeclaration currentMethod=info.getMethodDeclaration(iCM.next());
							
							Iterator<Method> iComM=comparedMl.iterator();
							while(iComM.hasNext()){
								try{
									MethodDeclaration comparedMethod=info.getMethodDeclaration(iComM.next());
									
									if(isParameterListDuplicated(currentMethod,comparedMethod)){
										DetectResult dr=new DetectResult(currentCd.getFullName()+" & "+comparedCd.getFullName());
										DetectorMessages dm=new DetectorMessages();
										dm.addMessage(DetectorMessages.messagePatternTwo("Parameters in method "
												+currentMethod.getFullName()+" and "+comparedMethod.getFullName(), "duplicated"));
										dr.setMessages(dm);
										resultList.add(dr);
									}
								}catch(Exception e2){
									log(e2.getMessage());
								}
							}
						}catch(Exception e1){
							log(e1.getMessage());
						}
					}
				}
			}
		}
	}*/
	
	private boolean inSameHierarchy(ClassDeclaration classA, ClassDeclaration classB){
		try{
			Iterator<ClassType> listA=classA.getAllSupertypes().iterator();
			
			//getAllSupertypes return the selected ClassType as the first member of the return list
			while(listA.hasNext()){
				ClassType ctA=listA.next();
				//System.out.println("ClassType:<"+ctA.getFullName()+">");
				Iterator<ClassType> listB=classB.getAllSupertypes().iterator();
				while(listB.hasNext()){
					ClassType ctB=listB.next();
					if(ctA.getFullName().equals(ctB.getFullName())
						&&!ctA.getFullName().equals(JAVA_OBJECT_TYPE)
						&&!ctB.getFullName().equals(JAVA_OBJECT_TYPE)){
						return true;
					}
				}
			}
		}catch(Exception e){
			log(e.getMessage());
		}
		
		return false;
	}
	
	private boolean isParameterListDuplicated(MethodDeclaration methodA,MethodDeclaration methodB){
		List<ParameterDeclaration> listA=methodA.getParameters();
		List<ParameterDeclaration> listB=methodB.getParameters();
		int numOfSameParameter=0;
		
		//System.out.println("Method A="+methodA.getFullName());
		//System.out.println("Method B="+methodB.getFullName());
		Iterator<ParameterDeclaration> iA=listA.iterator();
		while(iA.hasNext()){
			boolean hasSameParameter=false;
			
			ParameterDeclaration pdA=iA.next();
			Iterator<ParameterDeclaration> iB=listB.iterator();
			while(iB.hasNext()){
				ParameterDeclaration pdB=iB.next();
				String nameA=pdA.getVariableSpecification().getName();
				String typeA=pdA.getVariableSpecification().getType().getFullName();
				String nameB=pdB.getVariableSpecification().getName();
				String typeB=pdB.getVariableSpecification().getType().getFullName();
				
				
				//System.out.println("Parameter in methodA="+nameA+":"+typeA
				//		+" Parameter in methodB="+nameB+":"+typeB);
				
				if(nameA.equalsIgnoreCase(nameB)&&typeA.equalsIgnoreCase(typeB)) hasSameParameter=true;
			}

			//System.out.println("hasSameParemeter"+hasSameParameter);
			if(hasSameParameter){
				numOfSameParameter++;
			}
		}
		
		//System.out.println("numOfSameParameter="+numOfSameParameter);
		if(numOfSameParameter>=MinNumOfSimilar){
			return true;
		}
		return false;
	}
	
	private static class MyClassInfo{
		private String fileName=null;
		private List<FieldSpecification> fields=null;
		private ClassDeclaration declaration=null;
		
		public String getFileName() {
			return fileName;
		}
		
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		
		public ClassDeclaration getDeclaration() {
			return declaration;
		}
		
		public void setDeclaration(ClassDeclaration declaration) {
			this.declaration = declaration;
		}
		
		public List<FieldSpecification> getFields() {
			return fields;
		}
		
		public void setFields(List<FieldSpecification> fields) {
			this.fields = fields;
		}
	}

}
