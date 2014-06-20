package com.redhat.gpe.refarch.bpm_rulesMgmt.domain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;

public class ObjectList<T> {
    
    private List<T> items;
    
    public ObjectList() {
        items = new ArrayList<T>();
    }
    
    public ObjectList(List<T> items) {
        this.items = items;
    }
 
    @XmlAnyElement(lax=true)
    public List<T> getItems() {
        return items;
    }
}
