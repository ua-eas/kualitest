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

package org.kuali.test.utils;

import com.gargoylesoftware.htmlunit.util.NameValuePair;


public class UpdateableNameValuePair extends NameValuePair {
    private String name;
    private String value;

    public UpdateableNameValuePair(String name, String value) {
        super(name, value);
        this.name = name;
        this.value = value;
    }

    public UpdateableNameValuePair(NameValuePair nvp) {
        super( nvp.getName(), nvp.getValue());
        this.name = nvp.getName();
        this.value = nvp.getValue();
    }
    
    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
