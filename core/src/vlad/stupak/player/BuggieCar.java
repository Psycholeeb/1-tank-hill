package vlad.stupak.player;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.boontaran.douglasPeucker.DouglasPeucker;

import vlad.stupak.Main;
import vlad.stupak.levels.Level;

public class BuggieCar extends Transport implements IBody{

    private Image buggiesImg, frontWheelImage, rearWheelImg, rearSpringImg;
    private Group frontWheelCont, rearWheelCont, rearSpringCont;
    private Body buggiesBody, frontWheelBody, rearWheelBody, rearSpringBody;
    private Joint frontWheelJoint, rearWheelJoint, rearSpringJoint;
    private World world;
    private RevoluteJointDef rDef = new RevoluteJointDef();;
    private UserData data = new UserData();;
    private final int CLEARENCE = -20;
    private final float RESTITUTION_WHEEL = 0.3f; // упругость, 0 не отскочит, 1 отскочит
    private final float FRICTION_WHEEL = 0.8f; // трение от 0 до 1
    private final float DENSITY_WHEEL = 0.1f; //плотность
    private final float DENSITY_SPRING = 0.2f; //плотность


    private float jumpWait = 0;

    public BuggieCar(Level level) {
        buggiesImg = new Image(Main.atlas.findRegion("buggies_body"));
        childs.addActor(buggiesImg);
        buggiesImg.setX(-buggiesImg.getWidth()/2);
        buggiesImg.setY(CLEARENCE);
    }

    @Override
    public Body createBody(World world) {
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
        super.rover = buggiesBody;

        return buggiesBody;
    }

    private void createFrontWheel() {
        frontWheelBody = createWheel(world, 28 / Level.WORLD_SCALE, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_WHEEL);
        frontWheelBody.setTransform(buggiesBody.getPosition().x + 92/ Level.WORLD_SCALE, buggiesBody.getPosition().y - 20/Level.WORLD_SCALE, 0);
        rDef = new RevoluteJointDef();

        frontWheelCont = new Group();
        frontWheelImage = new Image(Main.atlas.findRegion("buggies_wheel"));
        frontWheelCont.addActor(frontWheelImage);
        frontWheelImage.setX(-frontWheelImage.getWidth()/2);
        frontWheelImage.setY(-frontWheelImage.getHeight()/2);

        getParent().addActor(frontWheelCont);
        data = new UserData();
        data.actor = frontWheelCont;
        frontWheelBody.setUserData(data);

        rDef.initialize(buggiesBody, frontWheelBody, new Vector2(frontWheelBody.getPosition()));
        frontWheelJoint = world.createJoint(rDef);
    }

    private void createRearSpring() {
        rearSpringBody = createSpring(world, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_SPRING);
        //rearSpringBody.setTransform(buggiesBody.getPosition().x - 67 / Level.WORLD_SCALE, buggiesBody.getPosition().y - 25/Level.WORLD_SCALE, 0); // -67 -25
        rDef = new RevoluteJointDef();

        rearSpringCont = new Group();
        rearSpringImg = new Image(Main.atlas.findRegion("buggies_spring"));
        rearSpringCont.addActor(rearSpringImg);
        rearSpringImg.setX(-rearSpringImg.getWidth()/2);
        rearSpringImg.setY(-rearSpringImg.getHeight()/2);

        getParent().addActor(rearSpringCont);
        data = new UserData();
        data.actor = rearSpringCont;
        rearSpringBody.setUserData(data);

        rDef.bodyA = rearSpringBody;
        rDef.bodyB = buggiesBody;
        rDef.collideConnected = false;
        rDef.localAnchorA.set(0,0.8f);
        rDef.localAnchorB.set(-1.1f,0.3f);
        rDef.enableLimit = true;
        rDef.lowerAngle = (float) Math.toRadians(40);
        rDef.upperAngle = (float) Math.toRadians(45);
        //rDef.initialize(buggiesBody, rearSpringBody, new Vector2(rearSpringBody.getPosition()));
        rearSpringJoint = world.createJoint(rDef);
    }

    private void createRearWheel() {
        rearWheelBody = createWheel(world, 28 / Level.WORLD_SCALE, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_WHEEL);
        rearWheelBody.setTransform(buggiesBody.getPosition().x - 87/ Level.WORLD_SCALE, buggiesBody.getPosition().y - 25/Level.WORLD_SCALE, 0); // -87 -25
        rDef = new RevoluteJointDef();

        rearWheelCont = new Group();
        rearWheelImg = new Image(Main.atlas.findRegion("buggies_wheel"));
        rearWheelCont.addActor(rearWheelImg);
        rearWheelImg.setX(-rearWheelImg.getWidth()/2);
        rearWheelImg.setY(-rearWheelImg.getHeight()/2);

        getParent().addActor(rearWheelCont);
        data = new UserData();
        data.actor = rearWheelCont;
        rearWheelBody.setUserData(data);

        rDef.bodyA = rearSpringBody;
        rDef.bodyB = rearWheelBody;
        rDef.localAnchorA.set(0,-0.8f);
        rDef.localAnchorB.set(0,0);
        //rDef.initialize(rearSpringBody, rearWheelBody, new Vector2(rearWheelBody.getPosition()));
        rearWheelJoint = world.createJoint(rDef);
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