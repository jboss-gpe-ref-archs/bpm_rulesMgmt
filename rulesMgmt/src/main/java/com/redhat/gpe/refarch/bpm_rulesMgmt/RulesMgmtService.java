package com.redhat.gpe.refarch.bpm_rulesMgmt;

import java.io.Serializable;
import java.util.Collection;

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

    public Collection<Serializable> getFacts(String deploymentId) {
        return rMgmtBean.getFacts(deploymentId);
    }
    
    public Object getFact(String deploymentId, FactHandle fHandle){
    	return rMgmtBean.getFact(deploymentId, fHandle);
    }

    public void dumpFacts(String deploymentId) {
        rMgmtBean.dumpFacts(deploymentId);
    }
    
    public int removeFacts(String deploymentId) {
        return rMgmtBean.removeFacts(deploymentId);
    }
}
