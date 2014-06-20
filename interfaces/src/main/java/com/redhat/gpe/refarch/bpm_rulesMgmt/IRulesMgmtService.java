package com.redhat.gpe.refarch.bpm_rulesMgmt;

import java.io.Serializable;
import java.util.Collection;

import org.kie.api.runtime.rule.FactHandle;

public interface IRulesMgmtService {
    
    public FactHandle insertFact(String deploymentId, Object fObject);
    public void setGlobal(String deploymentId, String identifier, Object gObject);
    public int fireAllRules(String deploymentId);
    
    public Collection<Serializable> getFacts(String deploymentId);
    public Object getFact(String deploymentId, FactHandle fHandle);
    
    public int removeFacts(String deploymentId);
    public void dumpFacts(String deploymentId);
}
