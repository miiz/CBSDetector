package com.zhang.min.kelvin.CBSDetector.output.xml;

import java.util.Iterator;
import java.util.List;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;


import com.zhang.min.kelvin.CBSDetector.core.DetectResult;
import com.zhang.min.kelvin.CBSDetector.output.DetectorOutput;

import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

public class XMLOutput implements DetectorOutput {

	private Document xmlDoc=null;
	private String outputName=null;
	
	public XMLOutput(String fileName){
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//DOMImplementation impl = factory.newDocumentBuilder().getDOMImplementation();
			xmlDoc=factory.newDocumentBuilder().newDocument();
			Element root=xmlDoc.createElement("Root");
			xmlDoc.appendChild(root);
			outputName=fileName;
		}catch(Exception e){
			log(e.getMessage());
		}
	}
	
	public void addDetectorResults(String name, List<DetectResult> results) {
		//log(">>"+xmlDoc.getDocumentElement().getNodeName());
		Element root=xmlDoc.getDocumentElement();
		Element badSmell=xmlDoc.createElement("BadSmell");
		badSmell.setAttribute("name", name);
		badSmell.setAttribute("instances", Integer.toString(results.size()));
		
		Iterator<DetectResult> resultI=results.iterator();
		while(resultI.hasNext()){
			DetectResult dr=resultI.next();
			Element info=xmlDoc.createElement("Info");
			info.setAttribute("location", dr.getClassName());
			info.setAttribute("instances", Integer.toString(dr.getMessages().size()));
			CDATASection data=xmlDoc.createCDATASection(dr.getMessages().getAllMessages());	
			info.appendChild(data);
			badSmell.appendChild(info);
		}
		
		root.appendChild(badSmell);
	}
	
	public void output(){
		if(outputName!=null){
			
			try{
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serializer = tf.newTransformer();
				DOMSource domSource = new DOMSource(xmlDoc);
				FileWriter writer=new FileWriter(outputName);
				StreamResult streamResult = new StreamResult(writer);
				serializer.transform(domSource, streamResult);
				writer.close();
			}catch(Exception e){
				log(e.getMessage());
			}
		}
	}
	
	public void clear(){
		if(outputName!=null){
			File xmlfile=new File(outputName);
			if(xmlfile.exists()){
				xmlfile.delete();
			}
		}
	}

}
