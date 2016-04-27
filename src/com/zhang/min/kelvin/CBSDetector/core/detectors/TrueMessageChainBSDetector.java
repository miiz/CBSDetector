package com.zhang.min.kelvin.CBSDetector.core.detectors;

import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;


import recoder.abstraction.Method;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.reference.MethodReference;
import recoder.service.CrossReferenceSourceInfo;


public class TrueMessageChainBSDetector extends MessageChainBSDetector {
	//Check whether a get method call is from a same class
	@Override
	public String getName() {
		return "True Message Chains";
	}
	
	@Override
	protected boolean doCallExternalGetMethod(MethodReference mr,CrossReferenceSourceInfo si){
		return super.doCallExternalGetMethod(mr, si) && !isThirdPartySource(mr,si);
	}
	
	private boolean isThirdPartySource(MethodReference mr,CrossReferenceSourceInfo si){
		try{
			Method calledMethod=si.getMethod(mr);
			MethodDeclaration methodDeclaration=si.getMethodDeclaration(calledMethod);
			//log("Method:"+calledMethod.getName()
			//		+" declaration:"+methodDeclaration);
			
			/* The third party library references do not have source code so method declaration is null*/
			if(methodDeclaration!=null){
				return false;
			}
		}catch(Exception e){
			log(e.getMessage());
		}
		
		return true;
	}
}
