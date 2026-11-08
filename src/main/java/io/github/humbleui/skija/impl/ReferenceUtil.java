package io.github.humbleui.skija.impl;

import java.lang.ref.Reference;

public class ReferenceUtil {
    public static void reachabilityFence(Object ref) {
        Reference.reachabilityFence(ref);
    }
}

