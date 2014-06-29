package com.styryx.gui;

import java.util.Locale;

import javax.swing.*;

public interface RootPanelHolder {
	
	public abstract class PanelInitializer {

		public static PanelInitializer create(RootPanelHolder holder) {
			PanelInitializer initializer = holder.createPanelInitializer();
			if (null == initializer) {
				throw new NullPointerException("createPanelInitializer() must not return 'null'!");
			}
			return initializer;
		}
		
		public void initialize(JRootPane rootPane, Locale locale, CloseHandler closeHandler) throws Exception {
			setupLookAndFeel();
			createMainPanel(rootPane, locale);
			JMenuBar menu = createMenu(closeHandler);
			if (null != menu) {
				rootPane.setJMenuBar(menu);
			}
		}

		protected void setupLookAndFeel() throws Exception {
			// Set cross-platform look nad feel by default.
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}

		protected JMenuBar createMenu(CloseHandler closeHandler) {
			// No menu by default.
			return null;
		}

		protected abstract void createMainPanel(JRootPane rootPane, Locale locale) throws Exception;

	}
	
	PanelInitializer createPanelInitializer();

}
