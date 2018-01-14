package pl.edu.mimuw.cloudatlas.webclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @RequestMapping(method = RequestMethod.GET)
    public List<String> listAgents() {
        return agentService.retriveAllAgents();
    }

    @RequestMapping(value = "{id}/set-contacts", method = RequestMethod.GET)
    public void setFallbackContacts(@PathVariable("id") String id) {
        agentService.setFallbackContacts(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public List<String> getZones(@PathVariable("id") String id) {
        return agentService.getZones(id);
    }

    @RequestMapping(value = "{id}/zone{zone}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getAttributes(@PathVariable("id") String id, @PathVariable("zone") String zone) {
        return agentService.getAttributes(id, zone);
    }

    @RequestMapping(value = "{id}/install", method = RequestMethod.GET)
    public void installQuery(@PathVariable("id") String id, @RequestParam("name")String name,
                             @RequestParam(value = "query", defaultValue = "")String query) {
        agentService.installQuery(id, name, query);
    }
}
