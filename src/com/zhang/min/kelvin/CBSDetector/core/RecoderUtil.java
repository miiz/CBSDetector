package com.zhang.min.kelvin.CBSDetector.core;

import java.util.Iterator;

import recoder.java.Statement;
import recoder.java.StatementBlock;
import recoder.java.statement.*;
import recoder.kit.StatementKit;

import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

public class RecoderUtil {
	public static int statementCount(Then thenStatement){
		Statement stmt=thenStatement.getBody();
		return statementCount(stmt);
	}
	
	public static int statementCount(Else elseStatement){
		Statement stmt=elseStatement.getBody();
		return statementCount(stmt);
	}
	
	public static int statementCount(Statement stmt){
		if(stmt instanceof StatementBlock) 
			return ((StatementBlock)stmt).getStatementCount();
		return 1;
	}
	
	public static Statement getNextStatement(Statement currentStatement){
		try{
			Iterator<Statement> statementList=StatementKit.getStatementMutableList(currentStatement).iterator();
			while(statementList.hasNext()){
				Statement statement=statementList.next();
				if(statement.equals(currentStatement)){
					if(statementList.hasNext()){
						return statementList.next();
					}else{
						return null;
					}
				}
			}
		}catch(Exception e){
			log(e.getMessage());
		}
		return null;
	}
}
