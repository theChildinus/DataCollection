package com.weizesan.protocolcomposite.cmodbus;

import java.util.Comparator;

public class ContentComparator implements Comparator{

	public int compare(Object o1, Object o2) {

		int m1 = Integer.parseInt((String) o1, 16);
		int m2 = Integer.parseInt((String) o2, 16);
		if(m1>m2){
			return 1;
		}else if(m1<m2){
			return -1;
		}else
			return 0;
	}

}
