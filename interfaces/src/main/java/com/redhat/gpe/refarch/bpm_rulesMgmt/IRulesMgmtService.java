package com.redhat.gpe.refarch.bpm_rulesMgmt;

import java.io.Serializable;
import java.util.Collection;

public interface IRulesMgmtService {
    
    public void insertFact(String deploymentId, Object fObject);
    public void setGlobal(String deploymentId, String identifier, Object gObject);
    public int fireAllRules(String deploymentId);
    public int removeFacts(String deploymentId);
    
    public Collection<Serializable> getFacts(String deploymentId);
    public void dumpFacts(String deploymentId);
}
