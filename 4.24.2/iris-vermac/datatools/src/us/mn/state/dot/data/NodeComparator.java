package us.mn.state.dot.data;

import java.util.Comparator;

public class NodeComparator implements Comparator{

	public int compare(Object o1, Object o2){
		return o1.toString().compareTo(o2.toString());
	}
}
