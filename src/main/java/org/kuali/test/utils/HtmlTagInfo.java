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
    private String group = Constants.DEFAULT_HTML_GROUP;
    private String subgroup = Constants.DEFAULT_HTML_SUBGROUP;
    private String additionalIdentifiers;
    private String typeAttribute;
    private String nameAttribute;
    private String idAttribute;
    private String forAttribute;
    private String classAttribute;
    private String text;

    public HtmlTagInfo() {
    }
    
    public HtmlTagInfo(TagNode tag) {
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
        
        if (Utils.isValidCheckpointTag(this)) {
            checkGroups(tag);
        }
    }   

    private void checkGroups(TagNode tag) {
        TagNode parent = tag.getParent();
        
        while(parent != null) {
            if (Utils.isHtmlGroupMatch(parent)) {
                group = Utils.getHtmlGroup(parent);
            } else if (Utils.isHtmlSubgroupMatch(parent)) {
                subgroup = Utils.getHtmlSubgroup(parent);
            }
        }
    }
    
    private String getInputValue(TagNode tag, String typeAttribute, String nameAttribute) {
        String retval = null;
        
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(String subgroup) {
        this.subgroup = subgroup;
    }

    public String getAdditionalIdentifiers() {
        return additionalIdentifiers;
    }

    public void setAdditionalIdentifiers(String additionalIdentifiers) {
        this.additionalIdentifiers = additionalIdentifiers;
    }

    
    @Override
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
