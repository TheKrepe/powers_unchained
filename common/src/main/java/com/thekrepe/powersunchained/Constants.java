package com.thekrepe.powersunchained;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {

    public static final String MOD_ID = "powers_unchained";
    public static final String MOD_NAME = "Powers Unchained";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Constants() {
    }

    /** Shortcut for an identifier in the mod's namespace, e.g. {@code id("flight")} -> {@code powers_unchained:flight}. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
