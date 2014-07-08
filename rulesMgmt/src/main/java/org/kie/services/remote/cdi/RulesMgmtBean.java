/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.services.remote.cdi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.remote.services.cdi.DeploymentInfoBean;
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
        StringBuilder sBuilder = new StringBuilder("start() registered deploymentIds = ");
        logger.info("start");
        Collection<String> dCollection = dInfoBean.getDeploymentIds();
        for(String dId : dCollection){
            sBuilder.append("\n\t");
            sBuilder.append(dId);
        }
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
    
    public int removeFact(String deploymentId, FactHandle fHandle) {
        KieSession kSession = getKieSession(deploymentId);
        Object fObj = kSession.getObject(fHandle);
        if(fObj != null){
            kSession.delete(fHandle);
            return 1;
        } else {
            logger.warn("removeFact() following fact not found: "+fHandle);
            return 0;
        }
    }
    
    // dumps inventory of facts to log file
    public void logFacts(String deploymentId) {
        KieSession kSession = getKieSession(deploymentId);
        Collection<? extends Object> facts = kSession.getObjects();
        Iterator iFacts = facts.iterator();
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("logFacts() facts = \n");
        while(iFacts.hasNext()){
            sBuilder.append(iFacts.next());
            sBuilder.append("\n");
        }
        logger.info(sBuilder.toString());
    }
    
    public void logRules(String deploymentId, boolean showMetadata) {
        KieSession kSession = getKieSession(deploymentId);
        StringBuilder sBuilder = new StringBuilder("logRules() rules for deploymentId = "+deploymentId+" as follows:");
        KieBase kBase = kSession.getKieBase();
        Collection<KiePackage> kPackages = kBase.getKiePackages();
        if(kPackages.size() == 0){
            sBuilder.append("\n\tNo kPackages defined");
        }else {
            for(KiePackage kPackage : kPackages){
                sBuilder.append("\n\t");
                sBuilder.append(kPackage.getName());
                Collection<Rule> rules = kPackage.getRules();
                if(rules.size() == 0)
                    sBuilder.append("\n\t\tNo rules defined");
                else {
                    for(Rule rule : rules) {
                        sBuilder.append("\n\t\t");
                        sBuilder.append(rule.getName());
                        if(showMetadata){
                            Map<String, Object> ruleMetadata = rule.getMetaData();
                            if(ruleMetadata.size() == 0)
                                sBuilder.append("\n\t\t\tno metadata for this rule");
                            else {
                                for(Entry<String,Object> entry : ruleMetadata.entrySet()){
                                    sBuilder.append("\n\t\t\t");
                                    sBuilder.append(entry.getKey()+" : "+entry.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        logger.info(sBuilder.toString());
    }
    
    public ExecutionResults execute(String deploymentId, BatchExecutionCommand batchCommand) {
        KieSession kSession = getKieSession(deploymentId, true);
        ExecutionResults eResults = kSession.execute(batchCommand);
        return eResults;
    }
    
    public void dispose(String deploymentId) {
        KieSession kSession = getKieSession(deploymentId);
        kSession.dispose();
        sessionMap.remove(deploymentId);
    }
    
    private KieSession getKieSession(String deploymentId){
        return this.getKieSession(deploymentId, false);
    }

    private KieSession getKieSession(String deploymentId, boolean stateless) {
        if(!stateless){
            if(sessionMap.get(deploymentId) != null)
                return sessionMap.get(deploymentId);
        }
        
        RuntimeManager runtimeManager = dInfoBean.getRuntimeManager(deploymentId);
        if (runtimeManager == null) {
            throw new RuntimeException("getRuntimeEngine() No runtime manager could be found for deployment '" + deploymentId + "'.");
        }
        
        /* 
         * There are two other implementations of: org.kie.api.runtime.manager.Context
         *   1)  CorrelationKeyContext
         *   2)  ProcessInstanceIdContext
         * Both alternatives appear to be specific to process engine scenarious
         */
        Context<?> runtimeContext = EmptyContext.get();
        RuntimeEngine rEngine = runtimeManager.getRuntimeEngine(runtimeContext);
        KieSession kSession = rEngine.getKieSession();
        
        if(!stateless)
            sessionMap.put(deploymentId, kSession);
        return kSession;
    }
    
    @PreDestroy
    public void stop() throws Exception{
        logger.info("destroy");
        
    }

    
    
}
