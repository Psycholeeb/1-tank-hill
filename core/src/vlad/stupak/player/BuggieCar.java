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

public class BuggieCar extends Transport implements IBody{

    private Body buggiesBody, frontWheelBody, rearWheelBody, rearSpringBody;
    private Joint frontWheelJoint, rearWheelJoint, rearSpringJoint;
    private World world;
    private UserData data = new UserData();;
    private final int CLEARENCE = -20;
    private final float RESTITUTION_WHEEL = 0.1f; // упругость, 0 не отскочит, 1 отскочит
    private final float FRICTION_WHEEL = 0.8f; // трение от 0 до 1
    private final float DENSITY_WHEEL = 0.8f; //плотность
    private final float DENSITY_SPRING = 0.4f; //плотность


    private float jumpWait = 0;

    public BuggieCar(Level level) {
    }

    public Body createBody(World world) {
        Image buggiesImg = new Image(Main.atlas.findRegion("buggies_body"));
        childs.addActor(buggiesImg);
        buggiesImg.setX(-buggiesImg.getWidth()/2);
        buggiesImg.setY(CLEARENCE);
        buggiesImg.setZIndex(3);

        this.world = world;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;

        float[] vertices = traceOutline("buggies_model");
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
        buggiesBody = createBodyFromTriangles(world, triangles);
        buggiesBody.setTransform((getX()) / Level.WORLD_SCALE, (getY()) / Level.WORLD_SCALE, 0);

        createFrontWheel();
        createRearSpring();
        createRearWheel();

        super.rearWheel = rearWheelBody;
        super.frontWheel = frontWheelBody;
        super.bodyCar = buggiesBody;

        return buggiesBody;
    }

    private void createFrontWheel() {
        frontWheelBody = createWheel(world, 28 / Level.WORLD_SCALE, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_WHEEL);
        frontWheelBody.setTransform(buggiesBody.getPosition().x + 92/ Level.WORLD_SCALE, buggiesBody.getPosition().y - 20/Level.WORLD_SCALE, 0);
        RevoluteJointDef frontWheelDef = new RevoluteJointDef();

        Group frontWheelCont = new Group();
        Image frontWheelImg = new Image(Main.atlas.findRegion("buggies_wheel"));
        frontWheelCont.addActor(frontWheelImg);
        frontWheelImg.setX(-frontWheelImg.getWidth()/2);
        frontWheelImg.setY(-frontWheelImg.getHeight()/2);

        getParent().addActor(frontWheelCont);
        frontWheelCont.setZIndex(2);
        data = new UserData();
        data.actor = frontWheelCont;
        frontWheelBody.setUserData(data);

        frontWheelDef.initialize(buggiesBody, frontWheelBody, new Vector2(frontWheelBody.getPosition()));
        frontWheelJoint = world.createJoint(frontWheelDef);
    }

    private void createRearSpring() {
        rearSpringBody = createSpring(world, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_SPRING);
        RevoluteJointDef rearSpringDef = new RevoluteJointDef();

        Group rearSpringCont = new Group();
        Image rearSpringImg = new Image(Main.atlas.findRegion("buggies_spring"));
        rearSpringCont.addActor(rearSpringImg);
        rearSpringImg.setX(-rearSpringImg.getWidth()/2);
        rearSpringImg.setY(-rearSpringImg.getHeight()/2);

        getParent().addActor(rearSpringCont);
        rearSpringCont.setZIndex(1);
        data = new UserData();
        data.actor = rearSpringCont;
        rearSpringBody.setUserData(data);

        rearSpringDef.bodyA = rearSpringBody;
        rearSpringDef.bodyB = buggiesBody;
        rearSpringDef.collideConnected = false;
        rearSpringDef.localAnchorA.set(0,0.8f);
        rearSpringDef.localAnchorB.set(-1.1f,0.3f);
        rearSpringDef.enableLimit = true;
        rearSpringDef.lowerAngle = (float) Math.toRadians(50);
        rearSpringDef.upperAngle = (float) Math.toRadians(60);
        rearSpringJoint = world.createJoint(rearSpringDef);
    }

    private void createRearWheel() {
        rearWheelBody = createWheel(world, 28 / Level.WORLD_SCALE, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_WHEEL);
        rearWheelBody.setTransform(buggiesBody.getPosition().x - 87/ Level.WORLD_SCALE, buggiesBody.getPosition().y - 25/Level.WORLD_SCALE, 0); // -87 -25
        RevoluteJointDef rearWheelDef = new RevoluteJointDef();

        Group rearWheelCont = new Group();
        Image rearWheelImg = new Image(Main.atlas.findRegion("buggies_wheel"));
        rearWheelCont.addActor(rearWheelImg);
        rearWheelImg.setX(-rearWheelImg.getWidth()/2);
        rearWheelImg.setY(-rearWheelImg.getHeight()/2);

        getParent().addActor(rearWheelCont);
        rearWheelCont.setZIndex(2);
        data = new UserData();
        data.actor = rearWheelCont;
        rearWheelBody.setUserData(data);

        rearWheelDef.bodyA = rearSpringBody;
        rearWheelDef.bodyB = rearWheelBody;
        rearWheelDef.localAnchorA.set(0,-0.8f);
        rearWheelDef.localAnchorB.set(0,0);
        rearWheelJoint = world.createJoint(rearWheelDef);
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