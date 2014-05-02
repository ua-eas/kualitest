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

import org.htmlcleaner.TagNode;


public class HtmlTagInfo {
    private String type;
    private String name;
    private String id;
    private String text;

    public HtmlTagInfo() {
    }
    
    public HtmlTagInfo(String pageTitle, TagNode tag) {
        type = tag.getName();
        id = tag.getAttributeByName("id");
        name = tag.getAttributeByName("name");
        text = tag.getText().toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public String toString() {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append("tag-info: type=");
        retval.append(type);
        retval.append(", id=");
        retval.append(id);
        retval.append(", name=");
        retval.append(name);
        
        return retval.toString();
    }
}
