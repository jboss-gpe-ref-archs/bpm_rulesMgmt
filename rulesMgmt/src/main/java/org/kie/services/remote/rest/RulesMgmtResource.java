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

package org.kie.services.remote.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.common.DefaultFactHandle;
import org.kie.api.runtime.ExecutionResults;
import org.kie.services.remote.domain.ObjectList;
import org.kie.services.remote.ejb.IRulesMgmtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * RESTful API that exposes the functionality provided by the rules management CDI bean.
 * Supports invocation of both SINGLETON and PER_REQUEST KIESession strategies
 * 
 * addition of @Stateless annotation provides following advantages:
 *
 *   Injection capabilities: you can easily inject other EJBs, EntityManagers, JMS-resources, DataSources or JCA connectors
 *   Transactions: all changes made in a REST-call will be automatically and transparently synchronized with the database
 *   Single threading programming model -> the old EJB goodness.
 *   Monitoring: an EJB is visible in JMX
 *   Throttling: its easy to restrict the concurrency of an EJB using ThreadPools or bean pools
 *   Vendor-independence: EJB 3 runs on multiple containers, without any modification (and without any XML in particular :-))
 */
@Stateless
@Path("/RulesMgmtResource")
public class RulesMgmtResource {

    private static final String FACT_LIST = "factList";
    private static final String FACT_HANDLE_LIST = "factHandleList";

    @EJB(lookup="java:global/business-central/rulesMgmtService!org.kie.services.remote.ejb.IRulesMgmtService")
    IRulesMgmtService rProxy;

    private Logger log = LoggerFactory.getLogger("RulesMgmtResource");
    
