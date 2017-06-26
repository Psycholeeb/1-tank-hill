package vlad.stupak.player;


import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.boontaran.douglasPeucker.DouglasPeucker;
import com.boontaran.games.ActorClip;
import com.boontaran.marchingSquare.MarchingSquare;

import java.util.ArrayList;

import vlad.stupak.Setting;
import vlad.stupak.TankHill;
import vlad.stupak.levels.Level;

public class Btr extends ActorClip implements IBody{

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


    public Btr(Level level) {
        this.level = level;

        roverImg = new Image(TankHill.atlas.findRegion("rover"));
        childs.addActor(roverImg);
        roverImg.setX(-roverImg.getWidth()/2);

        /*astronautImg = new Image(TankHill.atlas.findRegion("astronaut"));
        childs.addActor(astronautImg);

        astronautImg.setX(-35);
        astronautImg.setY(20);*/

        astronautFallCont = new Group();

        astronautFallImg = new Image(TankHill.atlas.findRegion("astronaut_fall"));
        astronautFallCont.addActor(astronautFallImg);

        astronautFallImg.setX(-astronautFallImg.getWidth()/2);
        astronautFallImg.setY(-astronautFallImg.getHeight()/2);

    }

    public void touchGround() {
        isTouchGround = true;
    }

    public boolean isTouchedGround() {
        if (jumpWait > 0) return false;
        return isTouchGround;
    }

    @Override
    public Body createBody(World world) {
        this.world = world;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;

        float[] vertices = traceOutline("rover_model");
        Vector2 centroid = Level.calculateCentroid(vertices);

        int i = 0;
        while (i < vertices.length) {
            vertices[i] -= centroid.x;
            vertices[i + 1] -= centroid.y;
            i += 2;
        }

        vertices = DouglasPeucker.simplify(vertices, 4);
        Level.scaleToWorld(vertices);
        Array<Polygon> triangles = Level.getTriangles(new Polygon(vertices));
        rover = createBodyFromTriangles(world, triangles);
        rover.setTransform((getX()) / Level.WORLD_SCALE, (getY()) / Level.WORLD_SCALE, 0);


        // FRONT WHEEL
        frontWheel = createWheel(world, 18 / Level.WORLD_SCALE);
        frontWheel.setTransform(rover.getPosition().x + 75 / Level.WORLD_SCALE, rover.getPosition().y + 1/Level.WORLD_SCALE, 0);

        frontWheelCont = new Group();
        frontWheelImage = new Image(TankHill.atlas.findRegion("front_wheel"));

        frontWheelCont.addActor(frontWheelImage);
        frontWheelImage.setX(-frontWheelImage.getWidth()/2);
        frontWheelImage.setY(-frontWheelImage.getHeight()/2);

        getParent().addActor(frontWheelCont);

        UserData data = new UserData();
        data.actor = frontWheelCont;
        frontWheel.setUserData(data);

        RevoluteJointDef rDef = new RevoluteJointDef();
        rDef.initialize(rover, frontWheel, new Vector2(frontWheel.getPosition()));
        frontWheelJoint = world.createJoint(rDef);


        //FRONT WHEEL 2
        frontWheel2 = createWheel(world, 18 / Level.WORLD_SCALE);
        frontWheel2.setTransform(rover.getPosition().x + 27 / Level.WORLD_SCALE, rover.getPosition().y + 1/Level.WORLD_SCALE, 0);

        frontWheelCont2 = new Group();
        frontWheelImage2 = new Image(TankHill.atlas.findRegion("front_wheel"));

        frontWheelCont2.addActor(frontWheelImage2);
        frontWheelImage2.setX(-frontWheelImage2.getWidth()/2);
        frontWheelImage2.setY(-frontWheelImage2.getHeight()/2);

        getParent().addActor(frontWheelCont2);

        data = new UserData();
        data.actor = frontWheelCont2;
        frontWheel2.setUserData(data);

        rDef.initialize(rover, frontWheel2, new Vector2(frontWheel2.getPosition()));
        frontWheelJoint2 = world.createJoint(rDef);


        // REAR WHEEL
        rearWheel = createWheel(world, 18 / Level.WORLD_SCALE);
        rearWheel.setTransform(rover.getPosition().x - 83 / Level.WORLD_SCALE, rover.getPosition().y + 1/Level.WORLD_SCALE, 0);
        rDef = new RevoluteJointDef();


        rearWheelCont = new Group();
        rearWheelImg = new Image(TankHill.atlas.findRegion("rear_wheel"));
        rearWheelCont.addActor(rearWheelImg);
        rearWheelImg.setX(-rearWheelImg.getWidth()/2);
        rearWheelImg.setY(-rearWheelImg.getHeight()/2);

        getParent().addActor(rearWheelCont);
        data = new UserData();
        data.actor = rearWheelCont;
        rearWheel.setUserData(data);

        rDef.initialize(rover, rearWheel, new Vector2(rearWheel.getPosition()));
        rearWheelJoint = world.createJoint(rDef);


        // REAR WHEEL 2
        rearWheel2 = createWheel(world, 18 / Level.WORLD_SCALE);
        rearWheel2.setTransform(rover.getPosition().x - 35 / Level.WORLD_SCALE, rover.getPosition().y + 1/Level.WORLD_SCALE, 0);
        rDef = new RevoluteJointDef();


        rearWheelCont2 = new Group();
        rearWheelImg2 = new Image(TankHill.atlas.findRegion("rear_wheel"));
        rearWheelCont2.addActor(rearWheelImg2);
        rearWheelImg2.setX(-rearWheelImg2.getWidth()/2);
        rearWheelImg2.setY(-rearWheelImg2.getHeight()/2);

        getParent().addActor(rearWheelCont2);
        data = new UserData();
        data.actor = rearWheelCont2;
        rearWheel2.setUserData(data);

        rDef.initialize(rover, rearWheel2, new Vector2(rearWheel2.getPosition()));
        rearWheelJoint2 = world.createJoint(rDef);

        /*vertices = traceOutline("astronaut_model");
        centroid = Level.calculateCentroid(vertices);

        i = 0;
        while (i < vertices.length) {
            vertices[i] -= centroid.x;
            vertices[i + 1] -= centroid.y;

            i+= 2;
        }
        vertices = DouglasPeucker.simplify(vertices, 6);
        Level.scaleToWorld(vertices);
        triangles = Level.getTriangles(new Polygon(vertices));
        astronaut = createBodyFromTriangles(world, triangles);
        astronaut.setTransform(rover.getPosition().x - 0 / Level.WORLD_SCALE, rover.getPosition().y + 30/ Level.WORLD_SCALE, 0);

        WeldJointDef actronautDef = new WeldJointDef();
        actronautDef.initialize(rover, astronaut, new Vector2(astronaut.getPosition()));
        astroJoint = world.createJoint(actronautDef);*/

        return rover;
    }

