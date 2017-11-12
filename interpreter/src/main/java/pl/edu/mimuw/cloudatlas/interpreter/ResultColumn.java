package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.*;

import java.util.ArrayList;

public class ResultColumn extends Result{

    private final Value value;

    public ResultColumn(Value value) {
        this.value = value;
    }

    @Override
    protected Result binaryOperationTyped(Result.BinaryOperation operation, ResultSingle right) {
        if (this.getValue().isNull() || right.getValue().isNull())
            return new ResultColumn(ValueNull.getInstance());

        ArrayList<Value> newList = new ArrayList<>();
        for (Value v : getColumn())
            newList.add(operation.perform(v, right.getValue()));
        Type t = (newList.isEmpty() ? ((TypeCollection)value.getType()).getElementType() : newList.get(0).getType());
        return new ResultColumn(new ValueList(newList, t));
    }

    @Override
    protected Result binaryOperationTyped(Result.BinaryOperation operation, ResultColumn right) {
        return new ResultColumn(operation.perform(value, right.value));
    }

    @Override
    protected Result binaryOperationTyped(Result.BinaryOperation operation, ResultList right) {
        throw new InternalInterpreterException("Binary operation type not supported on (ResultColumn, ResultList).");
    }

    @Override
    public Result unaryOperation(UnaryOperation operation) {
        return new ResultSingle(operation.perform(value));
    }

    @Override
    protected Result callMe(BinaryOperation operation, Result left) {
        return left.binaryOperationTyped(operation, this);
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public ValueList getList() {
        throw new UnsupportedOperationException("Not a ResultList.");
    }

    @Override
    public ValueList getColumn() {
        return ((ValueList) value);
    }

    @Override
    public ResultSingle aggregationOperation(AggregationOperation operation) {
        return new ResultSingle(operation.perform(getColumn()));
    }

    @Override
    public Result transformOperation(TransformOperation operation) {
        return new ResultList(operation.perform(getColumn()));
    }

    @Override
    public Result filterNulls() {
        return new ResultColumn(filterNullsList(getColumn()));
    }

    @Override
    public Result first(int size) {
        return new ResultColumn(firstList(getColumn(), size));
    }

    @Override
    public Result last(int size) {
        return new ResultColumn(lastList(getColumn(), size));
    }

    @Override
    public Result random(int size) {
        return new ResultColumn(randomList(getColumn(), size));
    }

    @Override
    public ResultColumn convertTo(Type to) {
        return new ResultColumn(value.convertTo(to));
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
