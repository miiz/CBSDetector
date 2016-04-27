package com.zhang.min.kelvin.CBSDetector.eclipseplugin.popup.actions;

import java.io.File;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IPackagesViewPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.zhang.min.kelvin.CBSDetector.core.BSDetector;
import com.zhang.min.kelvin.CBSDetector.eclipseplugin.views.CBSDetectionResultView;
import com.zhang.min.kelvin.CBSDetector.output.DetectorOutput;

import static com.zhang.min.kelvin.CBSDetector.util.Debug.log;

public class CBSDetectAction implements IObjectActionDelegate {

	//private Shell shell;
	private IWorkbenchPart cPart;
	private IJavaProject jProject;
	
	/**
	 * Constructor for Action1.
	 */
	public CBSDetectAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		//shell = targetPart.getSite().getShell();
		cPart = targetPart;
		if(targetPart instanceof IPackagesViewPart){
			IPackagesViewPart pvPart=(IPackagesViewPart)targetPart;
			ITreeSelection tSelection=(ITreeSelection)pvPart.getTreeViewer().getSelection();
			Object sel=tSelection.getFirstElement();
			if(sel instanceof IAdaptable){
				jProject=(IJavaProject)((IAdaptable)sel).getAdapter(IJavaProject.class);
				//log(jProject.toString());
			}
		}
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		
		
		try{
			DetectorOutput output=((CBSDetectionResultView)cPart.getSite().getPage()
				.showView("com.zhang.min.kelvin.CBSDetector.eclipseplugin.views.CBSDetectionResultView"))
				.getDetectorOutput();
			output.clear();
			
			String sourceDir="";
			if(jProject!=null){
				String classpathStr=resolveClassPath(jProject);
				
				sourceDir=jProject.getProject().getLocation().toOSString()
							+File.pathSeparator+classpathStr;
				log("CP="+sourceDir);
				
				output=BSDetector.executeAllDetection(output, sourceDir);
			}
			
			output.output();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private String resolveClassPath(IJavaProject javaProject) throws JavaModelException {
		StringBuffer output=new StringBuffer();
		IClasspathEntry[] cpEntries=javaProject.getRawClasspath();
		for(IClasspathEntry cpEntry : cpEntries){
			if(cpEntry.getEntryKind()==IClasspathEntry.CPE_LIBRARY){
				IPath relativePath=cpEntry.getPath().makeRelativeTo(javaProject.getPath());
				if(relativePath.isAbsolute()){
					output.append(relativePath.toOSString());
				}else{
					output.append(javaProject.getProject().getLocation().toOSString());
					output.append(File.separator);
					output.append(relativePath.toOSString());
				}
				output.append(File.pathSeparator);
			}
		}
		return output.toString();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

}
