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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.kuali.test.utils.Constants;


public class MultiPartHeader {
    private Map <String, String> parameters = new HashMap<String, String>();
    private String[] PARAMETER_NAMES = {
        "name",
        "filename"
    };
    
    private String contentType;
    
    public MultiPartHeader(String data) throws IOException {
        LineNumberReader lnr = null;
        
        try {
            lnr = new LineNumberReader(new StringReader(data));
            String line = null;

            while ((line = lnr.readLine()) != null) {
                if (line.startsWith(Constants.CONTENT_DISPOSITION)) {
                    for (String param : PARAMETER_NAMES) {
                        int pos = line.indexOf(param);
                        if (pos > -1) {
                            pos += (param.length() + 2);
                            int pos2 = line.indexOf("\"", pos);

                            if ((pos > -1) && (pos2 > -1) && (pos2 > pos)) {
                                parameters.put(param, line.substring(pos, pos2));
                            }
                        }
                    }
                }

                if (line.startsWith(Constants.HTTP_RESPONSE_CONTENT_TYPE)) {
                    int pos = line.indexOf(Constants.SEPARATOR_COLON);

                    if (pos > -1) {
                        contentType = line.substring(pos+1).trim();
                    }
                }
            }
        }
        
        finally {
            try {
                if (lnr != null) {
                    lnr.close();
                }
            }
            
            catch (Exception ex) {};
        }
    }
    
    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getContentType() {
        return contentType;
    }
}
