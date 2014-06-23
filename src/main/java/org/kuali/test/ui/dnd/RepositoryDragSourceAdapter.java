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

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceEvent;

/**
 *
 * @author rbtucker
 */
public class RepositoryDragSourceAdapter extends DragSourceAdapter {
    @Override
    public void dragOver(DragSourceDragEvent dsde) {
        switch(dsde.getTargetActions()) {
            case DnDConstants.ACTION_LINK:
                dsde.getDragSourceContext().setCursor(DragSource.DefaultLinkDrop);
                break;
            case DnDConstants.ACTION_COPY:
                dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
                break;
            case DnDConstants.ACTION_MOVE:
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                break;
            default:
                dsde.getDragSourceContext().setCursor(DragSource.DefaultLinkNoDrop);
                break;
        }
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
        dse.getDragSourceContext().setCursor(DragSource.DefaultCopyNoDrop);
    }
    
}
