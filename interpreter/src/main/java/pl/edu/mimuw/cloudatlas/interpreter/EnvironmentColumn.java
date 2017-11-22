package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.stream.Collectors;

public class EnvironmentColumn extends Environment{

    public EnvironmentColumn(Table table) {
        super(table.getColumns().stream().map(c -> new ResultColumn(table.getColumn(c))).collect(Collectors.toList()), table.getColumns());
    }

    @Override
    ResultColumn getIdent(String ident) {
        ResultColumn result = (ResultColumn) super.getIdent(ident);
        if (result == null)
            throw new NoSuchAttributeException(ident);
        return result;
    }
}
