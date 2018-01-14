package pl.edu.mimuw.cloudatlas.model;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class ValueQuery extends Value implements Serializable {

    private static final String ALGORITHM = "SHA256withRSA";
    private final String name;
    private final String query;
    private final Long issued;
    private byte[] signature;

    public ValueQuery(String name, String query) {
        this.name = name;
        this.query = query;
        this.issued = System.currentTimeMillis();
    }

    public final void sign(PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance(ALGORITHM);
        privateSignature.initSign(privateKey);
        privateSignature.update((name + "&" + query + "&" + Long.toHexString(issued)).getBytes("UTF-8"));

        signature = privateSignature.sign();
    }

    public final boolean verify(PublicKey publicKey) throws Exception {

        if (signature == null) {
            return false;
        }

        Signature publicSignature = Signature.getInstance(ALGORITHM);
        publicSignature.initVerify(publicKey);
        publicSignature.update((name + "&" + query + "&" + Long.toHexString(issued)).getBytes("UTF-8"));

        return publicSignature.verify(signature);
    }

    public Long getIssued() {
        return issued;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public boolean isNull() {
        return name == null && query == null;
    }

    @Override
    public Type getType() {
        return TypePrimitive.QUERY;
    }

    @Override
    public Value getDefaultValue() {
        return new ValueQuery(null, null);
    }

    @Override
    public Value convertTo(Type to) {
        switch (to.getPrimaryType()) {
            case QUERY:
                return this;
            case STRING:
                if(isNull())
                    return ValueString.NULL_STRING;
                else
                    return new ValueString(query);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Value isEqual(Value value) {
        sameTypesOrThrow(value, Operation.EQUAL);
        ValueQuery valueQuery = (ValueQuery) value.convertTo(TypePrimitive.QUERY);
        return new ValueBoolean(
                name.equals(valueQuery.name) && issued.equals(valueQuery.issued));
    }
}
