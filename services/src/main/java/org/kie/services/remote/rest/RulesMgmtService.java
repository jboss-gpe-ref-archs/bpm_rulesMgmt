package org.kie.services.remote.rest;

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


/*
 * EJB Facade to RulesMgmt functionality
 */

@Remote(IRulesMgmtService.class)
@Local(IRulesMgmtService.class)
@Singleton(name="prodKSessionProxy")
@Startup
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RulesMgmtService implements IRulesMgmtService {
    
    @Inject
    private IRulesMgmt rMgmtBean;


    public void insertFact(String deploymentId, Object fObject) {
        rMgmtBean.insertFact(deploymentId, fObject);
    }

    public void setGlobal(String deploymentId, String identifier, Object gObject) {
        rMgmtBean.setGlobal(deploymentId, identifier, gObject);
    }

    public int fireAllRules(String deploymentId) {
        return rMgmtBean.fireAllRules(deploymentId);
    }

    public Collection<? extends Object> getFacts(String deploymentId) {
        return rMgmtBean.getFacts(deploymentId);
    }

    public void dispose(String deploymentId) {
        rMgmtBean.dispose(deploymentId);
    }
}
