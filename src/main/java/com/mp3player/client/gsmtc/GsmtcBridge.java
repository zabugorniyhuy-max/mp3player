/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Library
 */
package com.mp3player.client.gsmtc;

import com.mp3player.client.gsmtc.GsmtcLoader;
import com.sun.jna.Library;

public interface GsmtcBridge
extends Library {
    public static final GsmtcBridge INSTANCE = GsmtcBridge.loadLibrary();

    public static GsmtcBridge loadLibrary() {
        return GsmtcLoader.load();
    }

    public int gsmtc_init();

    public void gsmtc_update();

    public int gsmtc_has_track();

    public int gsmtc_get_status();

    public int gsmtc_get_title(byte[] var1, int var2);

    public int gsmtc_get_artist(byte[] var1, int var2);

    public int gsmtc_get_album(byte[] var1, int var2);

    public int gsmtc_get_source(byte[] var1, int var2);

    public double gsmtc_get_position();

    public double gsmtc_get_duration();

    public int gsmtc_get_thumbnail_version();

    public int gsmtc_get_thumbnail_size();

    public int gsmtc_get_thumbnail_data(byte[] var1, int var2);

    public int gsmtc_play();

    public int gsmtc_pause();

    public int gsmtc_toggle_play_pause();

    public int gsmtc_next();

    public int gsmtc_previous();

    public int gsmtc_seek(double var1);

    public float gsmtc_get_volume();

    public int gsmtc_set_volume(float var1);

    public void gsmtc_cleanup();

    public void gsmtc_shutdown();

    public void browser_set_parent_hwnd(long var1);

    public int browser_init(String var1);

    public int browser_open(int var1, int var2, int var3, int var4, String var5);

    public void browser_set_bounds(int var1, int var2, int var3, int var4);

    public void browser_set_visible(int var1);

    public void browser_navigate(String var1);

    public void browser_go_back();

    public void browser_go_forward();

    public void browser_reload();

    public int browser_is_ready();

    public int browser_is_open();

    public int browser_wants_close();

    public void browser_clear_wants_close();

    public void browser_close();
}

