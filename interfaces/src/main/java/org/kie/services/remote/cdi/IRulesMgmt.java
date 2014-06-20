package org.kie.services.remote.cdi;

import java.io.Serializable;
import java.util.Collection;

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
     * return a Collection of facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public Collection<Serializable> getFacts(String deploymentId);
    
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
     * log details of facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public void dumpFacts(String deploymentId);
    

}
