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
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.ValueType;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.output.TestOutput;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public abstract class AbstractOperationExecution implements OperationExecution {
    private Operation op;
    private Map<String, String> parameterMap = new HashMap<String, String>();
    private Map<String, CheckpointProperty> propertyMap = new HashMap<String, CheckpointProperty>();
    private TestExecutionContext context;
    
    public AbstractOperationExecution(Checkpoint cp) {
        if (cp != null) {
            mapCheckpointParametersAndProperties(cp);
        }
    }

    /**
     *
     * @param context
     * @param op
     */
    public AbstractOperationExecution (TestExecutionContext context, Operation op) {
        this.op = op;
        this.context = context;

        if (op.getCheckpointOperation() != null) {
            mapCheckpointParametersAndProperties(op.getCheckpointOperation());
        }
    }

    public AbstractOperationExecution () {
        if (op.getCheckpointOperation() != null) {
            mapCheckpointParametersAndProperties(op.getCheckpointOperation());
        }
    }

    private void mapCheckpointParametersAndProperties(Checkpoint cp) {
        loadCheckpointParameters(cp);
        loadCheckpointProperties(cp);
    }
    
    private void loadCheckpointParameters(Checkpoint cp) {
        if (cp.getInputParameters() != null) {
            for (Parameter param : cp.getInputParameters().getParameterArray()) {
                parameterMap.put(param.getName(), param.getValue());
            }
        }
    }
    
    private void loadCheckpointProperties(Checkpoint cp) {
        if (cp.getCheckpointProperties() != null) {
            for (CheckpointProperty property : cp.getCheckpointProperties().getCheckpointPropertyArray()) {
                propertyMap.put(property.getPropertyName(), property);
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
    
    private boolean isTestExecutionParameter(String input) {
        return (StringUtils.isNotBlank(input) 
            && input.contains(Constants.TEST_EXECUTION_PARAMETER_PREFIX) 
            && input.contains(Constants.TEST_EXECUTION_PARAMETER_SUFFIX));
    }
    
    private String getTestExecutionParameterValue(String parameterName) {
        String retval = null;
        
        for (TestExecutionParameter tep : getTestExecutionContext().getTestExecutionContextParameters()) {
            if (tep.getName().equalsIgnoreCase(parameterName)) {
                if (Utils.isGeneratedIdParameterHandler(tep.getParameterHandler())) {
                    retval = Utils.parseGeneratedIdParameterValue(tep.getValue());
                } else {
                    retval = tep.getValue();
                }
                break;
            }
        }   
        
        return retval;
    }
    
    private String getTestExecutionParameterName(String input) {
        String retval = input;
        
        if (StringUtils.isNotBlank(input)) {
            int pos1 = input.indexOf(Constants.TEST_EXECUTION_PARAMETER_PREFIX) +  Constants.TEST_EXECUTION_PARAMETER_PREFIX.length();
            int pos2 = input.indexOf(Constants.TEST_EXECUTION_PARAMETER_SUFFIX);


            if (pos2 > pos1) {
                retval = input.substring(pos1, pos2).trim();
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
        
        if (isTestExecutionParameter(comparisonValue)) {
            String pname = getTestExecutionParameterName(comparisonValue);
            String pval = getTestExecutionParameterValue(pname);
            
            comparisonValue = comparisonValue.replace((Constants.TEST_EXECUTION_PARAMETER_PREFIX + pname + Constants.TEST_EXECUTION_PARAMETER_SUFFIX), pval);
        }
        
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
     * @param testWrapper
     * @param cp
     * @return
     * @throws TestException 
     */
    protected boolean evaluateCheckpointProperty(KualiTestWrapper testWrapper, CheckpointProperty cp) throws TestException {
        boolean retval = false;
        
        try {
            Object comparisonValue = getComparisonValue(cp);
            ComparisonOperator.Enum comparisonOperator = cp.getOperator();
            Object value = getValueForType(cp.getActualValue(), cp.getValueType());
            
            if (ComparisonOperator.NULL.equals(cp.getOperator())) {
                retval = ((value == null) && (comparisonValue == null));
            } else if ((value == null) || (comparisonValue == null)) {
                if (((cp.getOperator() == null) || ComparisonOperator.EQUAL_TO.equals(cp.getOperator())) 
                    && (value == null) && (comparisonValue == null)) {
                    retval = true;
                } else {
                    throw new TestException("input value is null, comparison value = " + comparisonValue, op, cp.getOnFailure());
                }
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
                        if (ValueType.STRING.equals(type)) {
                            String s1 = (String)comparisonValue;
                            String s2 = (String)value;
                            if (StringUtils.isNotBlank(s1)) {
                                comparisonValue = s1.trim();
                            }
                        
                            if (StringUtils.isNotBlank(s2)) {
                                value = s2.trim();
                            }
                        }
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
                } else {
                    throw new TestException("input type (" + inputType + ") comparison type (" + type + ") mismatch" + comparisonValue, op, cp.getOnFailure());
                }
            }
        }
        
        catch (ParseException ex) {
            throw new TestException("Exception occurred while parsing data for checkpoint comparison - " + ex.toString(), op, ex);
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
