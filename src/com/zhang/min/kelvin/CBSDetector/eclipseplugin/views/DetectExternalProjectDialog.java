package com.zhang.min.kelvin.CBSDetector.eclipseplugin.views;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.zhang.min.kelvin.CBSDetector.core.BSDetector;
import com.zhang.min.kelvin.CBSDetector.output.DetectorOutput;
import com.zhang.min.kelvin.CBSDetector.output.xml.XMLOutput;

public class DetectExternalProjectDialog extends Dialog {
	
	private Label projectSelectionLabel;
	private Text projectRootDir;
	private Label classpathLabel;
	private Text classpath;
	private Label outputfileLabel;
	private Text outputFile;
	private Button okButton;
	private Button selectProjectButton;
	
	private String outputFileStr="results.xml";
	
	
	public DetectExternalProjectDialog(Shell shell){
		super(shell);
	}
	
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Detect CBS for External Project");
    }
	
	protected Control createDialogArea(Composite parent){
		Composite mainCom=(Composite) super.createDialogArea(parent);
		mainCom.setLayout(new GridLayout(2,false));
		
		projectSelectionLabel = new Label(mainCom,SWT.WRAP);
        projectSelectionLabel.setText("Project Directory:");
        projectSelectionLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER));
        
        Composite projectDirCom=new Composite(mainCom,SWT.NONE);
        projectDirCom.setLayout(new GridLayout(1,false));
        
        projectRootDir = new Text(projectDirCom, SWT.SINGLE | SWT.BORDER);
        setTextBoxSize(projectRootDir,60,1);
        projectRootDir.setEditable(false);
        
        selectProjectButton =createButton(projectDirCom, IDialogConstants.DETAILS_ID,
                "Browse...", true);
        
        classpathLabel= new Label(mainCom,SWT.WRAP);
        classpathLabel.setText("Classpath:");
        classpathLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER));
        
        classpath = new Text(mainCom, SWT.WRAP | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL );
        setTextBoxSize(classpath,80,8);
        
        outputfileLabel = new Label(mainCom,SWT.WRAP);
        outputfileLabel.setText("Output File Name:");
        outputfileLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER));
        
        outputFile=new Text(mainCom, SWT.SINGLE | SWT.BORDER);
        setTextBoxSize(outputFile,80,1);
        outputFile.setText(outputFileStr);
        
		return mainCom;
	}

	private void setTextBoxSize(Text textBox, int cols, int rows) {
		GC gc = new GC(textBox);
        FontMetrics fm = gc.getFontMetrics ();
        gc.dispose ();
        int width = cols * fm.getAverageCharWidth();
        int height = rows * fm.getHeight();
        
        GridData data = new GridData();
        data.widthHint = width;
        data.heightHint = height;

        textBox.setLayoutData(data);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID,
                "Export XML", true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }
	
	protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.DETAILS_ID) {
        	DirectoryDialog dirDialog=new DirectoryDialog(this.getShell());
        	projectRootDir.setText(dirDialog.open());
        }else if (buttonId == IDialogConstants.OK_ID) {
        	if(!projectRootDir.getText().isEmpty()){
        		String projectRootStr=projectRootDir.getText();
        		String classpathStr=classpath.getText();
        		
        		if(!outputFile.getText().isEmpty()){
        			outputFileStr=outputFile.getText();
        		}
        		
        		DetectorOutput output=new XMLOutput(outputFileStr);
        		output.clear();
        		output=BSDetector.executeAllDetection(output, 
        				projectRootStr+File.pathSeparator+classpathStr);
        		output.output();
        	}
        }
        super.buttonPressed(buttonId);
    }

	public Button getSelectProjectButton() {
		return selectProjectButton;
	}

	public Button getOkButton() {
		return okButton;
	}
}
