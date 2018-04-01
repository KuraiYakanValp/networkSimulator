package com.graduationproject.logic;

import lombok.Getter;

public class Mac {
    @Getter
    String[] mac;

    public Mac(String mac) {
        setMac(mac);
    }

    public void setMac(String mac) {
        mac = mac.toLowerCase();
        this.mac = mac.split(":");
        if (this.mac.length == 1)
            this.mac = mac.split("(?<=\\G.{2})");
        if (this.mac.length != 6)
            throw new IllegalArgumentException("Unsupported mac");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Mac) {
            String[] objMac = ((Mac) obj).getMac();
            if (mac.length == objMac.length) {
                boolean same = true;
                for (int i = 0; i < mac.length; i++) {
                    if (!mac[i].equals(objMac[i])) {
                        same = false;
                        break;
                    }
                }
                if (same)
                    return true;
            }
            return false;
        } else
            return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.join(":", this.mac);
    }
}
