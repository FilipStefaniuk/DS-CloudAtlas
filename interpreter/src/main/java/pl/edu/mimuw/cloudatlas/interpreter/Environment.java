package pl.edu.mimuw.cloudatlas.interpreter;

	import java.util.HashMap;
	import java.util.Iterator;
	import java.util.List;
	import java.util.Map;

abstract class Environment {
	private final Map<String, Result> env = new HashMap<>();

	Environment(List<Result> values, List<String> columns) {
		Iterator<Result> valuesIterator = values.iterator();
		Iterator<String> columnsIterator = columns.iterator();

		while(valuesIterator.hasNext() && columnsIterator.hasNext()) {
			env.put(columnsIterator.next(), valuesIterator.next());
		}
	}

	Result getIdent(String ident) {
		return env.get(ident);
	}
}