    private Body createWheel(World world, float rad) {

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;
        def.angularDamping = 1f;

        Body body = world.createBody(def);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(rad);

        fDef.shape = shape;
        fDef.restitution = 0.1f;  //0.5f эластичность
        fDef.friction = 1;  //0.4f трение
        fDef.density = 1;  //1 плотность кг/m^2

        body.createFixture(fDef);
        shape.dispose();


        return body;
    }

    private float[] traceOutline(String regionName) {

        Texture bodyOutLine = TankHill.atlas.findRegion(regionName).getTexture();
        TextureAtlas.AtlasRegion reg = TankHill.atlas.findRegion(regionName);
        int w = reg.getRegionWidth();
        int h = reg.getRegionHeight();
        int x = reg.getRegionX();
        int y = reg.getRegionY();

        bodyOutLine.getTextureData().prepare();
        Pixmap allPixmap = bodyOutLine.getTextureData().consumePixmap();

        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(allPixmap,0,0,x,y,w,h);

        allPixmap.dispose();

        int pixel;

        w = pixmap.getWidth();
        h = pixmap.getHeight();

        int [][] map;
        map = new int[w][h];
        for (x=0; x < w; x++) {
            for (y = 0; y < h; y++) {
                pixel = pixmap.getPixel(x, y);
                if ((pixel & 0x000000ff) == 0) {
                    map[x][y] = 0;
                } else {
                    map[x][y] = 1;
                }
            }
        }

        pixmap.dispose();

        MarchingSquare ms = new MarchingSquare(map);
        ms.invertY();
        ArrayList<float[]> traces = ms.traceMap();

        float[] polyVertices = traces.get(0);
        return polyVertices;
    }

