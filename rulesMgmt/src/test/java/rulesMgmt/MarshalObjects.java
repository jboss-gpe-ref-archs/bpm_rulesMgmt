package rulesMgmt;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.Policy;
import com.redhat.gpe.refarch.bpm_rulesMgmt.domain.PolicyTracker;

public class MarshalObjects {

    public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
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

}
