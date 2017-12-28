package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.util.Set;

public class ContactsMessage extends Message{

    private Set<ValueContact> contacts;

    public ContactsMessage(Set<ValueContact> contacts) {
        this.contacts = contacts;
    }

    public Set<ValueContact> getContacts() {
        return contacts;
    }
}
