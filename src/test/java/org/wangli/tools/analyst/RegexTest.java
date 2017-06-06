package org.wangli.tools.analyst;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RegexTest {
	@Test
	public void testKeyValuePtn() {
		Pattern pattern = Pattern.compile("\\s*([0-9,]+)\\s+([a-zA-Z0-9:]+)\\s+(\\([0-9\\.%]+\\))\\s*");
		String line = "10,531,245,153,152      r003c                                                         (36.37%)";
		Matcher matcher = pattern.matcher(line);

		if (matcher.matches()) {
			System.out.println("Matched.");
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
			System.out.println(matcher.group(3));
		} else {
			System.out.println("Not Matched.");
		}
	}

}
