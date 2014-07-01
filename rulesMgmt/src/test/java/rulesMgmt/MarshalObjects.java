package rulesMgmt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;

import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Driver;
import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Policy;
import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.PolicyTracker;

public class MarshalObjects {

    public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
    	marshallTestDomain();
    }
    public static void marshallTestDomain() throws JsonGenerationException, JsonMappingException, IOException {
        Policy pObj = new Policy();
        pObj.setPolicyId(5);
        pObj.setPolicyName("Azra Policy");
        
        ObjectMapper jsonMapper = new ObjectMapper();
        String marshalledString = jsonMapper.writeValueAsString(pObj);
        System.out.println("main() policy json = "+marshalledString);
        
        Policy unmarshalledPObj =jsonMapper.readValue(marshalledString, Policy.class);
        System.out.println("main() policy obj = "+unmarshalledPObj);
        
        PolicyTracker pTracker = new PolicyTracker();
        pTracker.setProcessedCount(0);
        System.out.println("main() pTracker = "+jsonMapper.writeValueAsString(pTracker));
    }
    
    public Response executeTest(@PathParam("deploymentId") final String deploymentId ) throws JAXBException {
    	List<GenericCommand<?>> commandList = new ArrayList<GenericCommand<?>>();
    	Policy pObj = new Policy();
    	pObj.setPolicyId(234);
    	pObj.setPolicyName("werwer");
    	InsertObjectCommand insertPolicy = new InsertObjectCommand();
    	insertPolicy.setObject(pObj);
    	insertPolicy.setOutIdentifier("policyOut");
    	commandList.add(insertPolicy);
    	
    	Driver dObj = new Driver();
    	dObj.setDriverId(234234);
    	dObj.setDriverName("azra");
    	InsertObjectCommand insertDriver = new InsertObjectCommand();
    	insertDriver.setObject(dObj);
    	insertDriver.setOutIdentifier("driverOut");
    	commandList.add(insertDriver);
    	
    	PolicyTracker pTracker = new PolicyTracker();
    	pTracker.setProcessedCount(0);
    	SetGlobalCommand setGlobal = new SetGlobalCommand();
    	setGlobal.setObject(pTracker);
    	setGlobal.setIdentifier("pTracker");
    	commandList.add(setGlobal);
    	
    	FireAllRulesCommand fireCommand = new FireAllRulesCommand();
    	commandList.add(fireCommand);
    	BatchExecutionCommandImpl batchCommand = new BatchExecutionCommandImpl(commandList);
    	
    	JAXBContext jc = JAXBContext.newInstance(BatchExecutionCommandImpl.class,Driver.class, Policy.class, PolicyTracker.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(batchCommand, System.out);
    	
    	return Response.status(Status.OK).build();
    }

}
