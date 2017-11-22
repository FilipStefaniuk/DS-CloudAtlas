package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.ValueNull;

import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentSingle extends Environment {

    public EnvironmentSingle(TableRow row, List<String> columns) {
        super(row.asList().stream().map(ResultSingle::new).collect(Collectors.toList()), columns);
    }

    @Override
    ResultSingle getIdent(String ident) {
        ResultSingle result = (ResultSingle) super.getIdent(ident);

        if (result == null)
            return new ResultSingle(ValueNull.getInstance());

        return result;
    }
}
