package vlad.stupak.mediafile;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import vlad.stupak.Main;

import static com.badlogic.gdx.scenes.scene2d.Touchable.disabled;

public class LevelIcon extends Group{
    private int id;
    private Label label;
    private ImageButton lockImg, bg, currentIcon;
    private boolean isPulsation = false;
    private boolean alphaUp = false;

    public LevelIcon(int id) {
        this.id = id;

        currentIcon = new ImageButton(new TextureRegionDrawable(Main.atlas.findRegion("level_icon_current")),
                new TextureRegionDrawable(Main.atlas.findRegion("level_icon_current_down")));
        currentIcon.setOrigin(currentIcon.getWidth()/2, currentIcon.getHeight()/2);
        addActor(currentIcon);
        currentIcon.setVisible(false);

        bg = new ImageButton(new TextureRegionDrawable(Main.atlas.findRegion("level_icon_bg")),
                new TextureRegionDrawable(Main.atlas.findRegion("level_icon_bg_down")));
        addActor(bg);
        setSize(bg.getWidth(), bg.getHeight());

        currentIcon.setX((getWidth()- currentIcon.getWidth())/2);
        currentIcon.setY((getHeight()- currentIcon.getHeight())/2);

        lockImg = new ImageButton(new TextureRegionDrawable(Main.atlas.findRegion("level_icon_lock")),
                new TextureRegionDrawable(Main.atlas.findRegion("level_icon_lock")));
        lockImg.setX((getWidth()-lockImg.getWidth())/2);
        lockImg.setY((getHeight()-lockImg.getHeight())/2);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = Main.font40;

        label = new Label(id + "", style);
        label.setX((getWidth() - label.getWidth())/2);
        label.setY((getHeight() - label.getHeight())/2);
        label.setAlignment(Align.center);
        label.setTouchable(disabled);

        setLock(true);

        addCaptureListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                event.setTarget(LevelIcon.this);
                return true;
            }
        });
    }

    public int getId() {
        return id;
    }

    public void setLock(boolean lock) {
        if (lock) {
            label.remove();
            bg.remove();
            addActor(lockImg);
            setTouchable(disabled);
        } else {
            lockImg.remove();
            addActor(bg);
            addActor(label);
            setTouchable(Touchable.enabled);
        }
    }

    public void setHilite() {
        bg.setVisible(false);
        currentIcon.setVisible(true);
        //isPulsation = true;
    }

    @Override
    public void act(float delta) {
        if (isPulsation) {
            float scaleParam = currentIcon.getScaleX();

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
            currentIcon.setScale(scaleParam);
            label.setFontScale(scaleParam);
        }
        super.act(delta);
    }
}
