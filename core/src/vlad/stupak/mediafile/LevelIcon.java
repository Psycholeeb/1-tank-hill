package vlad.stupak.mediafile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import org.omg.CORBA.Bounds;

import vlad.stupak.Main;

public class LevelIcon extends Group{
    private int id;
    private Label label;
    private Image lockImg, bg, bgDown, hiliteImg;
    private boolean isHilited = false;
    private boolean alphaUp = false;

    public LevelIcon(int id) {
        this.id = id;

        hiliteImg = new Image(Main.atlas.findRegion("level_icon_hilite"));
        hiliteImg.setOrigin(hiliteImg.getWidth()/2, hiliteImg.getHeight()/2);
        addActor(hiliteImg);
        hiliteImg.setVisible(false);

        bg = new Image(Main.atlas.findRegion("level_icon_bg"));
        addActor(bg);
        setSize(bg.getWidth(), bg.getHeight());

        hiliteImg.setX((getWidth()-hiliteImg.getWidth())/2);
        hiliteImg.setY((getHeight()-hiliteImg.getHeight())/2);

        bgDown = new Image(Main.atlas.findRegion("level_icon_bg_down"));
        addActor(bgDown);

        bgDown.setX(bg.getX() + (bg.getWidth()-bgDown.getWidth())/2);
        bgDown.setY(bg.getY() + (bg.getHeight()-bgDown.getHeight())/2);
        bgDown.setVisible(false);

        lockImg = new Image(Main.atlas.findRegion("level_icon_lock"));
        lockImg.setX((getWidth()-lockImg.getWidth())/2);
        lockImg.setY((getHeight()-lockImg.getHeight())/2);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = Main.font40;

        label = new Label(id + "", style);
        label.setX((getWidth() - label.getWidth())/2);
        label.setY((getHeight() - label.getHeight())/2);
        label.setAlignment(Align.center);

        setLock(true);

        addCaptureListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                event.setTarget(LevelIcon.this);
                return true;
            }
        });

        addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                bgDown.setVisible(true);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                bgDown.setVisible(false);
                super.touchUp(event, x, y, pointer, button);
            }
        });

    }

    public int getId() {
        return id;
    }

    public void setLock(boolean lock) {
        if (lock) {
            label.remove();
            addActor(lockImg);
            setTouchable(Touchable.disabled);
        } else {
            lockImg.remove();
            addActor(label);
            setTouchable(Touchable.enabled);
        }
    }

    public void setHilite() {
        bg.setVisible(false);
        hiliteImg.setVisible(true);
        //isHilited = true;
    }

    @Override
    public void act(float delta) {
        if (isHilited) {
            float scaleParam = hiliteImg.getScaleX();

            if (alphaUp) {
                scaleParam += delta * 0.6;
                if (scaleParam >= 1) {
                    scaleParam = 1;
                    alphaUp = false;
                }
            } else {
                scaleParam -= delta * 0.6;
                if (scaleParam < 0.8) {
                    scaleParam = (float) 0.8;
                    alphaUp = true;
                }
            }
            hiliteImg.setScale(scaleParam);
            label.setFontScale(scaleParam);
        }
        super.act(delta);
    }
}
