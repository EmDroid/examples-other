package com.styryx.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import javax.swing.*;

import com.styryx.util.ResourceBundle;

public class Hyperlink extends JButton {
	
	private static final Color DEFAULT_COLOR_LINK = Color.BLUE;
	private static final Color DEFAULT_COLOR_VISITED = Color.BLUE;
	private static final Color DEFAULT_COLOR_ACTIVE = Color.RED;
	
	public static final String TARGET_BLANK = "_blank";
	
	public static class LinkOpener {
		public final void open(URI link, String target) throws IOException {
			if (null == target) {
				target = TARGET_BLANK;
			}
			openLink(link, target);
		}
		protected void openLink(URI link, String target) throws IOException {
			Desktop.getDesktop().browse(link);
		}
	}
	
	private static LinkOpener sm_linkOpener = new LinkOpener();
	
	public static void setLinkOpener(LinkOpener linkOpener) {
		sm_linkOpener = linkOpener;
	}
	
	private URI m_link;
	private String m_target;
	
	private Color m_colorLink;
	private Color m_colorVisited;
	private Color m_colorActive;
	
	private boolean m_tooltipSet;
	
	static private URI createUri(String link) throws MalformedURLException, URISyntaxException {
		if (null == link) {
			return null;
		} else {
			URL url;
			try {
				url = new URL(link);
			} catch (MalformedURLException e) {
				// Try to add http schema.
				try {
					url = new URL("http://" + link);
				} catch (MalformedURLException e1) {
					// Re-throw the original exception.
					throw e;
				}
			}
			return url.toURI();
		}
	}
	
	public Hyperlink() {
		super();
		init(null);
	}
	
	public Hyperlink(Icon icon) {
		super(icon);
		init(null);
	}
	
	public Hyperlink(Icon icon, String link) throws MalformedURLException, URISyntaxException {
		super(icon);
		init(createUri(link));
	}
	
	public Hyperlink(Icon icon, URI link) {
		super(icon);
		init(link);
	}
	
	public Hyperlink(String text) {
		super(text);
		init(null);
	}
	
	public Hyperlink(String text, String link) throws MalformedURLException, URISyntaxException {
		super(text);
		init(createUri(link));
	}
	
	public Hyperlink(String text, URI link) {
		super(text);
		init(link);
	}

//	public Hyperlink(Action a) {
//		super(a);
//		init(null);
//	}
//	
//	public Hyperlink(Action a, String link) throws MalformedURLException, URISyntaxException {
//		super(a);
//		init(createUri(link));
//	}
//	
	public Hyperlink(String text, Icon icon) {
		super(text, icon);
		init(null);
	}
	
	public Hyperlink(String text, Icon icon, String link) throws MalformedURLException, URISyntaxException {
		super(text, icon);
		init(createUri(link));
	}
	
	public Hyperlink(String text, Icon icon, URI link) {
		super(text, icon);
		init(link);
	}
	
	private class LaunchListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			if (null != m_link) {
				try {
//					JOptionPane.showMessageDialog(null, "Context class loader: " +
//							SwingUtilities.getRoot(getParent()).getClass().getName());
//					Thread appletThread = listAllThreads(Thread.currentThread().getThreadGroup() /*, Applet.class */);
//					JOptionPane.showMessageDialog(null, "Context class loader: " + thread.getContextClassLoader().getClass().getName());
//					try {
//						Field threadTarget = appletThread.getClass().getDeclaredField("target");
//						threadTarget.setAccessible(true);
//						Object appletViewerPanel = threadTarget.get(appletThread);
//						Field appletMethod = appletViewerPanel.getClass().getDeclaredField("applet");
//						appletMethod.setAccessible(true);
//						Applet applet = (Applet) appletMethod.get(appletViewerPanel);
//					} catch (SecurityException e) {
//						e.printStackTrace();
//					} catch (NoSuchFieldException e) {
//						e.printStackTrace();
//					} catch (IllegalArgumentException e) {
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						e.printStackTrace();
//					}
					
//					Container c = getParent();
//					while ((null != c) && !(c instanceof Applet)) {
//						c = c.getParent();
//					}
//					if (null != c) {
//						JOptionPane.showMessageDialog(null, "Running under applet.");
//						((Applet)c).getAppletContext().showDocument(m_link.toURL(), "_blank");
//					} else {
//						Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + m_link.toString());
//						Desktop desktop = Desktop.getDesktop();
//						desktop.browse(m_link);
//					}
					sm_linkOpener.open(m_link, m_target);
				} catch (IOException e) {
					ResourceBundle messages = ResourceBundle.getFrameworkBundle(getLocale());
					JOptionPane.showMessageDialog(SwingUtilities.getRoot(getParent()),
							messages.getString("ExceptionThrown", "Exception thrown") + ": " + e.getLocalizedMessage(),
							messages.getString("ErrorOpenLink", "Error opening link"),
							JOptionPane.ERROR_MESSAGE);
//					ExceptionHandler.handleException(null, e);
				}
			}
		}
		
	}
	
	private class RolloverListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {
			// Underline the text.
			setBorderPainted(true);
		}

		public void mouseExited(MouseEvent e) {
			// Remove underline.
			setBorderPainted(false);
		}

		public void mousePressed(MouseEvent e) {
			// Set active color.
			setForegroundInternal(m_colorActive);
		}

		public void mouseReleased(MouseEvent e) {
			// Set normal color.
			setForegroundInternal(m_colorVisited);
		}
		
	}
	
	public void setLink(String link) throws MalformedURLException, URISyntaxException {
		setLink(link, getLocale());
	}
	
	public void setLink(URI link) {
		setLink(link, getLocale());
	}
	
	public void setLink(String link, Locale locale) throws MalformedURLException, URISyntaxException {
		setLink(createUri(link), locale);
	}
	
	public void setLink(URI link, Locale locale) {
		m_link = link;
		if ((!m_tooltipSet) && (null != link)) {
			super.setToolTipText(
					ResourceBundle.getFrameworkBundle(locale).getString("OpenSite", "Open site")
					+ ": " + link.toString());
		}
	}
	
	public URI getLink() {
		return m_link;
	}
	
	public void setTarget(String target) {
		m_target = target;
	}
	
	public String getTarget() {
		return m_target;
	}

	private void init(URI link) {
		m_tooltipSet = false;
		setLink(link);
		m_target = null;
		setForeground(DEFAULT_COLOR_LINK);
		m_colorVisited = DEFAULT_COLOR_VISITED;
		m_colorActive = DEFAULT_COLOR_ACTIVE;
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addActionListener(new LaunchListener());
		addMouseListener(new RolloverListener());
	}
	
	public Color getLinkColor() {
		return m_colorLink;
	}
	
	public void setForeground(Color color) {
		m_colorLink = color;
		setForegroundInternal(color);
	}

	private void setForegroundInternal(Color color) {
		super.setForeground(color);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, color));
	}

	public Color getVisitedColor() {
		return m_colorVisited;
	}
	
	public void setVisitedColor(Color color) {
		m_colorVisited = color;
	}

	public Color getActiveColor() {
		return m_colorActive;
	}
	
	public void setActiveColor(Color color) {
		m_colorActive = color;
	}
	
	public void setToolTipText(String text) {
		m_tooltipSet = true;
		super.setToolTipText(text);
	}

}
