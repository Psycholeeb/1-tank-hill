package vlad.stupak;

public class Setting {
    public static final boolean DEBUG_GAME = false;
    public static final boolean DEBUG_WORLD = true;


    public static final float GRAVITY = 40;  //8 гравитация
    public static final float JUMP_IMPULSE = 300;
    public static final float WHEEL_TORQUE = 150;  //50 крутящий момент
    public static final float SPEED = 80;

    //SETTINGS OF WHELLS
    public static final float RESTITUTION = 0;  //0.5f эластичность
    public static final float FRICTION = 3000;  //0.4f трение
    public static final float DENSITY = 1;  //1 плотность кг/m^2
}
