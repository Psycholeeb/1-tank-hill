package vlad.stupak.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Created by Vlad on 26.02.2017.
 */

public class Packer {

    public static void main(String[] args) {
        TexturePacker.Settings set = new TexturePacker.Settings();
        set.filterMag = Texture.TextureFilter.MipMapLinearNearest;
        set.filterMag = Texture.TextureFilter.Linear;
        set.paddingX = 2;
        set.paddingY = 2;
        set.maxHeight = 2048;
        set.maxWidth = 2048;

        TexturePacker.process(set, "raw_images_ru", "android/assets/images_ru", "pack");
        //TexturePacker.process(set, "raw_images_en", "android/assets/images_en", "pack");
    }
}
