package pl.edu.mimuw.cloudatlas.agent.framework;

import java.io.Serializable;

public class Address implements Serializable {

    private String module;
    private Integer handler;

    public Address(String module, Integer handler) {
        this.module = module;
        this.handler = handler;
    }

    public Address(Class<? extends ModuleBase> clazz, Integer handler) {

        Module annotation = clazz.getAnnotation(Module.class);
        if (annotation == null)
            throw new IllegalArgumentException();

        this.module = annotation.name();
        this.handler = handler;
    }

    public String getModule() {
        return module;
    }

    public Integer getHandler() {
        return handler;
    }
}
