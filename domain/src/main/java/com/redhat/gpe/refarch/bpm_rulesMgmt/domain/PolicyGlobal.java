package com.redhat.gpe.refarch.bpm_rulesMgmt.domain;

import java.io.Serializable;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/*
 *  Purpose:  used as a global variable for bpm_rulesMgmt test scenario
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolicyGlobal", propOrder = {
    "sleepTime"
})
@XmlRootElement(name="PolicyGlobal")
public class PolicyGlobal implements Serializable {
    
    private static final long serialVersionUID = 2661926848351583891L;
    private int sleepTime;
    
    public int getSleepTime() {
        return sleepTime;
    }
    
    public void setSleepTime(int x) {
        this.sleepTime = x;
    }

}
