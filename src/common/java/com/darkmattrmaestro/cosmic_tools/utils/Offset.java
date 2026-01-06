package com.darkmattrmaestro.cosmic_tools.utils;

import java.util.Arrays;

public enum Offset {
    nnn(-1,-1,-1),
    nno(-1,-1,0),
    nnp(-1,-1,1),
    non(-1,0,-1),
    noo(-1,0,0),
    nop(-1,0,1),
    npn(-1,1,-1),
    npo(-1,1,0),
    npp(-1,1,1),

    onn(0,-1,-1),
    ono(0,-1,0),
    onp(0,-1,1),
    oon(0,0,-1),
    //    ooo(0,0,0),
    oop(0,0,1),
    opn(0,1,-1),
    opo(0,1,0),
    opp(0,1,1),

    pnn(1,-1,-1),
    pno(1,-1,0),
    pnp(1,-1,1),
    pon(1,0,-1),
    poo(1,0,0),
    pop(1,0,1),
    ppn(1,1,-1),
    ppo(1,1,0),
    ppp(1,1,1);

    public static final Offset[] ALL_DIRECTIONS = values();
    public static final Offset[] AXIAL = Arrays.stream(values()).filter((Offset offset) -> offset.xOff == 0 || offset.yOff == 0 || offset.zOff == 0).toArray(Offset[]::new);
    public static Offset[] CoplanarWith(Offset[] arr, Vector3Int vec) {
        return Arrays.stream(arr).filter((Offset offset) -> (offset.xOff == 0 && vec.x != 0) || (offset.yOff == 0 && vec.y != 0) || (offset.zOff == 0 && vec.z != 0)).toArray(Offset[]::new);
    }

    private int xOff;
    private int yOff;
    private int zOff;

    Offset(int xOff, int yOff, int zOff) {
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;
    }

    public int getXOffset() {
        return this.xOff;
    }
    public int getYOffset() {
        return this.yOff;
    }
    public int getZOffset() {
        return this.zOff;
    }
}
