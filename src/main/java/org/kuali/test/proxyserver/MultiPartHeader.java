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

package org.kuali.test.proxyserver;

import com.google.common.net.HttpHeaders;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class MultiPartHeader {
    private Map <String, String> parameters = new HashMap<String, String>();
    private String contentType;
    
    public MultiPartHeader(String data) {
        StringTokenizer st = new StringTokenizer(data);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith(HttpHeaders.CONTENT_TYPE)) {
                if (st.hasMoreTokens()) {
                    contentType = st.nextToken().trim();
                    break;
                }
            }
            
            int pos = token.indexOf("=");
            
            if (pos > -1) {
                String key = token.substring(0, pos).trim();
                String value = token.substring(pos+1).replace("\"", "").trim();
                parameters.put(key, value);
            }
        }
    }
    
    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getContentType() {
        return contentType;
    }
}
