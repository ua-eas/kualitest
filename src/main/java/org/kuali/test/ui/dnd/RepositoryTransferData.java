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

package org.kuali.test.ui.dnd;

/**
 *
 * @author rbtucker
 * @param <T1>
 * @param <T2>
 */
public class RepositoryTransferData <T1, T2> {
    private T1 target;
    private T2 data;
    
    /**
     *
     * @param target
     * @param data
     */
    public RepositoryTransferData(T1 target, T2 data) {
        this.target = target;
        this.data = data;
    }
    
    /**
     *
     * @return
     */
    public T1 getTarget() {
        return target;
    }
    
    /**
     *
     * @return
     */
    public T2 getData() {
        return data;
    }
    
}
