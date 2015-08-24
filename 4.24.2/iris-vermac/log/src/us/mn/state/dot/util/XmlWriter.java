package us.mn.state.dot.util;

public class XmlWriter {

	public static String toXML(String s){
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}
}