    /**
     * sample usage :curl -v -u jboss:brms -X POST -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/Policy.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/fact?fqn=com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Policy
     *  
     */
    @POST
    @Path("/{deploymentId: .*}/fact")
    @Consumes({"application/json","application/xml"})
    @Produces({ "application/xml" })
    public Response insertFact(@PathParam("deploymentId") final String deploymentId, @QueryParam("fqn") final String fqn, InputStream oStream) {
        Object fObj = null;
        try {
            fObj = this.unmarshalXML(fqn, oStream);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.BAD_REQUEST).build();
        }
        DefaultFactHandle fHandle = (DefaultFactHandle)rProxy.insertFact(deploymentId, fObj);
        return marshallObject(fHandle.getClass(), fHandle);
    }
   
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X POST -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/PolicyGlobal.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/global/pGlobal
     */
    @POST
    @Path("/{deploymentId: .*}/global/{identifier: .*}/")
    @Consumes({"application/json","application/xml"})
    @Produces({ "text/plain" })
    public Response setGlobal(@PathParam("deploymentId") final String deploymentId, @PathParam("identifier") final String identifier, @QueryParam("fqn") final String fqn, InputStream oStream) {
        Object gObj = null;
        try {
            gObj = this.unmarshalXML(fqn, oStream);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.BAD_REQUEST).build();
        }
        rProxy.setGlobal(deploymentId, identifier, gObj);
        ResponseBuilder builder = Response.ok();
        return builder.build();
    }
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X POST docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/fireAllRules
     */
    @POST
    @Path("/{deploymentId: .*}/fireAllRules")
    @Produces({ "text/plain" })
    public Response fireAllRules(@PathParam("deploymentId") final String deploymentId) {
        int firedRuleCount = rProxy.fireAllRules(deploymentId);
        ResponseBuilder builder = Response.ok(firedRuleCount);
        return builder.build();
    }
    
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/factHandles > rulesMgmt/src/test/resources/fHandles.xml
     */
    @GET
    @Path("/{deploymentId: .*}/factHandles")
    @Produces({ "application/xml" })
    public Response getAllFactHandles(@PathParam("deploymentId") final String deploymentId) {
        Collection factHandles = rProxy.getFactHandles(deploymentId);
        log.info("getFacts() # of fact handles = "+factHandles.size());
        return marshalList(factHandles, FACT_HANDLE_LIST);
    }
    
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/facts
     */
    @GET
    @Path("/{deploymentId: .*}/facts")
    @Produces({ "application/xml" })
    public Response getAllFacts(@PathParam("deploymentId") final String deploymentId) {
        Collection<Serializable> facts = rProxy.getFacts(deploymentId);
        log.info("getAllFacts() # of fact handles = "+facts.size());
        return marshalList(facts, FACT_LIST);
    }
    
   /*
    *  curl -v -u jboss:brms -X GET -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/fHandles.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/facts
    */
   @GET
   @Path("/{deploymentId: .*}/facts")
   @Consumes({"application/xml"})
   @Produces({ "application/xml" })
   public Response getFacts(@PathParam("deploymentId") final String deploymentId, InputStream fHandleStream) {
       List fHandles = null;
       try {
           Class[] classes = new Class[]{DefaultFactHandle.class, ObjectList.class};
           fHandles = unmarshalXMLObjectList(fHandleStream, classes);
       }catch(JAXBException x){
           x.printStackTrace();
           return Response.status(Status.BAD_REQUEST).build();
       }
       log.info("getFacts() # of fact handles = "+fHandles.size());
       Collection<Serializable> facts = rProxy.getFacts(deploymentId, fHandles);
       return marshalList(facts, FACT_LIST);
   }
   
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/fHandle.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/fact
     */
    @GET
    @Path("/{deploymentId: .*}/fact")
    @Consumes({"application/xml"})
    @Produces({ "application/xml" })
    public Response getFact(@PathParam("deploymentId") final String deploymentId, DefaultFactHandle fHandle) {
        log.info("getFact() fHandle = "+fHandle);
        Object fact = rProxy.getFact(deploymentId, fHandle);
        return marshallObject(fact.getClass(), fact);
    }
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X DELETE docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/facts
     */
    @DELETE
    @Path("/{deploymentId: .*}/facts")
    @Produces({ "text/plain" })
    public Response removeFacts(@PathParam("deploymentId") final String deploymentId) {
        int factsRemoved = rProxy.removeFacts(deploymentId);
        ResponseBuilder builder = Response.ok(factsRemoved);
        return builder.build();
    }
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X DELETE -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/fHandle.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/fact
     */
    @DELETE
    @Path("/{deploymentId: .*}/fact")
    @Consumes({"application/xml"})
    @Produces({ "text/plain" })
    public Response removeFact(@PathParam("deploymentId") final String deploymentId,  DefaultFactHandle fHandle) {
        int factsRemoved = rProxy.removeFact(deploymentId, fHandle);
        ResponseBuilder builder = Response.ok(factsRemoved);
        return builder.build();
    }
    
    /**
     * purpose:
     *   - support use-cases requiring a PER_REQUEST KIESession
     *   
     * sample usage :
     *   curl -v -u jboss:brms -X POST -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/Commands.xml "docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/perrequest?fqns=com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Policy-com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Driver-com.redhat.gpe.refarch.bpm_rulesMgmt.domain.PolicyGlobal"
     *  
     * fqns query param:  '-' delimited String of your domain model's fqns that will be passed in the batch execution command
     */
    @POST
    @Path("/{deploymentId: .*}/perrequest")
    @Consumes({"application/json","application/xml"})
    @Produces({ "application/xml" })
    public Response execute(@PathParam("deploymentId") final String deploymentId, @QueryParam("fqns") final String fqnsString, InputStream commandStream){
        if(fqnsString == null || fqnsString.isEmpty()){
            log.error("execute() fqns query param = null");
            return Response.status(Status.BAD_REQUEST).build();
        }
        String[] fqns = fqnsString.split("-");
        if(fqns.length == 0){
            log.error("execute() no fqns added to POST as query param");
            return Response.status(Status.BAD_REQUEST).build();
        }
        log.info("execute() # of fqns = "+fqns.length);
        Class[] classes = new Class[fqns.length+1];
        classes[0] = BatchExecutionCommandImpl.class;
        int f=1;
        for(String fqn: fqns){
            try {
                Class fqnClass = Class.forName(fqn);
                classes[f] = fqnClass;
                f++;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return Response.status(Status.BAD_REQUEST).build();
            }
        }
        
        BatchExecutionCommandImpl batchCommand = null;
        try {
            batchCommand = unmarshalXMLCommandList(commandStream, classes);
        }catch(JAXBException x){
            x.printStackTrace();
            return Response.status(Status.BAD_REQUEST).build();
        }
        log.info("execute() # of commands = "+batchCommand.getCommands().size());
        ExecutionResults eResults = rProxy.execute(deploymentId, batchCommand);
        Collection<String> identifiers = eResults.getIdentifiers();
        Collection<Serializable> facts = new ArrayList<Serializable>();
        for(String identifier : identifiers){
            facts.add((Serializable)eResults.getValue(identifier));
        }
        return marshalList(facts, FACT_LIST);
    }
    
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/facts/count
     */
    @GET
    @Path("/{deploymentId: .*}/facts/count")
    @Produces({ "text/plain" })
    public Response getFactCount(@PathParam("deploymentId") final String deploymentId) {
        Collection<Serializable> facts = rProxy.getFacts(deploymentId);
        ResponseBuilder builder = Response.ok(facts.size());
        return builder.build();
    }
    
    /**
     * sample usage:
     *   curl -v -u jboss:brms -X PUT docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/rules?showMetadata=true
     */
    @PUT
    @Path("/{deploymentId: .*}/rules")
    @Produces({ "text/plain" } )
    public Response logRules(@PathParam("deploymentId") final String deploymentId,  @QueryParam("showMetadata") final String showMetadataString) {
        boolean showMetadata = true;
        if(showMetadataString != null && !showMetadataString.isEmpty()){
            showMetadata = Boolean.parseBoolean(showMetadataString);
        }
        rProxy.logRules(deploymentId, showMetadata);
        ResponseBuilder builder = Response.ok("check server.log\n");
        return builder.build();
        
    }
    
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET -HAccept:text/plain docker_bpms:8080/business-central/rest/RulesMgmtResource/sanityCheck
     */
    @GET
    @Path("/sanityCheck")
    @Produces({ "text/plain" })
    public Response sanityCheck() {
        ResponseBuilder builder = Response.ok("good to go\n");
        return builder.build();
    }

    private Response marshallObject(Class classObj, Object obj) {
        ResponseBuilder builder = null;
        if(obj == null)
            builder = Response.status(Status.NOT_FOUND);
        else {
            JAXBContext jc;
            Writer sWriter = null;
            try {
                jc = JAXBContext.newInstance(classObj);
                Marshaller marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                sWriter = new StringWriter();
                marshaller.marshal(obj, sWriter);
                builder = Response.ok(sWriter.toString());
            } catch (JAXBException e) {
                e.printStackTrace();
                builder = Response.status(Status.INTERNAL_SERVER_ERROR);
            }finally {
                try {
                    if(sWriter != null)
                        sWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.build();
    }
    
    private Response marshalList(Collection<Serializable> objects, String jaxbListName) {
        ResponseBuilder builder = null;
        if(objects == null || objects.isEmpty())
            builder = Response.status(Status.NOT_FOUND);
        else {
            List fList = new ArrayList(objects);
            ObjectList pList = new ObjectList(fList);
            JAXBContext jc;
            Writer sWriter = null;
            try {
                jc = JAXBContext.newInstance(findTypes(objects));
                Marshaller marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                QName qName = new QName(jaxbListName);
                JAXBElement<ObjectList> jaxbElement = new JAXBElement<ObjectList>(qName, ObjectList.class, pList);
                //marshaller.marshal(jaxbElement, System.out);
                sWriter = new StringWriter();
                marshaller.marshal(jaxbElement, sWriter);
                builder = Response.ok(sWriter.toString());
            } catch (JAXBException e) {
                e.printStackTrace();
                builder = Response.status(Status.INTERNAL_SERVER_ERROR);
            }finally {
                try {
                    if(sWriter != null)
                        sWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.build();
    }
    
    private Object unmarshalXML(String fqn, InputStream iStream) throws JAXBException, ClassNotFoundException {
        JAXBContext    jc = JAXBContext.newInstance(Class.forName(fqn));
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(iStream);
        return obj;
    }
    
    private <T> List<T> unmarshalXMLObjectList(InputStream iStream, Class<T>... clazzes) throws JAXBException {
        Source iSource = new StreamSource(iStream);
        JAXBContext jc = JAXBContext.newInstance(clazzes);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ObjectList<T> unmarshalledList = (ObjectList<T>) unmarshaller.unmarshal(iSource, ObjectList.class).getValue();
        return unmarshalledList.getItems();
    }
    
    private BatchExecutionCommandImpl unmarshalXMLCommandList(InputStream iStream, Class<?>... clazzes) throws JAXBException {
        Source iSource = new StreamSource(iStream);
        JAXBContext jc = JAXBContext.newInstance(clazzes);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        BatchExecutionCommandImpl batchCommands = (BatchExecutionCommandImpl) unmarshaller.unmarshal(iSource, BatchExecutionCommandImpl.class).getValue();
        return batchCommands;
    }
    
    private static <T> Class[] findTypes(Collection<T> c) {
        Set<Class> types = new HashSet<Class>();
        types.add(ObjectList.class);
        for(T o : c){
            if(o != null) {
                types.add(o.getClass());
            }
        }
        return types.toArray(new Class[0]);
    }
}

