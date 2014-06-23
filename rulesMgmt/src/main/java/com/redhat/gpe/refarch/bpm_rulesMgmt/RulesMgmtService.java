package com.redhat.gpe.refarch.bpm_rulesMgmt;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.kie.api.runtime.rule.FactHandle;
import org.kie.services.remote.cdi.IRulesMgmt;


/*
 * EJB Facade that introduces transaction boundaries and remoting interface for RulesMgmt functionality
 */

@Local(IRulesMgmtService.class)
@Singleton(name="rulesMgmtService")
@Startup
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RulesMgmtService implements IRulesMgmtService {
    
    @Inject
    private IRulesMgmt rMgmtBean;


    public FactHandle insertFact(String deploymentId, Object fObject) {
        return rMgmtBean.insertFact(deploymentId, fObject);
    }

    public void setGlobal(String deploymentId, String identifier, Object gObject) {
        rMgmtBean.setGlobal(deploymentId, identifier, gObject);
    }

    public int fireAllRules(String deploymentId) {
        return rMgmtBean.fireAllRules(deploymentId);
    }

    public Collection getFactHandles(String deploymentId) {
        return rMgmtBean.getFactHandles(deploymentId);
    }
    
    public Collection<Serializable> getFacts(String deploymentId) {
        return rMgmtBean.getFacts(deploymentId);
    }
    
    public Collection<Serializable> getFacts(String deploymentId, List<FactHandle> fHandles) {
        return rMgmtBean.getFacts(deploymentId, fHandles);
    }
    
    public Object getFact(String deploymentId, FactHandle fHandle){
        return rMgmtBean.getFact(deploymentId, fHandle);
    }

    public int removeFacts(String deploymentId) {
        return rMgmtBean.removeFacts(deploymentId);
    }
    
    public int removeFact(String deploymentId, FactHandle fHandle){
        return rMgmtBean.removeFact(deploymentId, fHandle);
    }
    
    public void logFacts(String deploymentId) {
        rMgmtBean.logFacts(deploymentId);
    }
    

}
