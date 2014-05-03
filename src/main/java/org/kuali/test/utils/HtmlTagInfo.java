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

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.TagNode;


public class HtmlTagInfo {
    private String tagType;
    private String typeAttribute;
    private String nameAttribute;
    private String idAttribute;
    private String forAttribute;
    private String classAttribute;
    private String text;

    public HtmlTagInfo() {
    }
    
    public HtmlTagInfo(String pageTitle, TagNode tag) {
        tagType = tag.getName();
        typeAttribute = tag.getAttributeByName("type");
        idAttribute = tag.getAttributeByName("id");
        nameAttribute = tag.getAttributeByName("name");
        forAttribute = tag.getAttributeByName("for");
        classAttribute = tag.getAttributeByName("class");
        
        if (Utils.isHtmlInputTag(tagType)) {
            text = getInputValue(tag, typeAttribute, nameAttribute);
        } else if (Utils.isHtmlSelectTag(tagType)) {
            text = getSelectedSelectOption(tag);
        } else {
            text = Utils.cleanDisplayText(tag.getText().toString());
        }
    }

    private String getInputValue(TagNode tag, String typeAttribute, String nameAttribute) {
        String retval = "";
        
        if (Constants.HTML_INPUT_TYPE_RADIO.equalsIgnoreCase(typeAttribute)) {
            retval = getSelectedRadioValue(tag, typeAttribute, nameAttribute);
        } else if (Constants.HTML_INPUT_TYPE_CHECKBOX.equalsIgnoreCase(typeAttribute)) {
            retval = getCheckedValues(tag, typeAttribute, nameAttribute);
        } else {
            retval = tag.getAttributeByName("value");
        }
        
        return retval;
    }

    private String getSelectedRadioValue(TagNode tag, String typeAttribute, String nameAttribute) {
        String retval = "";
        
        TagNode[] childTags = tag.getParent().getChildTags();
        
        for (int i = 0; i < childTags.length; ++i) {
            if (Utils.isHtmlInputTag(childTags[i].getName()) 
                && typeAttribute.equals(Constants.HTML_INPUT_TYPE_RADIO)
                && nameAttribute.equals(childTags[i].getAttributeByName("name"))) {
                if (StringUtils.isNotBlank(childTags[i].getAttributeByName("checked"))) {
                    retval = childTags[i].getAttributeByName("value");
                    break;
                }
            }
        }
        
        return retval;
    }
    
    private String getCheckedValues(TagNode tag, String typeAttribute, String nameAttribute) {
        StringBuilder retval = new StringBuilder(64);
        
        String comma = "";
        
        TagNode[] childTags = tag.getParent().getChildTags();
        
        for (int i = 0; i < childTags.length; ++i) {
            if (Utils.isHtmlInputTag(childTags[i].getName()) 
                && typeAttribute.equals(Constants.HTML_INPUT_TYPE_CHECKBOX)
                && nameAttribute.equals(childTags[i].getAttributeByName("name"))) {
                if (StringUtils.isNotBlank(childTags[i].getAttributeByName("checked"))) {
                    retval.append(comma);
                    retval.append(childTags[i].getAttributeByName("value"));
                    comma = ",";
                }
            }
        }
        
        return retval.toString();
    }

    private String getSelectedSelectOption(TagNode selectTag) {
        String retval = "";
        
        TagNode[] childTags = selectTag.getChildTags();
        
        for (int i = 0; i < childTags.length; ++i) {
            if (Utils.isHtmlOptionTag(childTags[i].getName())) {
                if (StringUtils.isNotBlank(childTags[i].getAttributeByName("selected"))) {
                    retval = childTags[i].getAttributeByName("value");
                    break;
                }
            }
        }
        
        return retval;
    }
    
    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getTypeAttribute() {
        return typeAttribute;
    }

    public void setTypeAttribute(String typeAttribute) {
        this.typeAttribute = typeAttribute;
    }

    public String getNameAttribute() {
        return nameAttribute;
    }

    public void setNameAttribute(String nameAttribute) {
        this.nameAttribute = nameAttribute;
    }

    public String getIdAttribute() {
        return idAttribute;
    }

    public void setIdAttribute(String idAttribute) {
        this.idAttribute = idAttribute;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = Utils.cleanDisplayText(text);
    }

    public String getForAttribute() {
        return forAttribute;
    }

    public void setForAttribute(String forAttribute) {
        this.forAttribute = forAttribute;
    }

    public String getClassAttribute() {
        return classAttribute;
    }

    public void setClassAttribute(String classAttribute) {
        this.classAttribute = classAttribute;
    }
    
    
    public String toString() {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append("tag-info: tagType=");
        retval.append(tagType);
        retval.append(", id=");
        retval.append(idAttribute);
        retval.append(", name=");
        retval.append(nameAttribute);
        retval.append(", type=");
        retval.append(typeAttribute);
        
        return retval.toString();
    }
}
