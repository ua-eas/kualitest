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

package org.kuali.test.runner;

import java.util.Date;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.TestSuite;


public class TestExecutionContext extends Thread {
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private boolean completed = false;

    public TestExecutionContext(TestSuite testSuite, Date scheduledTime) {
        this.testSuite = testSuite;
        this.scheduledTime = scheduledTime;
        
        // if no scheduled time then run immediately
        if (scheduledTime == null) {
            startTest();
        }
    }

    public TestExecutionContext(TestSuite testSuite) {
        this(testSuite, null);
    }

    public TestExecutionContext(KualiTest kualiTest, Date scheduledTime) {
        this.kualiTest = kualiTest;
        this.scheduledTime = scheduledTime;
        
        // if no scheduled time then run immediately
        if (scheduledTime == null) {
            startTest();
        }
    }

    public TestExecutionContext(KualiTest kualiTest) {
        this(kualiTest, null);
    }
    
    @Override
    public void run() {
        startTime= new Date();
        
        endTime= new Date();
        completed = true;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public KualiTest getKualiTest() {
        return kualiTest;
    }

    public void setKualiTest(KualiTest kualiTest) {
        this.kualiTest = kualiTest;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public void startTest() {
        start();
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isCompleted() {
        return completed;
    }
}
