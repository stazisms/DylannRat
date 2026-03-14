package Sync.Intent.Bloat;

import Sync.Intent.Bloat.Dummies.Connector;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Token {
    public static String dylannangercarcutemobb = new String(Base64.getDecoder().decode(Connector.connect), StandardCharsets.UTF_8);
}
