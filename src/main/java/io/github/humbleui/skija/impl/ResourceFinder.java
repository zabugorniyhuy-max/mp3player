package io.github.humbleui.skija.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

public final class ResourceFinder implements Closeable {
    private final String _moduleName;

    public ResourceFinder(String moduleName) {
        this._moduleName = moduleName;
    }

    public URL findResource(String resourcePath) throws IOException {
        URL url = ResourceFinder.class.getClassLoader().getResource(resourcePath);
        if (url != null) {
            return url;
        }
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl != null) {
            url = ccl.getResource(resourcePath);
            if (url != null) {
                return url;
            }
        }
        return ClassLoader.getSystemResource(resourcePath);
    }

    @Override
    public void close() throws IOException {
    }
}

