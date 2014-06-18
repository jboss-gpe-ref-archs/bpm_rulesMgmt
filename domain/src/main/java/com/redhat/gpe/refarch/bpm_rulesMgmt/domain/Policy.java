package com.redhat.gpe.refarch.bpm_rulesMgmt.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Policy", propOrder = {
    "policyId",
    "policyName"
})
@XmlRootElement(name="Policy")
public class Policy implements java.io.Serializable{

    private int policyId;
    private String policyName;

    public Policy() {}

    public Policy(int policyId, String policyName){
        this.policyId = policyId;
        this.policyName = policyName;
    }

    public String toString(){
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("policyId = ");
        sBuilder.append(policyId);
        sBuilder.append("\tpolicyName = ");
        sBuilder.append(policyName);
        return sBuilder.toString();
    }

    public int getPolicyId() {
        return policyId;
    }
    public void setPolicyId(int x) {
        policyId = x;
    }
    public String getPolicyName() {
        return policyName;
    }
    public void setPolicyName(String x) {
        policyName = x;
    }
}
