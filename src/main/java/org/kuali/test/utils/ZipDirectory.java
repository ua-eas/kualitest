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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectory {
	public ZipDirectory(File sourceDir, File targetFile) throws Exception {
		List<File> fileList = new ArrayList<File>();
		getAllFiles(sourceDir, fileList);
		writeZipFile(sourceDir, targetFile, fileList);
	}

	private void getAllFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, fileList);
            }
        }
	}

	private void writeZipFile(File sourceDir, File targetFile, List<File> fileList) throws Exception {
        ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(targetFile.getPath()));

			for (File file : fileList) {
				if (!file.isDirectory()) { // we only zip files, not directories
					addToZip(sourceDir, file, zos);
				}
			}

			zos.close();
		} 
        
        finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            }
            
            catch (Exception ex) {};
        }
	}

	private void addToZip(File sourceDir, File file, ZipOutputStream zos) throws Exception,
			IOException {

		FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(file);

            // we want the zipEntry's path to be a relative path that is relative
            // to the directory being zipped, so chop off the rest of the path
            String zipFilePath = file.getCanonicalPath().substring(sourceDir.getCanonicalPath().length() + 1,
                    file.getCanonicalPath().length());
            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
        }
        
        finally {
            if (fis != null) {
                fis.close();
            }
        }
	}
}