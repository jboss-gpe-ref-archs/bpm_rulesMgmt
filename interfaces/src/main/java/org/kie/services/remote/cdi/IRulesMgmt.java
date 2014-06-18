package org.kie.services.remote.cdi;

import java.io.Serializable;
import java.util.Collection;

public interface IRulesMgmt {
    
    /*
     * insert a fact (of any Object type) into the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public void insertFact(String deploymentId, Object fObject);
    
    /*
     * insert a global (of any Object type) into the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public void setGlobal(String deploymentId, String identifier, Object gObject);
    
    /*
     * fire all rules on the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public int fireAllRules(String deploymentId);
    
    /*
     * return a Collection of facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public Collection<Serializable> getFacts(String deploymentId);
    
    /*
     * flush all facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public int removeFacts(String deploymentId);
    
    /*
     * log details of facts that are presently in the working memory of the rules engine assigned to a specific Deployment Unit
     */
    public void dumpFacts(String deploymentId);
    

}
