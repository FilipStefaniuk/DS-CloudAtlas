package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.*;

import java.util.ArrayList;

public class ResultList extends Result {

    private final Value value;

    public ResultList(Value value) {
        this.value = value;
    }

    @Override
    protected Result binaryOperationTyped(Result.BinaryOperation operation, ResultSingle right) {
        if (this.getValue().isNull() || right.getValue().isNull())
            return new ResultSingle(ValueNull.getInstance());
        ArrayList<Value> newList = new ArrayList<Value>();
        for (Value v : getList())
            newList.add(operation.perform(v, right.getValue()));
        Type type = (newList.isEmpty() ? ((TypeCollection)value.getType()).getElementType() : newList.get(0).getType());

        return new ResultList(new ValueList(newList, type));
    }

    @Override
    protected Result binaryOperationTyped(Result.BinaryOperation operation, ResultColumn right) {
        throw new UnsupportedOperationException("Binary operations not supported on (ResultColumn, ResultList).");
    }

    @Override
    protected Result binaryOperationTyped(Result.BinaryOperation operation, ResultList right) {
        throw new UnsupportedOperationException("Binary operations not supported on (ResultList, ResultList).");
    }

    @Override
    public Result unaryOperation(UnaryOperation operation) {
        if (this.getValue().isNull())
            return new ResultList(ValueNull.getInstance());
        ArrayList<Value> newList = new ArrayList<>();
        for (Value v : getList())
            newList.add(operation.perform(v));
        Type type = (newList.isEmpty() ? ((TypeCollection)value.getType()).getElementType() : newList.get(0).getType());
        return new ResultList(new ValueList(newList, type));
    }

    @Override
    protected Result callMe(BinaryOperation operation, Result left) {
        return left.binaryOperationTyped(operation, this);
    }

    @Override
    public ResultSingle aggregationOperation(AggregationOperation operation) {
        return new ResultSingle(operation.perform(getList()));
    }

    @Override
    public Result transformOperation(TransformOperation operation) {
        return new ResultList(operation.perform(getList()));
    }


    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public ValueList getList() {
        return (ValueList) value;
    }

    @Override
    public ValueList getColumn() {
        throw new UnsupportedOperationException("Not a ResultColumn.");
    }

    @Override
    public Result filterNulls() {
        return new ResultList(filterNullsList(getList()));
    }

    @Override
    public Result first(int size) {
        return new ResultSingle(firstList(getList(), size));
    }

    @Override
    public Result last(int size) {
        return new ResultSingle(lastList(getList(), size));
    }

    @Override
    public Result random(int size) {
        return new ResultSingle(randomList(getList(), size));
    }

    @Override
    public Result convertTo(Type to) {
        ValueList list = new ValueList(new ArrayList<>(), to);
        for (Value v: getList())
            list.add(v.convertTo(to));
        return new ResultList(list);
    }

    @Override
    public ResultSingle isNull() {
        return new ResultSingle(new ValueBoolean(value.isNull()));
    }

    @Override
    public Type getType() {
        return value.getType();
    }
}
