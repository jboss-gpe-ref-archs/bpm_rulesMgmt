package org.kie.services.remote.cdi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
    
    public FactHandle insertFact(String deploymentId, Object fObject){
        KieSession kSession = getKieSession(deploymentId);
        FactHandle fHandle = kSession.insert(fObject);
        return fHandle;
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
    
	public Collection getFactHandles(String deploymentId) {
		KieSession kSession = getKieSession(deploymentId);
        return kSession.getFactHandles();
	}

    
    // returns facts;  typically want to invoke this after having invoked:  fireAllRules
    @SuppressWarnings("unchecked")
    public Collection<Serializable> getFacts(String deploymentId){
        KieSession kSession = getKieSession(deploymentId);
        Collection<? extends Object> droolsCollection = kSession.getObjects();
        Iterator iCollection = droolsCollection.iterator();
        Collection<Serializable> serializedCollection = new ArrayList<Serializable>();
        while(iCollection.hasNext()){
            serializedCollection.add((Serializable)iCollection.next());
        }
        return serializedCollection;
    }
    
    public Collection<Serializable> getFacts(String deploymentId, List<FactHandle> fHandles) {
        KieSession kSession = getKieSession(deploymentId);
        Collection<Serializable> serializedCollection = new ArrayList<Serializable>();
        for(FactHandle fHandle : fHandles){
            Object fObj = kSession.getObject(fHandle);
            if(fObj == null)
                logger.warn("getFacts() fact not found for : "+fHandle);
            else
                serializedCollection.add((Serializable)fObj);
        }
        return serializedCollection;
    }
    
    public Object getFact(String deploymentId, FactHandle fHandle) {
        KieSession kSession = getKieSession(deploymentId);
        return kSession.getObject(fHandle);
    }
    
    public int removeFacts(String deploymentId) {
        KieSession kSession = getKieSession(deploymentId);
        Collection<FactHandle> facts = kSession.getFactHandles();
        Iterator<FactHandle> iFacts = facts.iterator();
        int factCount = facts.size();
        while(iFacts.hasNext()){
          FactHandle fHandle = iFacts.next();
          kSession.delete(fHandle);
        }
        return factCount;
    }
    
    // dumps inventory of facts to log file
    public void dumpFacts(String deploymentId) {
        KieSession kSession = getKieSession(deploymentId);
        Collection<? extends Object> facts = kSession.getObjects();
        Iterator iFacts = facts.iterator();
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("dumpFacts() facts = \n");
        while(iFacts.hasNext()){
            sBuilder.append(iFacts.next());
            sBuilder.append("\n");
        }
        logger.info(sBuilder.toString());
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
