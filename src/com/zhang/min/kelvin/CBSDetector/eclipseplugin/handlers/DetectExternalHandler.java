package com.zhang.min.kelvin.CBSDetector.eclipseplugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.zhang.min.kelvin.CBSDetector.eclipseplugin.views.DetectExternalProjectDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class DetectExternalHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public DetectExternalHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		DetectExternalProjectDialog dialog=new DetectExternalProjectDialog(window.getShell());
		
		dialog.open();
		
		return null;
	}
}
