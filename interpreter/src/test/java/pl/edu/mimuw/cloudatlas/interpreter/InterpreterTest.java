package pl.edu.mimuw.cloudatlas.interpreter;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class InterpreterTest {
	private ZMI root;

	private static PathName getPathName(ZMI zmi) {
		String name = ((ValueString)zmi.getAttributes().get("name")).getValue();
		return zmi.getFather() == null? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
	}
	
	private String executeQueries(ZMI zmi, String query) throws Exception {
		StringJoiner stringJoiner = new StringJoiner("\n");

		if(zmi.getSons().isEmpty())
			return "";

		for(ZMI son : zmi.getSons()) {
			String r = executeQueries(son, query);
			if (r != "")
				stringJoiner.add(r);
		}
		try {
			Interpreter interpreter = new Interpreter(zmi);
			Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
			List<QueryResult> result = interpreter.interpretProgram((new parser(lex)).pProgram());

			PathName zone = getPathName(zmi);
			for (QueryResult r : result) {
				stringJoiner.add(zone + ": " + r);
				zmi.getAttributes().addOrChange(r.getName(), r.getValue());
			}
		}catch (InterpreterException exception) {
			return stringJoiner.toString();
		}
		return stringJoiner.toString();
	}
	
	private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
			throws UnknownHostException {
		return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[] {
			ip1, ip2, ip3, ip4
		}));
	}

	@Before
	public void createTestHierarchy() throws ParseException, UnknownHostException {
		ValueContact violet07Contact = createContact("/uw/violet07", (byte)10, (byte)1, (byte)1, (byte)10);
		ValueContact khaki13Contact = createContact("/uw/khaki13", (byte)10, (byte)1, (byte)1, (byte)38);
		ValueContact khaki31Contact = createContact("/uw/khaki31", (byte)10, (byte)1, (byte)1, (byte)39);
		ValueContact whatever01Contact = createContact("/uw/whatever01", (byte)82, (byte)111, (byte)52, (byte)56);
		ValueContact whatever02Contact = createContact("/uw/whatever02", (byte)82, (byte)111, (byte)52, (byte)57);
		
		List<Value> list;
		
		root = new ZMI();
		root.getAttributes().add("level", new ValueInt(0l));
		root.getAttributes().add("name", new ValueString(null));
		root.getAttributes().add("owner", new ValueString("/uw/violet07"));
		root.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:10:17.342"));
		root.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		root.getAttributes().add("cardinality", new ValueInt(0l));
		
		ZMI uw = new ZMI(root);
		root.addSon(uw);
		uw.getAttributes().add("level", new ValueInt(1l));
		uw.getAttributes().add("name", new ValueString("uw"));
		uw.getAttributes().add("owner", new ValueString("/uw/violet07"));
		uw.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
		uw.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		uw.getAttributes().add("cardinality", new ValueInt(0l));
		
		ZMI pjwstk = new ZMI(root);
		root.addSon(pjwstk);
		pjwstk.getAttributes().add("level", new ValueInt(1l));
		pjwstk.getAttributes().add("name", new ValueString("pjwstk"));
		pjwstk.getAttributes().add("owner", new ValueString("/pjwstk/whatever01"));
		pjwstk.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
		pjwstk.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		pjwstk.getAttributes().add("cardinality", new ValueInt(0l));
		
		ZMI violet07 = new ZMI(uw);
		uw.addSon(violet07);
		violet07.getAttributes().add("level", new ValueInt(2l));
		violet07.getAttributes().add("name", new ValueString("violet07"));
		violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
		violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
		list = Arrays.asList(new Value[] {
			khaki31Contact, whatever01Contact
		});
		violet07.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			violet07Contact,
		});
		violet07.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
		violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
		violet07.getAttributes().add("num_cores", new ValueInt(3l));
		violet07.getAttributes().add("has_ups", new ValueBoolean(null));
		list = Arrays.asList(new Value[] {
			new ValueString("tola"), new ValueString("tosia"),
		});
		violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		violet07.getAttributes().add("expiry", new ValueDuration(13l, 12l, 0l, 0l, 0l));
		
		ZMI khaki31 = new ZMI(uw);
		uw.addSon(khaki31);
		khaki31.getAttributes().add("level", new ValueInt(2l));
		khaki31.getAttributes().add("name", new ValueString("khaki31"));
		khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
		khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
		list = Arrays.asList(new Value[] {
			violet07Contact, whatever02Contact,
		});
		khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			khaki31Contact
		});
		khaki31.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
		khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
		khaki31.getAttributes().add("num_cores", new ValueInt(3l));
		khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
		list = Arrays.asList(new Value[] {
			new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
		});
		khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki31.getAttributes().add("expiry", new ValueDuration(-13l, -11l, 0l, 0l, 0l));
		
		ZMI khaki13 = new ZMI(uw);
		uw.addSon(khaki13);
		khaki13.getAttributes().add("level", new ValueInt(2l));
		khaki13.getAttributes().add("name", new ValueString("khaki13"));
		khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
		khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			khaki13Contact,
		});
		khaki13.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("creation", new ValueTime((Long)null));
		khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		khaki13.getAttributes().add("num_cores", new ValueInt(null));
		khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki13.getAttributes().add("expiry", new ValueDuration((Long)null));
		
		ZMI whatever01 = new ZMI(pjwstk);
		pjwstk.addSon(whatever01);
		whatever01.getAttributes().add("level", new ValueInt(2l));
		whatever01.getAttributes().add("name", new ValueString("whatever01"));
		whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
		whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
		list = Arrays.asList(new Value[] {
			violet07Contact, whatever02Contact,
		});
		whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			whatever01Contact,
		});
		whatever01.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
		whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		whatever01.getAttributes().add("num_cores", new ValueInt(7l));
		list = Arrays.asList(new Value[] {
			new ValueString("rewrite")
		});
		whatever01.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));
		
		ZMI whatever02 = new ZMI(pjwstk);
		pjwstk.addSon(whatever02);
		whatever02.getAttributes().add("level", new ValueInt(2l));
		whatever02.getAttributes().add("name", new ValueString("whatever02"));
		whatever02.getAttributes().add("owner", new ValueString("/uw/whatever02"));
		whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
		list = Arrays.asList(new Value[] {
			khaki31Contact, whatever01Contact,
		});
		whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			whatever02Contact,
		});
		whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
		whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
		whatever02.getAttributes().add("num_cores", new ValueInt(13l));
		list = Arrays.asList(new Value[] {
			new ValueString("odbc")
		});
		whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));
	}

	@Test
	public void query1Test() throws Exception {
		String result = executeQueries(root, "SELECT 2 + 2 AS two_plus_two");
		Assert.assertEquals("/uw: two_plus_two: 4\n/pjwstk: two_plus_two: 4\n/: two_plus_two: 4", result);

		System.out.print(result);
	}

	@Test
	public void  query2Test() throws  Exception {
		String result = executeQueries(root, "SELECT to_integer((to_double(3) - 5.6) / 11.0 + to_double(47 * (31 - 15))) AS math WHERE true");
		Assert.assertEquals("/uw: math: 751\n/pjwstk: math: 751\n/: math: 751", result);
		System.out.print(result);
	}

	@Test
	public void query3Test() throws Exception {
		String result = executeQueries(root, "SELECT count(members) AS members_count");
		Assert.assertEquals("/uw: members_count: 3\n/pjwstk: members_count: 2", result);
		System.out.print(result);
	}

	@Test
	public void query4Test() throws Exception {
		String result = executeQueries(root, "SELECT first(99, name) AS new_contacts ORDER BY cpu_usage DESC NULLS LAST, num_cores ASC NULLS FIRST");
		Assert.assertEquals("/uw: new_contacts: [khaki13, violet07, khaki31]\n/pjwstk: new_contacts: [whatever01, whatever02]\n/: new_contacts: [uw, pjwstk]", result);
		System.out.print(result);
	}

	@Test
	public void query5Test() throws Exception {
		String result = executeQueries(root, "SELECT count(num_cores - size(some_names)) AS sth");
		Assert.assertEquals("/uw: sth: 2\n", result);
		System.out.print(result);
	}

	@Test
	public void query6Test() throws Exception {
		String result = executeQueries(root, "SELECT min(sum(distinct(2 * level)) + 38 * size(contacts)) AS sth WHERE num_cores < 8");
		Assert.assertEquals("/uw: sth: 80\n/pjwstk: sth: 80\n/: sth: NULL", result);
		System.out.print(result);
	}

	@Test
	public void query7Test() throws Exception {
		String result = executeQueries(root, "SELECT first(1, name) + last(1,name) AS concat_name WHERE num_cores >= (SELECT min(num_cores) ORDER BY timestamp) ORDER BY creation ASC NULLS LAST");
		Assert.assertEquals("/uw: concat_name: [violet07, khaki31]\n/pjwstk: concat_name: [whatever01, whatever02]", result);
		System.out.print(result);
	}

	@Test
	public void query8Test() throws Exception {
		String result = executeQueries(root, "SELECT sum(cardinality) AS cardinality");
		Assert.assertEquals("/uw: cardinality: 3\n/pjwstk: cardinality: 2\n/: cardinality: 5", result);
		System.out.print(result);
	}

	@Test
	public void query9Test() throws Exception {
		String result = executeQueries(root, "SELECT land(cpu_usage < 0.5) AS cpu_ok");
		Assert.assertEquals("/uw: cpu_ok: false\n/pjwstk: cpu_ok: true", result);
		System.out.print(result);
	}

	@Test
	public void query10Test() throws Exception {
		String result = executeQueries(root, "SELECT min(name) AS min_name, to_string(first(1, name)) AS max_name ORDER BY name DESC");
		Assert.assertEquals("/uw: min_name: khaki13\n/uw: max_name: [violet07]\n/pjwstk: min_name: whatever01\n/pjwstk: max_name: [whatever02]\n/: min_name: pjwstk\n/: max_name: [uw]", result);
		System.out.print(result);
	}

	@Test
	public void query11Test() throws Exception {
		String result = executeQueries(root, "SELECT epoch() AS epoch, land(timestamp > epoch()) AS afterY2K");
		Assert.assertEquals("/uw: epoch: 2000/01/01 00:00:00.000\n/uw: afterY2K: true\n/pjwstk: epoch: 2000/01/01 00:00:00.000\n/pjwstk: afterY2K: true\n/: epoch: 2000/01/01 00:00:00.000\n/: afterY2K: true", result);
		System.out.print(result);
	}

	@Test
	public void query12Test() throws Exception {
		String result = executeQueries(root, "SELECT min(timestamp) + (max(timestamp) - epoch()) / 2 AS t2");
		Assert.assertEquals("/uw: t2: 2019/04/16 05:31:30.000\n/pjwstk: t2: 2019/04/16 08:48:30.000\n/: t2: 2019/04/16 07:12:19.684", result);
		System.out.print(result);
	}

	@Test
	public void query13Test() throws Exception {
		String result = executeQueries(root, "SELECT lor(unfold(some_names) + \"xx\" REGEXP \"([a-z]*)atkax([a-z]*)\") AS beatka");
		Assert.assertEquals("/uw: beatka: true", result);
		System.out.print(result);
	}

	@Test
	public void query14Test() throws Exception {
		String result = executeQueries(root, "SELECT (SELECT avg(cpu_usage) WHERE false) AS smth");
		Assert.assertEquals("/uw: smth: NULL\n/pjwstk: smth: NULL", result);
		System.out.print(result);
	}

	@Test
	public void query15Test() throws Exception {
		String result = executeQueries(root, "SELECT avg(cpu_usage) AS cpu_usage WHERE (SELECT sum(cardinality)) > (SELECT to_integer((1 + 2 + 3 + 4) / 5))");
		Assert.assertEquals("/uw: cpu_usage: 0.5\n/pjwstk: cpu_usage: NULL\n/: cpu_usage: NULL", result);
		System.out.print(result);
	}

	@Test
	public void query16Test() throws Exception {
		String result = executeQueries(root, "SELECT ceil(to_double(min(num_cores)) / 1.41) AS sth");
		Assert.assertEquals("/uw: sth: 3.0\n/pjwstk: sth: 5.0", result);
		System.out.print(result);
	}

	@Test
	public void query17Test() throws Exception {
		String result = executeQueries(root, "SELECT floor(5.0 / 1.9) AS fl");
		Assert.assertEquals("/uw: fl: 2.0\n/pjwstk: fl: 2.0\n/: fl: 2.0", result);
		System.out.print(result);
	}

	@Test
	public void query18Test() throws Exception {
		String result = executeQueries(root, "SELECT to_time(\"2013/07/05 12:54:32.098\") + to_duration(6811) AS tim");
		Assert.assertEquals("/uw: tim: 2013/07/05 12:54:38.909\n/pjwstk: tim: 2013/07/05 12:54:38.909\n/: tim: 2013/07/05 12:54:38.909", result);
		System.out.print(result);
	}

	@Test
	public void query19Test() throws Exception {
		String result = executeQueries(root, "SELECT avg(cpu_usage * to_double(num_cores)) AS cpu_load, sum(num_cores) AS num_cores");
		Assert.assertEquals("/uw: cpu_load: 2.7\n/uw: num_cores: 6\n/pjwstk: cpu_load: 2.95\n/pjwstk: num_cores: 20", result);
		System.out.print(result);
	}
}
