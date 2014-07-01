package org.kie.services.remote.ejb;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.rule.FactHandle;

public interface IRulesMgmtService {
    
    public FactHandle insertFact(String deploymentId, Object fObject);
    public void setGlobal(String deploymentId, String identifier, Object gObject);
    public int fireAllRules(String deploymentId);
    public Collection getFactHandles(String deploymentId);
    public Collection<Serializable> getFacts(String deploymentId);
    public Collection<Serializable> getFacts(String deploymentId, List<FactHandle> fHandles);
    public Object getFact(String deploymentId, FactHandle fHandle);
    public int removeFacts(String deploymentId);
    public int removeFact(String deployment, FactHandle fHandle);
    public ExecutionResults execute(String deploymentId, BatchExecutionCommand batchCommand);
    
    public void logFacts(String deploymentId);
    public void logRules(String deploymentId, boolean showMetadata);
}
