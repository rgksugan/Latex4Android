package in.rgksugan.latex4android.util;

import in.rgksugan.latex4android.TexEditor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;

public class SyntaxHighlighter implements TextWatcher {

	public static boolean first = true;

	/*
	 * This method is called after there is a change in the content of the
	 * EditBox. The input parameter Editable holds the current content of the
	 * EditBox.
	 */

	@Override
	public void afterTextChanged(Editable s) {
		String tmp = "";

		// Gets the current cursor position in the EditBox
		int pos = TexEditor.editor.getSelectionStart();
		int length = s.length();
		int start = 0, end = 0;

		// Check if it's the first time the method is called.

		// If not highlight only a part of the document.
		if (!first) {
			if (pos - 50 < 0) {
				start = 0;
			} else {
				start = pos - 50;
			}
			if (pos + 50 > length) {
				end = length;
			} else {
				end = pos + 50;
			}
			if (pos == 0) {
				start = 0;
				end = length;
			}
			tmp = s.subSequence(start, end).toString();
		} else {
			// If its the first time highlight the whole content
			tmp = s.toString();
		}
		first = false;
		
		// pattern to find all words following a \
		String slashpattern = "\\\\[\\w]*\\b";
		Pattern slash = Pattern.compile(slashpattern);
		Matcher matcher = slash.matcher(tmp);
		
		// loop for all the matches of the pattern
		while (matcher.find()) {
			
			//set foreground colour span
			s.setSpan(new ForegroundColorSpan(Color.rgb(209, 64, 240)),
					matcher.start() + start, matcher.end() + start,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		// pattern to find strings within the {}
		String curlypattern = "\\{.*?\\}";
		Pattern curly = Pattern.compile(curlypattern);
		Matcher matcher1 = curly.matcher(tmp);
		while (matcher1.find()) {
			s.setSpan(new ForegroundColorSpan(Color.rgb(28, 0, 255)),
					matcher1.start() + 1 + start, matcher1.end() - 1 + start,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		// pattern to find strings within the []
		String squarepattern = "\\[.*?\\]";
		Pattern square = Pattern.compile(squarepattern);
		Matcher matcher2 = square.matcher(tmp);
		while (matcher2.find()) {
			s.setSpan(new ForegroundColorSpan(Color.GREEN), matcher2.start()
					+ 1 + start, matcher2.end() - 1 + start,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		// pattern to find strings within the $$
		String dollarpattern = "(\\$(.*?)\\$)";
		Pattern dollar = Pattern.compile(dollarpattern);
		Matcher matcher3 = dollar.matcher(tmp);
		while (matcher3.find()) {
			String str = matcher3.group();
			if (str.length() == 2) {
				Matcher m4 = Pattern.compile("(\\$\\$(.*?)\\$\\$)")
						.matcher(tmp);
				while (m4.find()) {
					s.setSpan(new ForegroundColorSpan(Color.rgb(157, 69, 25)),
							m4.start() + 2 + start, m4.end() - 2 + start,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			} else {
				s.setSpan(new ForegroundColorSpan(Color.rgb(157, 69, 25)),
						matcher3.start() + 1 + start, matcher3.end() - 1
								+ start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		
		// This highlights all the LaTeX comments
		if (tmp.contains("%")) {
			int a = tmp.indexOf('%');
			int b = tmp.indexOf("\n", a);
			while (a != -1 && b != -1) {
				s.setSpan(new ForegroundColorSpan(Color.rgb(124, 10, 10)), a
						+ start, b + start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				a = tmp.indexOf('%', b);
				if (a != -1) {
					b = tmp.indexOf("\n", a);
				}
			}
		}
	}

	/*
	 * This method is called before the typed character is displayed in the screen
	 * 
	 */
	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
	}

	/*
	 * This method is called when the text is changed.
	 * 
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
