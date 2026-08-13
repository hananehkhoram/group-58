package com.workshop.controller.repository;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class PamAnimationLoader {

    public static Texture loadFirstTextureFromPam(FileHandle file) {
        try (InputStream is = file.read()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();

            // ۱. جستجو برای الگوی PNG معتبر در دل فایل باینری (PNG Magic Header: 89 50 4E 47 0D 0A 1A 0A)
            int pngOffset = findSequence(bytes, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

            if (pngOffset != -1) {
                byte[] pngBytes = new byte[bytes.length - pngOffset];
                System.arraycopy(bytes, pngOffset, pngBytes, 0, pngBytes.length);
                Pixmap pixmap = new Pixmap(pngBytes, 0, pngBytes.length);
                Texture tex = new Texture(pixmap);
                pixmap.dispose();
                return tex;
            }

            // ۲. در صورتی که دیتای PNG فشرده نشده باشد و بایت‌های خام باشد
            // تلاش برای ساخت Pixmap ساده بر اساس ابعاد اولیه
            return null;

        } catch (Exception e) {
            Gdx.app.error("PamAnimationLoader", "Error reading PAM animation bytes: " + file.path(), e);
            return null;
        }
    }

    private static int findSequence(byte[] data, byte[] sequence) {
        for (int i = 0; i < data.length - sequence.length; i++) {
            boolean match = true;
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
}
