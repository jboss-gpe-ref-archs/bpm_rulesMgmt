package org.kie.services.remote.cdi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.rule.FactHandle;

public interface IRulesMgmt {
    
    /*
     * insert a fact (of any Object type) into the working memory of the rules engine assigned to a specific Deployment Unit
     * returns the FactHandle for this fact
     */
    public FactHandle insertFact(String deploymentId, Object fObject);
    
    /*
     * insert a global (of any Object type) into the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public void setGlobal(String deploymentId, String identifier, Object gObject);
    
    /*
     * fire all rules on the working memory of the rules engine assigned to a specific Deployment Unit
     * returns # of rules fired
     */
    public int fireAllRules(String deploymentId);
    
    /*
     * return Collection of factHandles of all the facts presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public Collection getFactHandles(String deploymentId);
    
    /*
     * return the Collection of all facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public Collection<Serializable> getFacts(String deploymentId);
    
    /*
     * given a List of FactHandle objects, return a Collection of corresponding facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public Collection<Serializable> getFacts(String deploymentId, List<FactHandle> fHandles);
    
    /*
     * return from the working memory of the rules engine assigned to a specific Deployment Unit the fact corresponding to a fact handle
     */
    public Object getFact(String deploymentId, FactHandle fHandle);
    
    /*
     * flush all facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     * returns number of facts flushed from working memory 
     */
    public int removeFacts(String deploymentId);
    

    /*
     * given a fact handle, remove corresponding fact presently in the working memory of the rules engine assigned to a specific Deployment Unit
     * returns number of facts flushed from working memory (1 if fact was removed, 0 if fact is not found)
     */
    public int removeFact(String deploymentId, FactHandle fHandle);
    
    /*
     * Execute a batch of commands in a single transaction
     * Particularly useful to support interaction with stateless KIE sessions
     */
    public ExecutionResults execute(String deploymentId, List<Command> commandList);
    
    /*
     * log details of facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public void logFacts(String deploymentId);
    
    /*
     * log names of rules per KiePackage for a specific Deployment Unit
     */
    public void logRules(String deploymentId, boolean showMetadata);
    

}
