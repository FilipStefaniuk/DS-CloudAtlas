package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class InterpreterMain {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        ZMI root = createDefaultInterpreterHierarchy();

        while(scanner.hasNext()) {
            try {
                String[] inputQuery = scanner.next().split(":");
                if (inputQuery.length == 2) {
                    Attribute queryName = new Attribute(inputQuery[0].trim());
                    installQuery(root, queryName, new ValueString(inputQuery[1]));
                    System.out.println(execute(root));
                    uninstallQuery(root, queryName);
                } else {
                    System.err.println("Wrong input format, should be: <&attribute_name>: <SQL_query1_1>; ...");
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void installQuery(ZMI zmi, Attribute attribute, ValueString value) throws Exception {
        validateQuery(zmi, attribute, value);
        addQuery(zmi, attribute, value);
    }

    private static void addQuery(ZMI zmi, Attribute attribute, ValueString value) {

        if (zmi.getSons().isEmpty()) {
            return;
        }

        for(ZMI son : zmi.getSons()) {
            addQuery(son, attribute, value);
        }

        zmi.getAttributes().add(attribute, value);
    }

    private static void validateQuery(ZMI zmi, Attribute attribute, ValueString valueString) {

        if (zmi.getSons().isEmpty())
            return;

        if(zmi.getAttributes().getOrNull(attribute) != null)
            throw new InternalInterpreterException("Attribute " + attribute.getName() + " already exists.");

        for(ZMI son : zmi.getSons()) {
            validateQuery(son, attribute, valueString);
        }

        Interpreter interpreter = new Interpreter(zmi);
        List<QueryResult> old_result = new ArrayList<>();
        List<QueryResult> results;

        try {
            results = interpreter.executeQuery(valueString.getValue());
            for (Map.Entry<Attribute, Value> entry : zmi.getAttributes()) {
                if (entry.getKey().getName().startsWith("&")) {
                    String query = ((ValueString) entry.getValue()).getValue();
                    old_result.addAll(interpreter.executeQuery(query));
                }
            }
        } catch(Exception e) {
            throw new InternalInterpreterException("Query parsing error.");
        }

        Set<Attribute> old_attributes = new HashSet<>(old_result.stream().map(QueryResult::getName).collect(Collectors.toList()));

        for (QueryResult r : results) {
            if (old_attributes.contains(r.getName()))
                throw new InternalInterpreterException("Other query modifies attribute " + r.getName() + ".");
        }
    }

    public static void uninstallQuery(ZMI zmi, Attribute attribute) throws Exception{

        if(zmi.getSons().isEmpty())
            return;

        Value query = zmi.getAttributes().getOrNull(attribute);
        if (query != null) {

            zmi.getAttributes().remove(attribute);

            Interpreter interpreter = new Interpreter(zmi);
            List<QueryResult> results = interpreter.executeQuery(((ValueString) query).getValue());

            for (QueryResult r : results) {
                zmi.getAttributes().remove(r.getName());
            }
        }

        for(ZMI son : zmi.getSons()) {
            uninstallQuery(son, attribute);
        }
    }

    public static String execute(ZMI zmi) throws Exception {
        if(zmi.getSons().isEmpty())
            return "";

        StringJoiner stringJoiner = new StringJoiner("\n");
        for(ZMI son : zmi.getSons()) {
            String r = execute(son);
            if (!r.isEmpty())
                stringJoiner.add(r);
        }

        List<QueryResult> result = new ArrayList<>();
        Interpreter interpreter = new Interpreter(zmi);

        for(Map.Entry<Attribute, Value> entry : zmi.getAttributes()) {
            if (entry.getKey().getName().startsWith("&")) {
                String query = ((ValueString) entry.getValue()).getValue();
                result.addAll(interpreter.executeQuery(query));
            }
        }

        for(QueryResult r : result)
            zmi.getAttributes().addOrChange(r.getName(), r.getValue());

        PathName zone = getPathName(zmi);
        for (QueryResult r : result) {
            stringJoiner.add(zone + ": " + r);
        }

        return stringJoiner.toString();
    }

    public static PathName getPathName(ZMI zmi) {
        String name = ((ValueString)zmi.getAttributes().get("name")).getValue();
        return zmi.getFather() == null? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
    }

    static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
            throws UnknownHostException {
        return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[] {
                ip1, ip2, ip3, ip4
        }));
    }

    public static ZMI createDefaultInterpreterHierarchy() {
        try {

            ValueContact violet07Contact = createContact("/uw/violet07", (byte) 10, (byte) 1, (byte) 1, (byte) 10);
            ValueContact khaki13Contact = createContact("/uw/khaki13", (byte) 10, (byte) 1, (byte) 1, (byte) 38);
            ValueContact khaki31Contact = createContact("/uw/khaki31", (byte) 10, (byte) 1, (byte) 1, (byte) 39);
            ValueContact whatever01Contact = createContact("/uw/whatever01", (byte) 82, (byte) 111, (byte) 52, (byte) 56);
            ValueContact whatever02Contact = createContact("/uw/whatever02", (byte) 82, (byte) 111, (byte) 52, (byte) 57);

            List<Value> list;

            // /
            ZMI root = new ZMI();
            root.getAttributes().add("level", new ValueInt(0L));
            root.getAttributes().add("name", new ValueString(null));

            // /uw
            ZMI uw = new ZMI(root);
            root.addSon(uw);
            uw.getAttributes().add("level", new ValueInt(1L));
            uw.getAttributes().add("name", new ValueString("uw"));

            // /pjwstk
            ZMI pjwstk = new ZMI(root);
            root.addSon(pjwstk);
            pjwstk.getAttributes().add("level", new ValueInt(1L));
            pjwstk.getAttributes().add("name", new ValueString("pjwstk"));

            // /uw/violet07
            ZMI violet07 = new ZMI(uw);
            uw.addSon(violet07);
            violet07.getAttributes().add("level", new ValueInt(2L));
            violet07.getAttributes().add("name", new ValueString("violet07"));
            violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
            violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
            violet07.getAttributes().add("contacts", new ValueSet(new HashSet<>(Arrays.asList(violet07Contact, khaki31Contact, khaki31Contact)), TypePrimitive.CONTACT));
            violet07.getAttributes().add("cardinality", new ValueInt(1L));
            violet07.getAttributes().add("members", new ValueSet(new HashSet<>(Arrays.asList(violet07Contact, khaki13Contact, khaki31Contact)), TypePrimitive.CONTACT));
            violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
            violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
            violet07.getAttributes().add("num_cores", new ValueInt(3L));
            violet07.getAttributes().add("num_processes", new ValueInt(131L));
            violet07.getAttributes().add("has_ups", new ValueBoolean(null));
            violet07.getAttributes().add("some_names", new ValueList(Arrays.asList(new ValueString("tola"), new ValueString("tosia")), TypePrimitive.STRING));
            violet07.getAttributes().add("expiry", new ValueDuration(13L, 12L, 0L, 0L, 0L));

            // /uw/khaki31
            ZMI khaki31 = new ZMI(uw);
            uw.addSon(khaki31);
            khaki31.getAttributes().add("level", new ValueInt(2L));
            khaki31.getAttributes().add("name", new ValueString("khaki31"));
            khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
            khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
            khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<>(Arrays.asList(violet07Contact, khaki31Contact, khaki13Contact)), TypePrimitive.CONTACT));
            khaki31.getAttributes().add("cardinality", new ValueInt(1L));
            khaki31.getAttributes().add("members", new ValueSet(new HashSet<>(Arrays.asList(violet07Contact, khaki13Contact, khaki31Contact)), TypePrimitive.CONTACT));
            khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
            khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
            khaki31.getAttributes().add("num_cores", new ValueInt(3L));
            khaki31.getAttributes().add("num_processes", new ValueInt(124L));
            khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
            khaki31.getAttributes().add("some_names", new ValueList(Arrays.asList(new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina")), TypePrimitive.STRING));
            khaki31.getAttributes().add("expiry", new ValueDuration(-13L, -11L, 0L, 0L, 0L));

            // /uw/khaki13
            ZMI khaki13 = new ZMI(uw);
            uw.addSon(khaki13);
            khaki13.getAttributes().add("level", new ValueInt(2L));
            khaki13.getAttributes().add("name", new ValueString("khaki13"));
            khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
            khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
            khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<>(Arrays.asList(violet07Contact, khaki13Contact)), TypePrimitive.CONTACT));
            khaki13.getAttributes().add("cardinality", new ValueInt(1L));
            khaki13.getAttributes().add("members", new ValueSet(new HashSet<>(Arrays.asList(khaki13Contact, khaki31Contact)), TypePrimitive.CONTACT));
            khaki13.getAttributes().add("creation", new ValueTime((Long) null));
            khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
            khaki13.getAttributes().add("num_cores", new ValueInt(null));
            khaki13.getAttributes().add("num_processes", new ValueInt(107L));
            khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
            khaki13.getAttributes().add("some_names", new ValueList(new ArrayList<>(), TypePrimitive.STRING));
            khaki13.getAttributes().add("expiry", new ValueDuration((Long) null));

            // /pjwstk/whatever01
            ZMI whatever01 = new ZMI(pjwstk);
            pjwstk.addSon(whatever01);
            whatever01.getAttributes().add("level", new ValueInt(2L));
            whatever01.getAttributes().add("name", new ValueString("whatever01"));
            whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
            whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
            whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<>(Arrays.asList(whatever01Contact, whatever02Contact)), TypePrimitive.CONTACT));
            whatever01.getAttributes().add("cardinality", new ValueInt(1L));
            whatever01.getAttributes().add("members", new ValueSet(new HashSet<>(Arrays.asList(whatever01Contact, whatever02Contact)), TypePrimitive.CONTACT));
            whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
            whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
            whatever01.getAttributes().add("num_cores", new ValueInt(7L));
            whatever01.getAttributes().add("num_processes", new ValueInt(215L));
            whatever01.getAttributes().add("php_modules", new ValueList(Arrays.asList(new ValueString("rewrite")), TypePrimitive.STRING));

            // /pjwstk/whatever02
            ZMI whatever02 = new ZMI(pjwstk);
            pjwstk.addSon(whatever02);
            whatever02.getAttributes().add("level", new ValueInt(2L));
            whatever02.getAttributes().add("name", new ValueString("whatever02"));
            whatever02.getAttributes().add("owner", new ValueString("/uw/whatever02"));
            whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
            whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<>(Arrays.asList(whatever01Contact, whatever02Contact)), TypePrimitive.CONTACT));
            whatever02.getAttributes().add("cardinality", new ValueInt(1L));
            whatever02.getAttributes().add("members", new ValueSet(new HashSet<>(Arrays.asList(whatever01Contact, whatever02Contact)), TypePrimitive.CONTACT));
            whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
            whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
            whatever02.getAttributes().add("num_cores", new ValueInt(13L));
            whatever02.getAttributes().add("php_modules", new ValueList(Arrays.asList(new ValueString("odbc")), TypePrimitive.STRING));

            return root;
        } catch (ParseException | UnknownHostException e) {
            return null;
        }
    }
}
