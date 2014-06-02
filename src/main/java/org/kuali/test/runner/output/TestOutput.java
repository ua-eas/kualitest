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

package org.kuali.test.runner.output;

import java.util.Date;
import org.kuali.test.Checkpoint;
import org.kuali.test.Operation;


public class TestOutput {
    private Date startTime;
    private Date endTime;
    private String testName;
    private String testSuiteName;
    private String checkpointName;
    private String checkpointType;
    
    public TestOutput(Operation op) {
        startTime = new Date();
        if (op.getCheckpointOperation() != null) {
            Checkpoint cp = op.getCheckpointOperation();
            checkpointName = cp.getName();
            testName = cp.getTestName();
            testSuiteName = cp.getTestSuite();
            checkpointType = cp.getType().toString();
        }
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getTestName() {
        return testName;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public String getCheckpointName() {
        return checkpointName;
    }

    public String getCheckpointType() {
        return checkpointType;
    }
}
