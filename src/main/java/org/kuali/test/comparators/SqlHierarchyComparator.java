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
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.ui.components.sqlquerytree.TableData;


public class SqlHierarchyComparator implements Comparator <TableData>{
    @Override
    public int compare(TableData o1, TableData o2) {
        int retval = 0;
        
        DefaultMutableTreeNode tn1 = o1.getTreeNode();
        DefaultMutableTreeNode tn2 = o2.getTreeNode();
        
        if ((tn1 != null) && (tn2 != null)) {
            Integer i1 = Integer.valueOf(tn1.getLevel());
            Integer i2 = Integer.valueOf(tn2.getLevel());
            
            retval = i1.compareTo(i2);
        }
        
        if (retval == 0) {
            retval = o1.getName().compareTo(o2.getName());
        }
        
        return retval;
    }
}
