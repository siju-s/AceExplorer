package com.siju.acexplorer.filesystem.theme;


public enum Themes {
    LIGHT(0),
    DARK(1);

    private final int value;

    Themes(int value) {
        this.value = value;
    }

    public int getValue() {

        return value;
    }

    public static Themes getTheme(int position) {
        switch (position) {
            case 0:
                return LIGHT;
            case 1:
                return DARK;
        }
        return DARK;
    }
}
