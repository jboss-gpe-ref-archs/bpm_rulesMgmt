package com.redhat.gpe.refarch.bpm_rulesMgmt.domain;

import java.io.Serializable;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolicyTracker", propOrder = {
    "processedCount"
})
@XmlRootElement(name="PolicyTracker")
public class PolicyTracker implements Serializable {
	
	private static final long serialVersionUID = 2661926848351583891L;
	private int processedCount;
	
	public int getProcessedCount() {
		return processedCount;
	}
	
	public void setProcessedCount(int x) {
		this.processedCount = x;
	}

}
