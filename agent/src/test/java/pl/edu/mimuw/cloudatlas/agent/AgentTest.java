package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Assert;
import org.junit.Test;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class AgentTest {

    @Test
    public void getZMIByPathTest() throws Exception {
        Agent agent = new Agent();
        ZMI zmi = agent.getZMIByPathName(new PathName("/uw/violet07"));
        String result = ((ValueString) zmi.getAttributes().get("name")).getValue();
        Assert.assertEquals("violet07", result);
    }

//    @Test
//    public void getAttributesOfZoneTest() throws Exception {
//        Agent agent = new Agent();
//        System.out.println(agent.getAttributesOfZone(new PathName("/uw/violet07")));
//    }

    @Test
    public void getAgentZonesTest() throws Exception {
        Agent agent = new Agent();
        System.out.println(agent.getAgentZones());
    }
}
