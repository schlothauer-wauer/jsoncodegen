package de.lisaplus.atlas.codegen.test.base

import static org.junit.Assert.assertTrue

class FileHelper {
    static void removeDirectoryIfExists(String pathToDir) {
        File f = new File(pathToDir)
        if (f.exists()) {
            f.deleteDir()
        }
        assertTrue(!f.exists())
    }
}
