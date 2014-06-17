package org.kie.services.remote.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Alternative
@Default
public class RulesMgmtBean implements IRulesMgmt {
    
    private static final Logger logger = LoggerFactory.getLogger(RulesMgmtBean.class);
    
    // in-memory data structure that maps deploymentIds to KieSessions
    private static HashMap<String, KieSession> sessionMap = new HashMap<String, KieSession>();

    @Inject
    private DeploymentInfoBean dInfoBean;
    
    @PostConstruct
    public void start() {
        logger.info("start");
    }
    
    public void insertFact(String deploymentId, Object fObject){
        KieSession kSession = getKieSession(deploymentId);
        kSession.insert(fObject);
    }
    
    public void setGlobal(String deploymentId, String identifier, Object gObject) {
        KieSession kSession = getKieSession(deploymentId);
        kSession.setGlobal(identifier, gObject);
    }
    
    // returns number of rules fired
    public int fireAllRules(String deploymentId) {
        KieSession kSession = getKieSession(deploymentId);
        return kSession.fireAllRules();
    }
    
    // returns facts;  typically want to invoke this after having invoked:  fireAllRules
    public Collection<? extends Object> getFacts(String deploymentId){
        KieSession kSession = getKieSession(deploymentId);
        return kSession.getObjects();
    }
    
    public void dispose(String deploymentId) {
        KieSession kSession = getKieSession(deploymentId);
        kSession.dispose();
        sessionMap.remove(deploymentId);
    }

    private KieSession getKieSession(String deploymentId) {
        if(sessionMap.get(deploymentId) != null)
            return sessionMap.get(deploymentId);
        
        RuntimeManager runtimeManager = dInfoBean.getRuntimeManager(deploymentId);
        if (runtimeManager == null) {
            throw new RuntimeException("getRuntimeEngine() No runtime manager could be found for deployment '" + deploymentId + "'.");
        }
        Context<?> runtimeContext = EmptyContext.get();
        RuntimeEngine rEngine = runtimeManager.getRuntimeEngine(runtimeContext);
        KieSession kSession = rEngine.getKieSession();
        sessionMap.put(deploymentId, kSession);
        return kSession;
    }
    
    @PreDestroy
    public void stop() throws Exception{
        logger.info("destroy");
        
    }
    
}
