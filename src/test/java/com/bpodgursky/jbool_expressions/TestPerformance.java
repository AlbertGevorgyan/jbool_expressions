package com.bpodgursky.jbool_expressions;

import com.bpodgursky.jbool_expressions.rules.RuleSet;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.assertEquals;

public class TestPerformance {

  private static final int DEPTH = 1;

  @Before
  public void setUp() {
    reset();
  }

  private void reset() {
    Value.EQUALITY_CHECK_COUNTER = 0;
  }

  private static class Value {

    private static long EQUALITY_CHECK_COUNTER = 0;

    private final int label;

    Value(int label) {
      this.label = label;
    }

    @Override
    public boolean equals(Object o) {
      EQUALITY_CHECK_COUNTER++;
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Value value = (Value) o;
      return label == value.label;
    }

    @Override
    public int hashCode() {
      return Objects.hash(label);
    }

    @Override
    public String toString() {
      return "{" + label + '}';
    }
  }

  @Test
  public void testComplexAnd() {
    Expression<Value> deep = createDeepAnd(DEPTH);
    List<Variable<Value>> flattened = rangeClosed(0, DEPTH)
            .mapToObj(Value::new)
            .map(Variable::of)
            .collect(toList());
    Expression<Value> expected = And.of(flattened);

    assertEquals(expected, RuleSet.toCNF(deep));
    assertEquals(expected, RuleSet.toDNF(deep));
    System.out.println("DEEP: " + Value.EQUALITY_CHECK_COUNTER);

    reset();
    assertEquals(expected, RuleSet.toCNF(expected));
    assertEquals(expected, RuleSet.toDNF(expected));
    System.out.println("FLAT: " + Value.EQUALITY_CHECK_COUNTER);
  }

  private Expression<Value> createDeepAnd(int depth) {
    Value val = new Value(depth);
    Variable<Value> var = Variable.of(val);
    return depth == 0
            ? var
            : And.of(var, createDeepAnd(depth - 1));
  }

  @Test
  public void testComplexOr() {
    Expression<Value> deep = createDeepOr(DEPTH);
    List<Variable<Value>> flattened = rangeClosed(0, DEPTH)
            .mapToObj(Value::new)
            .map(Variable::of)
            .collect(toList());
    Expression<Value> expected = Or.of(flattened);

    assertEquals(expected, RuleSet.toCNF(deep));
    assertEquals(expected, RuleSet.toDNF(deep));
    System.out.println("DEEP: " + Value.EQUALITY_CHECK_COUNTER);

    reset();
    assertEquals(expected, RuleSet.toCNF(expected));
    assertEquals(expected, RuleSet.toDNF(expected));
    System.out.println("FLAT: " + Value.EQUALITY_CHECK_COUNTER);
  }

  private Expression<Value> createDeepOr(int depth) {
    Value val = new Value(depth);
    Variable<Value> var = Variable.of(val);
    return depth == 0
            ? var
            : Or.of(var, createDeepOr(depth - 1));
  }
}
