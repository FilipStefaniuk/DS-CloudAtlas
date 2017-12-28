package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.util.Set;

public class ZonesMessage extends Message {
    Set<PathName> zones;

    public ZonesMessage(Set<PathName> zones) {
        this.zones = zones;
    }

    public Set<PathName> getZones() {
        return zones;
    }
}
