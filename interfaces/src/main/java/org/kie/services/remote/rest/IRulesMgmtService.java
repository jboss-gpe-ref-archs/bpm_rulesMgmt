package org.kie.services.remote.rest;

import java.util.Collection;

public interface IRulesMgmtService {
    
    public void insertFact(String deploymentId, Object fObject);
    public void setGlobal(String deploymentId, String identifier, Object gObject);
    public int fireAllRules(String deploymentId);
    public Collection<? extends Object> getFacts(String deploymentId);
    public void dispose(String deploymentId);
}
