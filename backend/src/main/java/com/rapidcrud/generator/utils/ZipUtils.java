package com.rapidcrud.generator.utils;

import java.io.*;
import java.util.zip.*;

public class ZipUtils {

    public static void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zipOut = new ZipOutputStream(fos)
        ) {
            zipFile(sourceDir, sourceDir.getName(), zipOut);
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) return;

        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) zipOut.putNextEntry(new ZipEntry(fileName));
            else zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            zipOut.closeEntry();

            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}

