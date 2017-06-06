package org.wangli.tools.analyst;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class DataStructureTest {

	@Test
	public void testListContain() {
		List<String> list = new LinkedList<String>();
		list.add("aaa");
		if (list.contains("aaa")) {
			System.out.println("Contained.");
		} else {
			System.out.println("Not contained.");
		}
	}

	@Test
	public void testStringReplaceAll() {
		String s1 = "19,369,425,452";
		String s2 = s1.replaceAll(",", "");
		System.out.println(s1);
		System.out.println(s2);
	}

}
