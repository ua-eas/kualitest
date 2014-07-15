/*
 * Copyright 2014 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.test.runner.execution;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.ValueType;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.output.TestOutput;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public abstract class AbstractOperationExecution implements OperationExecution {
    private Operation op;
    private Map<String, String> parameterMap = new HashMap<String, String>();
    private Map<String, CheckpointProperty> propertyMap = new HashMap<String, CheckpointProperty>();
    private TestExecutionContext context;
    
    /**
     *
     * @param context
     * @param op
     */
    public AbstractOperationExecution (TestExecutionContext context, Operation op) {
        this.op = op;
        this.context = context;
        
        if (op.getCheckpointOperation() != null) {
            if (op.getCheckpointOperation().getInputParameters() != null) {
                for (Parameter param : op.getCheckpointOperation().getInputParameters().getParameterArray()) {
                    parameterMap.put(param.getName(), param.getValue());
                }
            }

            if (op.getCheckpointOperation().getCheckpointProperties() != null) {
                for (CheckpointProperty property : op.getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                    propertyMap.put(property.getPropertyName(), property);
                }
            }
        }
    }
    
    /**
     *
     * @param name
     * @return
     */
    protected String getParameter(String name) {
        return parameterMap.get(name);
    }
    
    /**
     *
     * @return
     */
    protected TestOutput initTestOutput() {
        return new TestOutput(op);
    }

    /**
     *
     * @param name
     * @return
     */
    protected CheckpointProperty getProperty(String name) {
        return propertyMap.get(name);
    }
    
    /**
     *
     * @return
     */
    protected Operation getOperation() {
        return op;
    }
    
    /**
     *
     * @param inputValue
     * @param type
     * @return
     * @throws ParseException
     */
    protected Object getValueForType(String inputValue, ValueType.Enum type) throws ParseException {
        Object retval = null;
        
        // default to string
        if (type == null) {
            type = ValueType.STRING;
        }
        
        switch (type.intValue()) {
            case ValueType.INT_STRING:
                if (StringUtils.isBlank(inputValue)) {
                    retval = null;
                } else {
                    retval = inputValue;
                }
                break;
            case ValueType.INT_INT:
                retval = Integer.parseInt(inputValue);
                break;
            case ValueType.INT_LONG:
                retval = Long.parseLong(inputValue);
                break;
            case ValueType.INT_DATE:
                retval = Constants.DEFAULT_DATE_FORMAT.parse(inputValue);
                break;
            case ValueType.INT_DOUBLE:
                retval = Double.parseDouble(inputValue);
                break;
            case ValueType.INT_TIMESTAMP:
                retval = Constants.DEFAULT_TIMESTAMP_FORMAT.parse(inputValue);
                break;
            case ValueType.INT_BOOLEAN:
                retval = Boolean.parseBoolean(inputValue);
                break;
        }

        return retval;
    }
    
    /**
     *
     * @param inputValue
     * @param type
     * @return
     * @throws ParseException
     */
    protected List buildComparisonArrayFromString(String inputValue, ValueType.Enum type) throws ParseException {
        List retval = new ArrayList();
        
        StringTokenizer st = new StringTokenizer(inputValue, ",");
        
        while (st.hasMoreTokens()) {
            Object o = getValueForType(st.nextToken(), type);
            
            if (o != null) {
                retval.add(o);
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param cp
     * @return
     * @throws ParseException
     */
    protected Object getComparisonValue(CheckpointProperty cp) throws ParseException {
        Object retval = null;
        ValueType.Enum type = cp.getValueType();
        String comparisonValue = cp.getPropertyValue();
        
        if (!ComparisonOperator.NULL.equals(cp.getOperator())) {
            if (ComparisonOperator.IN.equals(cp.getOperator())) {
                retval = buildComparisonArrayFromString(comparisonValue, type);
            } else {
                retval = getValueForType(comparisonValue, type);
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param cp
     * @return
     * @throws TestException
     */
    protected boolean evaluateCheckpointProperty(CheckpointProperty cp) throws TestException {
        boolean retval = false;
        
        try {
            Object comparisonValue = getComparisonValue(cp);
            ComparisonOperator.Enum comparisonOperator = cp.getOperator();
            Object value = getValueForType(cp.getActualValue(), cp.getValueType());
            
            if (ComparisonOperator.NULL.equals(cp.getOperator())) {
                retval = ((value == null) && (comparisonValue == null));
            } else if ((value == null) || (comparisonValue == null)) {
                getTestExecutionContext().updateCounts(cp.getOnFailure());
                throw new TestException("input value is null, comparison value = " + comparisonValue, op);
            } else {
                ValueType.Enum type = cp.getValueType();
                
                if (type == null) {
                    type = ValueType.STRING;
                }
                
                ValueType.Enum inputType = getInputValueType(value);
                if (type.equals(inputType)) {
                    if (ComparisonOperator.IN.equals(cp.getOperator()) && (comparisonValue instanceof List)) {
                        Iterator <Comparable> it = ((List)comparisonValue).iterator();
                        
                        while (it.hasNext()) {
                            if (it.next().equals(value)) {
                                retval = true;
                                break;
                            }
                        }
                    } else {
                        Comparable c1 = (Comparable)comparisonValue;
                        Comparable c2 = (Comparable)value;
                        switch (comparisonOperator.intValue()) {
                            case ComparisonOperator.INT_EQUAL_TO:
                                retval = c1.equals(c2);
                                break;
                            case ComparisonOperator.INT_GREATER_THAN:
                                retval = (c1.compareTo(c2) < 0);
                                break;
                            case ComparisonOperator.INT_GREATER_THAN_OR_EQUAL:
                                retval = (c1.compareTo(c2) <= 0);
                                break;
                            case ComparisonOperator.INT_LESS_THAN:
                                retval = (c1.compareTo(c2) > 0);
                                break;
                            case ComparisonOperator.INT_LESS_THAN_OR_EQUAL:
                                retval = (c1.compareTo(c2) >= 0);
                                break;
                            case ComparisonOperator.INT_BETWEEN:
                                
                                break;
                            case ComparisonOperator.INT_NOT_NULL:
                                retval = true;
                                break;
                        }
                    }
                    
                    if (retval) {
                        getTestExecutionContext().updateCounts(cp.getOnFailure());
                    }
                } else {
                    getTestExecutionContext().updateCounts(cp.getOnFailure());
                    throw new TestException("input type (" + inputType + ") comparison type (" + type + ") mismatch" + comparisonValue, op);
                }
            }
        }
        
        catch (ParseException ex) {
            getTestExecutionContext().updateCounts(cp.getOnFailure());
            throw new TestException("Exception occurrred while parsing data for checkpoint comparison - " + ex.toString(), op, ex);
        }

        if(!retval) {
            getTestExecutionContext().updateCounts(cp.getOnFailure());
        }
        
        return retval;
    }
    
    private ValueType.Enum getInputValueType(Object value) {
        ValueType.Enum retval = ValueType.STRING;
        
        if (value != null) {
            if (value instanceof String) {
                retval = ValueType.STRING;
            } else if (value instanceof Integer) {
                retval = ValueType.INT;
            } else if (value instanceof Double) {
                retval = ValueType.DOUBLE;
            } else if (value instanceof Date) {
                retval = ValueType.DATE;
            } else if (value instanceof Boolean) {
                retval = ValueType.BOOLEAN;
            }
        }
        
        return retval;
    }

    protected Class getClassForValueType(ValueType.Enum e) {
        Class retval = String.class;
        
        if (e != null) {
            if (e.equals(ValueType.STRING)) {
                retval = String.class;
            } else if (e.equals(ValueType.INT)) {
                retval = Integer.class;
            } else if (e.equals(ValueType.DOUBLE)) {
                retval = Double.class;
            } else if (e.equals(ValueType.DATE) || e.equals(ValueType.TIMESTAMP)) {
                retval = Calendar.class;
            } else if (e.equals(ValueType.BOOLEAN)) {
                retval = Boolean.class;
            } else if (e.equals(ValueType.LONG)) {
                retval = Long.class;
            }
        }
        
        return retval;
    }

    /**
     *
     * @return
     */
    public TestExecutionContext getTestExecutionContext() {
        return context;
    }
}
