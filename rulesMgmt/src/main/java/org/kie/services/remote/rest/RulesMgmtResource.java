package org.kie.services.remote.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Policy;

@Stateless
@Path("/RulesMgmtResource")
public class RulesMgmtResource {

    @EJB(lookup="java:global/business-central/rulesMgmtService!org.kie.services.remote.rest.IRulesMgmtService")
    IRulesMgmtService rProxy;

    private Logger log = LoggerFactory.getLogger("RulesMgmtResource");

    @PUT
    @Path("/rulesLifecycle/{deploymentId: .*}/")
    public Response testRulesLifecycle(@PathParam("deploymentId") final String deploymentId) {
    	rProxy.setGlobal(deploymentId, "sBuilder", new StringBuilder());
    	rProxy.insertFact(deploymentId, new Policy());
    	int numRulesFired = rProxy.fireAllRules(deploymentId);
    	//rProxy.dispose(deploymentId);
    	ResponseBuilder builder = Response.ok("number of rules Fired = "+numRulesFired+"\n");
        return builder.build();
    }

    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET -HAccept:text/plain localhost:8080/business-central/rest/RulesMgmtResource/sanityCheck
     */
    @GET
    @Path("/sanityCheck")
    @Produces({ "text/plain" })
    public Response sanityCheck() {
        ResponseBuilder builder = Response.ok("good to go\n");
        return builder.build();
    }

}
