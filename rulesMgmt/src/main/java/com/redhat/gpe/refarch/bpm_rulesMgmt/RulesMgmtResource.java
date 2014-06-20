package com.redhat.gpe.refarch.bpm_rulesMgmt;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.drools.core.common.DefaultFactHandle;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Policy;
import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.PolicyList;
import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.PolicyTracker;

/*
 * Application specific RESTful API that exposes the functionality provided by the rules management CDI bean.
 * It's expected that users of this reference architecture will customize this class to support thier application-specific domain model.
 */
@Stateless
@Path("/RulesMgmtResource")
public class RulesMgmtResource {

    @EJB(lookup="java:global/business-central/rulesMgmtService!com.redhat.gpe.refarch.bpm_rulesMgmt.IRulesMgmtService")
    IRulesMgmtService rProxy;

    private Logger log = LoggerFactory.getLogger("RulesMgmtResource");
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X POST -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/Policy.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/fact/
     */
    @POST
    @Path("/{deploymentId: .*}/fact")
    @Consumes({"application/json","application/xml"})
    @Produces({ "application/xml" })
    public Response insertFact(@PathParam("deploymentId") final String deploymentId, Policy pObj) {
        DefaultFactHandle fHandle = (DefaultFactHandle)rProxy.insertFact(deploymentId, pObj);
        ResponseBuilder builder = marshallObject(DefaultFactHandle.class, fHandle);
        return builder.build();
    }
   
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X POST -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/PolicyTracker.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/global/pTracker
     */
    @POST
    @Path("/{deploymentId: .*}/global/{identifier: .*}/")
    @Consumes({"application/json","application/xml"})
    @Produces({ "text/plain" })
    public Response setGlobal(@PathParam("deploymentId") final String deploymentId, @PathParam("identifier") final String identifier, PolicyTracker pTracker) {
        rProxy.setGlobal(deploymentId, identifier, pTracker);
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
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/facts
     */
    @GET
    @Path("/{deploymentId: .*}/facts")
    @Produces({ "application/xml" })
    public Response getFacts(@PathParam("deploymentId") final String deploymentId) {
        Collection<Serializable> facts = rProxy.getFacts(deploymentId);
        List fList = new ArrayList(facts);
        PolicyList pList = new PolicyList(fList);
        JAXBContext jc;
        ResponseBuilder builder = null;
        Writer sWriter = null;
        try {
            jc = JAXBContext.newInstance(PolicyList.class, Policy.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            QName qName = new QName("policyList");
            JAXBElement<PolicyList> jaxbElement = new JAXBElement<PolicyList>(qName, PolicyList.class, pList);
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
        return builder.build();
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
    	ResponseBuilder builder = marshallObject(Policy.class, fact);
    	return builder.build();
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
     * sample usage :
     *  curl -v -u jboss:brms -X POST docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/rulesLifecycle
     */
    @POST
    @Path("/{deploymentId: .*}/rulesLifecycle/")
    public Response testRulesLifecycle(@PathParam("deploymentId") final String deploymentId) {
        rProxy.setGlobal(deploymentId, "policyTracker", new StringBuilder());
        rProxy.insertFact(deploymentId, new Policy());
        int numRulesFired = rProxy.fireAllRules(deploymentId);
        rProxy.removeFacts(deploymentId);
        ResponseBuilder builder = Response.ok("number of rules Fired = "+numRulesFired+"\n");
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

    private ResponseBuilder marshallObject(Class classObj, Object obj) {
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
    	return builder;
    }
}
