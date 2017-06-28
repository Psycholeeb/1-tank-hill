package vlad.stupak.player;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import vlad.stupak.Setting;
import vlad.stupak.TankHill;
import vlad.stupak.levels.Level;

public class Jeep extends Transport{

    private Image roverImg, astronautImg, astronautFallImg, frontWheelImage, frontWheelImage2, rearWheelImg, rearWheelImg2;

    private Group frontWheelCont, frontWheelCont2, rearWheelCont, rearWheelCont2, astronautFallCont;

    public Body rover, frontWheel, frontWheel2, rearWheel, rearWheel2, astronaut;

    private Joint frontWheelJoint, frontWheelJoint2, rearWheelJoint, rearWheelJoint2, astroJoint;

    private World world;

    private boolean hasDestoyed = false;
    private boolean destroyOnNextUpdate = false;

    private boolean isTouchGround = true;

    private float jumpImpulse = Setting.JUMP_IMPULSE;
    private float jumpWait = 0;

    private Level level;


    public Jeep(Level level) {
        this.level = level;

        roverImg = new Image(TankHill.atlas.findRegion("rover"));
        childs.addActor(roverImg);
        roverImg.setX(-roverImg.getWidth()/2);
    }
}