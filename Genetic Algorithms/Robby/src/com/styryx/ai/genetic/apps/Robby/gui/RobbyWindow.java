package com.styryx.ai.genetic.apps.Robby.gui;

import java.util.Locale;

import com.styryx.gui.CloseHandler;
import com.styryx.gui.MainWindow;
import com.styryx.gui.MainWindowHolder;

public class RobbyWindow extends MainWindow {
	
	private static class WindowInitializer extends MainWindowHolder.WindowInitializer {
		public WindowInitializer(MainWindowHolder holder, CloseHandler closeHandler) {
			super(closeHandler);
		}
		protected RobbyWindow createMainWindow(Locale locale) throws Exception {
			return new RobbyWindow(locale);
		}
	}
	
	public RobbyWindow(Locale locale) throws Exception {
		super(RobbyPanel.getMessages(locale).getString("RobbyTitle", "Robby - The soda can collector"),
				"resources/images/Robby-16.png", locale);
		setResizable(false);
//        setSize(800, 600);
	}

	public static WindowInitializer createInitializer(MainWindowHolder holder, CloseHandler closeHandler) {
		return new WindowInitializer(holder, closeHandler);
	}

	public PanelInitializer createPanelInitializer() {
		return RobbyPanel.createPanelInitializer();
	}

}
