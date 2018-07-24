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

    private Image buggiesImg, frontWheelImage, rearWheelImg;
    private Group frontWheelCont, rearWheelCont;
    private Body buggiesBody, frontWheelBody, rearWheelBody;
    private Joint frontWheelJoint, rearWheelJoint;
    private World world;
    private final int CLEARENCE = -20;
    private final float RESTITUTION_WHEEL = 0.3f; // упругость, 0 не отскочит, 1 отскочит
    private final float FRICTION_WHEEL = 0.8f; // трение от 0 до 1
    private final float DENSITY_WHEEL = 0.5f; //плотность


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


        // FRONT WHEEL
        frontWheelBody = createWheel(world, 28 / Level.WORLD_SCALE, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_WHEEL);
        frontWheelBody.setTransform(buggiesBody.getPosition().x + 92/ Level.WORLD_SCALE, buggiesBody.getPosition().y - 20/Level.WORLD_SCALE, 0);

        frontWheelCont = new Group();
        frontWheelImage = new Image(Main.atlas.findRegion("buggies_wheel"));

        frontWheelCont.addActor(frontWheelImage);
        frontWheelImage.setX(-frontWheelImage.getWidth()/2);
        frontWheelImage.setY(-frontWheelImage.getHeight()/2);

        getParent().addActor(frontWheelCont);

        UserData data = new UserData();
        data.actor = frontWheelCont;
        frontWheelBody.setUserData(data);

        RevoluteJointDef rDef = new RevoluteJointDef();
        rDef.initialize(buggiesBody, frontWheelBody, new Vector2(frontWheelBody.getPosition()));
        frontWheelJoint = world.createJoint(rDef);

        // REAR WHEEL
        rearWheelBody = createWheel(world, 28 / Level.WORLD_SCALE, RESTITUTION_WHEEL, FRICTION_WHEEL, DENSITY_WHEEL);
        rearWheelBody.setTransform(buggiesBody.getPosition().x - 87 / Level.WORLD_SCALE, buggiesBody.getPosition().y - 25/Level.WORLD_SCALE, 0);
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

        rDef.initialize(buggiesBody, rearWheelBody, new Vector2(rearWheelBody.getPosition()));
        rearWheelJoint = world.createJoint(rDef);

        super.rearWheel = rearWheelBody;
        super.frontWheel = frontWheelBody;
        super.rover = buggiesBody;

        return buggiesBody;
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