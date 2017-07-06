package vlad.stupak.player;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.boontaran.douglasPeucker.DouglasPeucker;

import vlad.stupak.Main;
import vlad.stupak.levels.Level;

public class Btr extends Transport implements IBody{

    private Image roverImg, frontWheelImage, frontWheelImage2, rearWheelImg, rearWheelImg2;

    private Group frontWheelCont, frontWheelCont2, rearWheelCont, rearWheelCont2;

    private Body rover, frontWheel, frontWheel2, rearWheel, rearWheel2;

    private Joint frontWheelJoint, rearWheelJoint;

    private World world;

    private float jumpWait = 0;

    private Level level;

    public Btr(Level level) {
        this.level = level;

        roverImg = new Image(Main.atlas.findRegion("btr_body"));
        childs.addActor(roverImg);
        roverImg.setX(-roverImg.getWidth()/2);
    }

    @Override
    public Body createBody(World world) {
        this.world = world;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;

        float[] vertices = traceOutline("btr_model");
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
        frontWheelImage = new Image(Main.atlas.findRegion("btr_wheel"));

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
        frontWheelImage2 = new Image(Main.atlas.findRegion("btr_wheel"));

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
        rearWheelImg = new Image(Main.atlas.findRegion("btr_wheel"));
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
        rearWheelImg2 = new Image(Main.atlas.findRegion("btr_wheel"));
        rearWheelCont2.addActor(rearWheelImg2);
        rearWheelImg2.setX(-rearWheelImg2.getWidth()/2);
        rearWheelImg2.setY(-rearWheelImg2.getHeight()/2);

        getParent().addActor(rearWheelCont2);
        data = new UserData();
        data.actor = rearWheelCont2;
        rearWheel2.setUserData(data);

        rDef.initialize(rover, rearWheel2, new Vector2(rearWheel2.getPosition()));
        rearWheelJoint2 = world.createJoint(rDef);

        super.rearWheel = rearWheel;
        super.rearWheel2 = rearWheel2;
        super.frontWheel = frontWheel;
        super.frontWheel2 = frontWheel2;
        super.rover = rover;

        return rover;
    }

    @Override
    public void act(float delta) {
        if (jumpWait > 0) {
            jumpWait -= delta;
        }

        if (super.destroyOnNextUpdate) {
            destroyOnNextUpdate = false;
            world.destroyJoint(frontWheelJoint);
            world.destroyJoint(rearWheelJoint);
        }

        super.act(delta);
    }
}