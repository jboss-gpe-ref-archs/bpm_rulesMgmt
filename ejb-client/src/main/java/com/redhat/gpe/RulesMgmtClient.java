package com.redhat.gpe;

import java.util.Hashtable;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.Context;

import org.kie.services.remote.ejb.IRemoteRulesMgmtService;

public class RulesMgmtClient {

    private static final Logger log = Logger.getLogger(RulesMgmtClient.class.getName());
    private static final String GPE_EXTENSIONS_REMOTE_INTERFACE="ejb:/business-central/rulesMgmtService!org.kie.services.remote.ejb.IRemoteRulesMgmtService";
    private static final String DEPLOYMENT_ID = "deploymentId";

    public static void main(String args[])  throws Exception{
        String deploymentId = System.getProperty(DEPLOYMENT_ID);
        log.info("main() deploymentId = "+deploymentId);

        Hashtable jndiProps = new Hashtable();
        //jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        //jndiProps.put(Context.PROVIDER_URL,"remote://localhost:4447");
        jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        Context jndiContext = new InitialContext(jndiProps);

        IRemoteRulesMgmtService rMgmtService = (IRemoteRulesMgmtService)jndiContext.lookup(GPE_EXTENSIONS_REMOTE_INTERFACE);

        log.info("main() invoking logRules() with deploymentId = "+deploymentId);
        rMgmtService.logFacts(deploymentId);
        log.info("main() just invoked logRules()");
    }
}
