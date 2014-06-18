package com.redhat.gpe.refarch.bpm_rulesMgmt.domain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;

public class PolicyList<T> {
	
	private List<T> items;
	
	public PolicyList() {
		items = new ArrayList<T>();
	}
	
	public PolicyList(List<T> items) {
        this.items = items;
    }
 
    @XmlAnyElement(lax=true)
    public List<T> getItems() {
        return items;
    }
}
