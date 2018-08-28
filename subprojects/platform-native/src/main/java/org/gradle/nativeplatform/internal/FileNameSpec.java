
package org.gradle.nativeplatform.internal;

import java.io.File;
import org.gradle.api.specs.Spec;


public class FileNameSpec implements Spec<File> {
    private final String fileName;

    public FileNameSpec(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSatisfiedBy(File file) {
        return fileName.equals(file.getName());
    }
}
