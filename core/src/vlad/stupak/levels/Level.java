package vlad.stupak.levels;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.boontaran.games.StageGame;
import com.boontaran.games.tiled.TileLayer;

import vlad.stupak.TankHill;
import vlad.stupak.Setting;
import vlad.stupak.controls.CButton;
import vlad.stupak.controls.JoyStick;
import vlad.stupak.controls.JumpGauge;
import vlad.stupak.player.IBody;
import vlad.stupak.player.Player;
import vlad.stupak.player.UserData;

public class Level extends StageGame{
    private String directory;

    public static final  float WORLD_SCALE = 30;

    public static final int ON_RESTART = 1;
    public static final int ON_QUIT = 2;
    public static final int ON_COMPLETED = 3;
    public static final int ON_FAILED = 4;
    public static final int ON_PAUSED = 5;
    public static final int ON_RESUME = 6;

    private static final int PLAY = 1;
    private static final int LEVEL_FAILED = 2;
    private static final int LEVEL_COMPLETED = 3;
    private static final int PAUSED = 4;

    private int state = 1;

    private JumpGauge jumpGauge;

    private int mapWidth, mapHeight, tilePixelWidth, tilePixelHeight, levelWidth, levelHeight;

    private Player player;
    private Body finish;

    private boolean moveFrontKey, moveBackKey;
    private Image pleaseWait;

    private JoyStick joyStick;
    private CButton jumpBackBtn, jumpForwardBtn;

    private String musicName;
    private boolean musicHasLoaded;

    private String customBackground = null;

    private static final float LAND_RESTITUTION = 0.5f;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Array<Body> bodies = new Array<Body>();

    private boolean hasBeenBuilt = false;

    private TiledMap map;

    public Level(String directory) {
        this.directory = directory;

        pleaseWait = new Image(TankHill.atlas.findRegion("please_wait"));
        addOverlayChild(pleaseWait);
        centerActorXY(pleaseWait);

        delayCall("build_level", 0.2f);
    }

    @Override
    protected void onDelayCall(String code) {
        if (code.equals("build_level")) {
            build();

        } else if (code.equals("resumeLevel2")) {
            resumeLevel2();
        }
    }

    private void setBackGround(String region) {
        clearBackground();
        Image bg = new Image(TankHill.atlas.findRegion(region));
        addBackground(bg, true, false);
    }



