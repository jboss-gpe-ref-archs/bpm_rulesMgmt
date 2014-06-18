package org.kie.services.remote.cdi;

import java.io.Serializable;
import java.util.Collection;

public interface IRulesMgmt {
    
    public void insertFact(String deploymentId, Object fObject);
    public void setGlobal(String deploymentId, String identifier, Object gObject);
    public int fireAllRules(String deploymentId);
    public Collection<Serializable> getFacts(String deploymentId);
    public int removeFacts(String deploymentId);
    public void dumpFacts(String deploymentId);
    public void dispose(String deploymentId);

}
