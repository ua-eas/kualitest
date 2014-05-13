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

package org.kuali.test.comparators;

import java.util.Comparator;
import org.kuali.test.TagMatchAttribute;
import org.kuali.test.TagMatchType;
import org.kuali.test.TagMatcher;
import org.kuali.test.handlers.HtmlTagHandler;


public class HtmlTagHandlerComparator implements Comparator <HtmlTagHandler>{
    @Override
    public int compare(HtmlTagHandler o1, HtmlTagHandler o2) {

        int retval = o1.getTagHandler().getTagName().compareTo(o2.getTagHandler().getTagName());
        
        if (retval == 0) {
            retval = o1.getTagHandler().getHandlerClassName().compareTo(o2.getTagHandler().getHandlerClassName());

            TagMatcher tm1 = getCurrentNodeTagMatcher(o1);
            TagMatcher tm2 = getCurrentNodeTagMatcher(o2);
            
            if (retval == 0) {
                // need to sort so we hit most specific matchers first then fall through to more generic
                if (tm1.getMatchAttributes().sizeOfMatchAttributeArray() > tm2.getMatchAttributes().sizeOfMatchAttributeArray()) {
                    retval = -1;
                } else if (tm1.getMatchAttributes().sizeOfMatchAttributeArray() < tm2.getMatchAttributes().sizeOfMatchAttributeArray()) {
                    retval = 1;
                } else {
                    retval = 0;
                }
            }
            
            if (retval == 0) {
                boolean o1HasWildcards = hasWildcardMatchAttributes(tm1);
                boolean o2HasWildcards = hasWildcardMatchAttributes(tm2);
                
                if (!o1HasWildcards && o2HasWildcards) {
                    retval = -1;
                } else if (o1HasWildcards && !o2HasWildcards) {
                    retval = 1;
                }
            }
        }
        
        return retval;
    }
    
    private boolean hasWildcardMatchAttributes(TagMatcher tm) {
        boolean retval = false;
        
        if (tm.getMatchAttributes().sizeOfMatchAttributeArray() > 0) {
            for (TagMatchAttribute att : tm.getMatchAttributes().getMatchAttributeArray()) {
                if (att.getValue().contains("*")) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }
    
    private TagMatcher getCurrentNodeTagMatcher(HtmlTagHandler th) {
        TagMatcher retval = null;
        
        for (TagMatcher tm : th.getTagHandler().getTagMatchers().getTagMatcherArray()) {
            if (TagMatchType.CURRENT.equals(tm.getMatchType())) {
                retval = tm;
                break;
            }
        }
        
        return retval;
    }
}