    private void build() {
        hasBeenBuilt = true;

        world = new World(new Vector2(0, -Setting.GRAVITY), true);
        world.setContactListener(contactListener);
        debugRenderer = new Box2DDebugRenderer();

        //loadMap();

        if (player == null) {
            throw new Error("player not defined");
        }
        if (finish == null) {
            throw new Error("finish not defined");
        }

        //addRectangleLand();

        int count = 60;
        while (count-- > 0) {
            world.step(1f / 60, 10, 10);
        }

        jumpGauge = new JumpGauge();
        addOverlayChild(jumpGauge);

        joyStick = new JoyStick(mmToPx(10));
        addOverlayChild(joyStick);
        joyStick.setPosition(15, 15);

        jumpBackBtn = new CButton(
                new Image(TankHill.atlas.findRegion("jump1")),
                new Image(TankHill.atlas.findRegion("jump1_down")),
                mmToPx(10)
        );

        addOverlayChild(jumpBackBtn);

        jumpForwardBtn = new CButton(
                new Image(TankHill.atlas.findRegion("jump2")),
                new Image(TankHill.atlas.findRegion("jump2_down")),
                mmToPx(10)
        );

        addOverlayChild(jumpForwardBtn);

        jumpForwardBtn.setPosition(getWidth() - jumpForwardBtn.getWidth() - 15, 15);
        jumpBackBtn.setPosition(jumpForwardBtn.getX() - jumpBackBtn.getWidth() - 15, 15);

        jumpBackBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (state == PLAY) {
                    if (player.isTouchedGround()) {
                        jumpGauge.start();
                        return true;
                    }
                }

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                float jumpValue = jumpGauge.getValue();
                player.jumpBack(jumpValue);
            }
        });

        jumpForwardBtn.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (state == PLAY) {
                    if (player.isTouchedGround()) {
                        jumpGauge.start();
                        return true;
                    }
                }

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                float jumpValue = jumpGauge.getValue();
                player.jumpForward(jumpValue);
            }
        });

        setBackGround("level_bg");

        world.getBodies(bodies);

        updateCamera();

    }

    protected void quitLevel() {
        call(ON_QUIT);
    }

    public void setMusic(String name) {
        musicName = name;
        TankHill.media.addMusic(name);
    }

    public String getMusicName() {
        return musicName;
    }

    @Override
    public void dispose() {
        if (musicName != null && musicHasLoaded) {
            TankHill.media.stopMusic(musicName);
            TankHill.media.removeMusic(musicName);
        }
        if (world != null) world.dispose();
        map.dispose();

        super.dispose();
    }

    private ContactListener contactListener = new ContactListener() {
        @Override
        public void beginContact(Contact contact) {
            Body bodyA = contact.getFixtureA().getBody();
            Body bodyB = contact.getFixtureB().getBody();

            if (bodyA == player.astronaut) {
                playerTouch(bodyB);
                return;
            }
            if (bodyB == player.astronaut) {
                playerTouch(bodyA);
                return;
            }

            if (bodyA == player.rover) {
                UserData data = (UserData) bodyB.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyB == player.rover) {
                UserData data = (UserData) bodyA.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyA == player.frontWheel) {
                UserData data = (UserData) bodyB.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyB == player.frontWheel) {
                UserData data = (UserData) bodyA.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyA == player.rearWheel) {
                UserData data = (UserData) bodyB.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyB == player.rearWheel) {
                UserData data = (UserData) bodyA.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }






        }

        @Override
        public void endContact(Contact contact) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    };

    private void loadMap(String tmxFile) {

        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.generateMipMaps = true;
        params.textureMinFilter = TextureFilter.MipMapLinearNearest;
        params.textureMagFilter = TextureFilter.Linear;

        map = new TmxMapLoader().load(tmxFile, params);

        MapProperties prop = map.getProperties();
        mapWidth = prop.get("width", Integer.class);
        mapHeight = prop.get("height", Integer.class);
        tilePixelWidth = prop.get("tilewidth", Integer.class);
        tilePixelHeight = prop.get("tileheight", Integer.class);
        levelWidth = mapWidth * tilePixelWidth;
        levelHeight = mapHeight * tilePixelHeight;

        for (MapLayer layer : map.getLayers()) {
            String name = layer.getName();

            if (name.equals("land")) {
                createLands(layer.getObjects());
            }
            else if (name.equals("items")) {
                createItems(layer.getObjects());
            }
            else {
                TileLayer tLayer = new TileLayer(camera, map, name,stage.getBatch());
                addChild(tLayer);
            }

        }



    }

    private void createItems(MapObjects objects) {
        Rectangle rect;

        for (MapObject object : objects) {
            rect = ((RectangleMapObject) object).getRectangle();

            if (object.getName().equals("player")) {
                player = new Player(this);
                player.setPosition(rect.x, rect.y);
                addChild(player);
                addBody(player);

                stage.addActor(player);
            } else if (object.getName().equals("finish")) {
                finish = addFinish(rect);
            }
        }
    }

    private Body addFinish(Rectangle rectangle) {
        rectangle.x /= WORLD_SCALE;
        rectangle.y /= WORLD_SCALE;
        rectangle.width /= WORLD_SCALE;
        rectangle.height /= WORLD_SCALE;

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.linearDamping = 0;

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rectangle.width/2, rectangle.height/2);

        fdef.shape = shape;
        fdef.restitution = LAND_RESTITUTION;
        fdef.density = 1;
        fdef.isSensor = true;

        Body body = world.createBody(def);
        body.createFixture(fdef);
        body.setTransform(rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2, 0);
        shape.dispose();

        return body;
    }

    private void playMusic() {
        if (musicName != null && musicHasLoaded) {
            TankHill.media.playMusic(musicName, true);
        }
    }
    private void stopMusic() {
        if (musicName != null && musicHasLoaded) {
            TankHill.media.stopMusic(musicName);
        }
    }

    private void hideButtons() {
        joyStick.setVisible(false);
        jumpBackBtn.setVisible(false);
        jumpForwardBtn.setVisible(false);
    }
    private void showButtons() {
        joyStick.setVisible(true);
        jumpBackBtn.setVisible(true);
        jumpForwardBtn.setVisible(true);
    }


    private void addBody(IBody item) {
        Body body = item.createBody(world);
        UserData data = new UserData();
        data.actor = (Actor) item;
        body.setUserData(data);
    }

    private void createLands(MapObjects objects) {
        Polygon polygon;
        Rectangle rectangle;

        Array<Polygon> childs;

        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                polygon = ((PolygonMapObject) object).getPolygon();
                scaleToWorld(polygon);
                childs = getTriangles(polygon);
                addPolygonLand(childs);
            } else if (object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                addRectangleLand(rectangle);
            }
        }
    }

    private void addRectangleLand(Rectangle rectangle) {
        rectangle.x /= WORLD_SCALE;
        rectangle.y /= WORLD_SCALE;
        rectangle.width /= WORLD_SCALE;
        rectangle.height /= WORLD_SCALE;

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.linearDamping = 0;

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rectangle.width/2, rectangle.height/2);

        fdef.shape = shape;
        fdef.restitution = LAND_RESTITUTION;
        fdef.density = 1;

        Body body = world.createBody(def);
        body.createFixture(fdef);
        body.setTransform(rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2, 0);
        body.setUserData(new UserData(null, "land"));
        shape.dispose();
    }

    private void addPolygonLand(Array<Polygon> childs) {
    }

    private Array<Polygon> getTriangles(Polygon polygon) {

        return null;
    }

    private void scaleToWorld(Polygon polygon) {
    }


    private void resumeLevel2() {
    }

    private void updateCamera() {
    }

    public void addChild(Actor actor) {
        this.stage.addActor(actor);
    }

    public void addChild(Actor actor, float x, float y) {
        this.addChild(actor);
        actor.setX(x);
        actor.setY(y);
    }

    protected void playerTouch(Body body) {
        UserData data = (UserData) body.getUserData();

        if (data != null) {
            if (data.name.equals("land") && !player.isHasDestoyed()) {
                if (player.getRotation() < -90 || player.getRotation() > 90) {
                    player.destroy();
                    TankHill.media.playSound("crash.ogg");
                    levelFailed();
                } else {
                    player.touchGround();
                }
            }
        } else {
            if (body == finish) {
                levelCompleted();
            }
        }
    }

    private void levelCompleted() {
    }

    private void levelFailed() {
    }

}