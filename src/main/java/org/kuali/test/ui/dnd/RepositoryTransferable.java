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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author rbtucker
 * @param <T1>
 * @param <T2>
 */
public class RepositoryTransferable <T1, T2> implements Transferable {
    private RepositoryTransferData<T1, T2> transferData;
    private DataFlavor dataFlavor;
    
    /**
     *
     * @param transferData
     * @param dataFlavor
     */
    public RepositoryTransferable(RepositoryTransferData transferData, DataFlavor dataFlavor) {
        this.transferData = transferData;
        this.dataFlavor = dataFlavor;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {dataFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(dataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(dataFlavor)) {
            return transferData;
        } else {
           throw new UnsupportedFlavorException(flavor); 
        }
    }
}
