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

public class Jeep extends Transport implements IBody{

    private Image roverImg, astronautImg, astronautFallImg, frontWheelImage, frontWheelImage2, rearWheelImg, rearWheelImg2;

    private Group frontWheelCont, frontWheelCont2, rearWheelCont, rearWheelCont2, astronautFallCont;

    private Body rover, frontWheel, frontWheel2, rearWheel, rearWheel2, astronaut;

    private Joint frontWheelJoint, frontWheelJoint2, rearWheelJoint, rearWheelJoint2, astroJoint;

    private World world;

    private float jumpWait = 0;

    private Level level;


    public Jeep(Level level) {
        this.level = level;

        roverImg = new Image(Main.atlas.findRegion("jeep_body"));
        childs.addActor(roverImg);
        roverImg.setX(-roverImg.getWidth()/2);
    }

    @Override
    public Body createBody(World world) {
        this.world = world;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;

        float[] vertices = traceOutline("jeep_model");
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
        rover.setTransform((getX()) / Level.WORLD_SCALE+20, (getY()) / Level.WORLD_SCALE, 0);


        // FRONT WHEEL
        frontWheel = createWheel(world, 18 / Level.WORLD_SCALE);
        frontWheel.setTransform(rover.getPosition().x + 75 / Level.WORLD_SCALE, rover.getPosition().y + 1/Level.WORLD_SCALE, 0);

        frontWheelCont = new Group();
        frontWheelImage = new Image(Main.atlas.findRegion("jeep_wheel"));

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

        // REAR WHEEL
        rearWheel = createWheel(world, 18 / Level.WORLD_SCALE);
        rearWheel.setTransform(rover.getPosition().x - 83 / Level.WORLD_SCALE, rover.getPosition().y + 1/Level.WORLD_SCALE, 0);
        rDef = new RevoluteJointDef();


        rearWheelCont = new Group();
        rearWheelImg = new Image(Main.atlas.findRegion("jeep_wheel"));
        rearWheelCont.addActor(rearWheelImg);
        rearWheelImg.setX(-rearWheelImg.getWidth()/2);
        rearWheelImg.setY(-rearWheelImg.getHeight()/2);

        getParent().addActor(rearWheelCont);
        data = new UserData();
        data.actor = rearWheelCont;
        rearWheel.setUserData(data);

        rDef.initialize(rover, rearWheel, new Vector2(rearWheel.getPosition()));
        rearWheelJoint = world.createJoint(rDef);

        super.rearWheel = rearWheel;
        super.frontWheel = frontWheel;
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