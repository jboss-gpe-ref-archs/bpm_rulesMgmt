package org.kie.services.remote.domain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;

/*
 * XML wrapper for a list of generic objects
 */
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
