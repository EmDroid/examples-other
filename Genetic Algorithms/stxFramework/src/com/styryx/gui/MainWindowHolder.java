package com.styryx.gui;

import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;

public interface MainWindowHolder {
	
	public abstract class WindowInitializer {
		
		public static WindowInitializer create(MainWindowHolder holder) {
			return create(holder, null);
		}
		
		public static WindowInitializer create(MainWindowHolder holder, CloseHandler closeHandler) {
			WindowInitializer initializer = holder.createWindowInitializer(closeHandler);
			if (null == initializer) {
				throw new NullPointerException("createWindowInitializer() must not return 'null'!");
			}
			return initializer;
		}
		
		final private CloseHandler m_closeHandler;
		
		public WindowInitializer(CloseHandler closeHandler) {
			m_closeHandler = closeHandler;
		}
		
		public JFrame initialize(Locale locale) throws Exception {
			MainWindow mainWin = createMainWindow(locale);
			if (null == mainWin) {
				throw new NullPointerException("createMainWindow() must not return 'null'!");
			}
			mainWin.setCloseHandler(m_closeHandler);
			// Setup the window panel.
			JRootPane rootPane = mainWin.getRootPane();
			RootPanelHolder.PanelInitializer.create(mainWin).initialize(rootPane, locale, mainWin);
			// Setup the window listener.
//			if (null != m_closeHandler) {
//				mainWin.addWindowListener(this);
//			}
			// Set the close operation.
			mainWin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			// Setup the look and feel.
	        if (JDialog.isDefaultLookAndFeelDecorated()) {
	        	boolean supportsWindowDecorations =
	        		UIManager.getLookAndFeel().getSupportsWindowDecorations();
	            if (supportsWindowDecorations) {
	            	mainWin.setUndecorated(true);
	            	rootPane.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
	            }
	        }
			// Show the window.
	        mainWin.pack();
			mainWin.setVisible(true);
			return mainWin;
		}

		protected abstract MainWindow createMainWindow(Locale locale) throws Exception;
		
	}
	
	WindowInitializer createWindowInitializer(CloseHandler closeHandler);

}
