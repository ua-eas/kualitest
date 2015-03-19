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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class UnzipFile {
    private static final int BUFFER_SIZE = 4096;
    
    public UnzipFile(File zippedFile, File destDirectory) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zippedFile));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            File f = new File(destDirectory.getPath() + File.separator + entry.getName());
            if (!f.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, f);
            } else {
                f.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, File f) throws IOException {
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        BufferedOutputStream bos = null;
        
        try {
            bos = new BufferedOutputStream(new FileOutputStream(f));
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
        
        finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            }
            
            catch (Exception ex) {};
        }
    }
}