    private Body createBodyFromTriangles(World world, Array<Polygon> triangles) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;
        Body body = world.createBody(def);

        for (Polygon triangle : triangles) {
            FixtureDef fDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            shape.set(triangle.getTransformedVertices());

            fDef.shape = shape;
            fDef.restitution = 0; //0.3f эластичность
            fDef.density = 1.2f;  //1 плотность кг/m^2

            body.createFixture(fDef);
            shape.dispose();
        }
        return body;
    }

    public void onKey(boolean moveFrontKey, boolean moveBackKey) {
        float torque = Setting.WHEEL_TORQUE;
        float maxAV = 18;

        if (moveFrontKey) {
            if (-rearWheel.getAngularVelocity() < maxAV) {
                rearWheel.applyTorque(-torque, true);
            }
            if (-frontWheel.getAngularVelocity() < maxAV) {
                frontWheel.applyTorque(-torque, true);
            }
            if (-rearWheel2.getAngularVelocity() < maxAV) {
                rearWheel2.applyTorque(-torque, true);
            }
            if (-frontWheel2.getAngularVelocity() < maxAV) {
                frontWheel2.applyTorque(-torque, true);
            }

        }
        if (moveBackKey) {
            if (rearWheel.getAngularVelocity() < maxAV) {
                rearWheel.applyTorque(torque, true);
            }
            if (frontWheel.getAngularVelocity() < maxAV) {
                frontWheel.applyTorque(torque, true);
            }
            if (rearWheel2.getAngularVelocity() < maxAV) {
                rearWheel2.applyTorque(torque, true);
            }
            if (frontWheel2.getAngularVelocity() < maxAV) {
                frontWheel2.applyTorque(torque, true);
            }
        }
    }

    public void jumpBack(float value) {
        if (value < 0.2f) value = 0.2f;

        rover.applyLinearImpulse(0, jumpImpulse * value,
                rover.getWorldCenter().x + 5 / Level.WORLD_SCALE,
                rover.getWorldCenter().y, true);
        isTouchGround = false;
        jumpWait = 0.3f;
    }

    public void jumpForward(float value){
        if (value < 0.2f) value = 0.2f;

        rover.applyLinearImpulse(0, jumpImpulse * value,
                rover.getWorldCenter().x - 4 / Level.WORLD_SCALE,
                rover.getWorldCenter().y, true);
        isTouchGround = false;
        jumpWait = 0.3f;
    }

    @Override
    public void act(float delta) {
        if (jumpWait > 0) {
            jumpWait -= delta;
        }

        if (destroyOnNextUpdate) {
            destroyOnNextUpdate = false;
            world.destroyJoint(frontWheelJoint);
            world.destroyJoint(rearWheelJoint);
            //world.destroyJoint(astroJoint);
            //world.destroyBody(astronaut);
            //astronautImg.remove();

            //astronautFall();
        }

        super.act(delta);
    }

    private void astronautFall() {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;
        def.angularDamping = 0;

        def.position.x = astronaut.getPosition().x;
        def.position.y = astronaut.getPosition().y;
        def.angle = getRotation() * 3.1416f / 180;
        def.angularVelocity = astronaut.getAngularVelocity();

        Body body = world.createBody(def);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(10 / Level.WORLD_SCALE);

        fDef.shape = shape;
        fDef.restitution = 0.5f;
        fDef.friction = 0.4f;
        fDef.density = 1;
        fDef.isSensor = true;

        body.createFixture(fDef);

        body.setLinearVelocity(astronaut.getLinearVelocity());

        shape.dispose();

        level.addChild(astronautFallCont);
        astronautFallCont.setPosition(getX(), getY());

        UserData data = new UserData();
        data.actor = astronautFallCont;
        body.setUserData(data);
    }

    public void destroy() {
        if (hasDestoyed) return;
        hasDestoyed = true;

        destroyOnNextUpdate = true;
    }

    public boolean isHasDestoyed() {
        return hasDestoyed;
    }
}