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

package org.kuali.test.handlers.parameter;

import java.util.Random;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.KualiApplication;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.w3c.dom.Document;


public abstract class BaseParameterHandler implements ParameterHandler {
    private String commentText;

    @Override
    public abstract String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp);

    @Override
    public abstract String getDescription();

    @Override
    public String toString() {
        String retval = getClass().getName();
        int pos1 = retval.lastIndexOf(".");
        int pos2 = retval.lastIndexOf("Handler");
        
        if ((pos1 > -1) && (pos2 > pos1)) {
            retval = retval.substring(pos1+1, pos2);
        } else if (pos1 > -1) {
            retval = retval.substring(pos1+1);
        } 
        
        return retval;
    }

    @Override
    public int compareTo(ParameterHandler o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public String getCommentText() {
        String retval = commentText;
        commentText = null;
        return retval;
    }
    
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    @Override
    public boolean isReplaceByValue() {
        return false;
    }

    @Override
    public boolean isValidForApplication(KualiApplication.Enum app) {
        return true;
    }

    @Override
    public boolean isExistingPropertyValueRequired() {
        return false;
    }

    @Override
    public boolean isTextEntryAllowed() {
        return false;
    }

    @Override
    public boolean isTextEntryRequired() {
        return false;
    }

    protected int getRandomIndex(int maxValue) {
        return new Random(System.currentTimeMillis()).nextInt(maxValue);
    }
    
    @Override
    public boolean isResubmitRequestAllowed() {
        return true;
    }
}
