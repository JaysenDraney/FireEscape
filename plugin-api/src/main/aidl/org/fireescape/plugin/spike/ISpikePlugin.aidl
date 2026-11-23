// M0.2 spike interface. Not the real plugin API. It exists to test the transport:
// binding across APKs, versioning, a plugin crash and a plugin that hangs.
package org.fireescape.plugin.spike;

interface ISpikePlugin {
    /** Plugin API version. getInterfaceVersion() is reserved by the AIDL compiler. New methods are only added at the end. */
    int getPluginApiVersion();

    /** A normal call that returns data. */
    String describe();

    /** Kills the plugin's own process, to test that the launcher survives. */
    void crash();

    /** Blocks for the given time before answering, to test client-side timeouts. */
    String slowEcho(String text, long delayMillis);
}