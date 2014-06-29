package com.styryx;

import java.net.URI;
import java.net.URL;
import java.util.Locale;

import com.styryx.app.ExceptionHandler;
import com.styryx.gui.components.Hyperlink;

public class Main {
	
	// Characters to obfuscate the author name.
	private static char dot() {return '.';}
	private static char brOpen() {return '(';}
	private static char brClose() {return ')';}
	private static char cp() {return '\u00A9';}
	private static char num0() {return '0';}
	private static char num1() {return '1';}
	private static char num2() {return '2';}
	private static char a() {return 'a';}
	private static char c() {return 'c';}
	private static char e() {return 'e';}
	private static char h() {return 'h';}
	private static char i() {return 'i';}
	private static char k() {return 'k';}
	private static char l() {return 'l';}
	private static char m() {return 'm';}
	private static char o() {return 'o';}
	private static char p() {return 'p';}
	private static char r() {return 'r';}
	private static char s() {return 's';}
	private static char sc() {return 'š';}
	private static char t() {return 't';}
	private static char v() {return 'v';}
	private static char w() {return 'w';}
	private static char x() {return 'x';}
	private static char y() {return 'y';}
	private static char ya() {return 'ý';}
	
	public static Hyperlink getAuthorLink(Locale locale) {
		// The author's link.
		URI uri = null;
		try {
			StringBuffer protocol = new StringBuffer();
			protocol
				.append(h())
				.append(t())
				.append(t())
				.append(p());
			StringBuffer address = new StringBuffer();
			address
				.append(w())
				.append(w())
				.append(w())
				.append(dot())
				.append(s())
				.append(t())
				.append(y())
				.append(r())
				.append(y())
				.append(x())
				.append(dot())
				.append(c())
				.append(o())
				.append(m())
			;
			URL url = new URL(protocol.toString(), address.toString(), "");
			uri = url.toURI();
		} catch (Exception e) {
			// Should not happen.
			ExceptionHandler.handleException(null, locale, e);
		}
		StringBuffer cp = new StringBuffer();
		cp
			.append(cp())
			.append(' ')
			.append(num2())
			.append(num0())
			.append(num1())
			.append(num0());
		StringBuffer auth = new StringBuffer();
		auth
			.append(Character.toUpperCase(e()))
			.append(m())
			.append(i())
			.append(l())
			.append(' ')
			.append(Character.toUpperCase(m()))
			.append(a())
			.append(sc())
			.append(k())
			.append(o())
			.append(v())
			.append(s())
			.append(k())
			.append(ya())
			.append(' ')
			.append(brOpen())
			.append(Character.toUpperCase(s()))
			.append(t())
			.append(y())
			.append(r())
			.append(y())
			.append(x())
			.append(brClose())
		;
		Hyperlink author = new Hyperlink(cp.toString() + ' ' + auth);
		author.setLink(uri, locale);
		author.setTarget(Hyperlink.TARGET_BLANK);
		return author;
	}

}
