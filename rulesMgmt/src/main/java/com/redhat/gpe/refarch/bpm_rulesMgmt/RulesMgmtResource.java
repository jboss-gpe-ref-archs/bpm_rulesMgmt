package com.redhat.gpe.refarch.bpm_rulesMgmt;

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

import org.drools.core.common.DefaultFactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.ObjectList;

/*
 * Application specific RESTful API that exposes the functionality provided by the rules management CDI bean.
 * It's expected that users of this reference architecture will customize this class to support thier application-specific domain model.
 */
@Stateless
@Path("/RulesMgmtResource")
public class RulesMgmtResource {

    private static final String FACT_LIST = "factList";
    private static final String FACT_HANDLE_LIST = "factHandleList";

    @EJB(lookup="java:global/business-central/rulesMgmtService!com.redhat.gpe.refarch.bpm_rulesMgmt.IRulesMgmtService")
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
     *  curl -v -u jboss:brms -X POST -H "Content-Type:application/xml" -d @rulesMgmt/src/test/resources/PolicyTracker.xml docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/global/pTracker
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
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/factHandles
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
           unmarshalXMLList(DefaultFactHandle.class, fHandleStream);
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
     *   curl -v -u jboss:brms -X PUT docker_bpms:8080/business-central/rest/RulesMgmtResource/com.redhat.gpe.refarch.bpm_rulesMgmt:processTier:1.0/rules
     */
    @PUT
    @Path("/{deploymentId: .*}/rules")
    @Produces({ "text/plain" } )
    public Response logRules(@PathParam("deploymentId") final String deploymentId) {
        rProxy.logRules(deploymentId);
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
    
    private <T> List<T> unmarshalXMLList(Class<T> clazz, InputStream iStream) throws JAXBException {
        Source iSource = new StreamSource(iStream);
        JAXBContext jc = JAXBContext.newInstance(ObjectList.class, clazz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ObjectList<T> objectList = (ObjectList<T>) unmarshaller.unmarshal(iSource, ObjectList.class).getValue();
        return objectList.getItems();
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

