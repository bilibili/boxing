/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bilibili.boxing.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;


/**
 * A compress for image.
 *
 * @author ChenSL
 */
public class ImageCompressor {
    public static final long MAX_LIMIT_SIZE_LONG = 1024 * 1024L;

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final int MAX_WIDTH = 3024;
    private static final int MAX_HEIGHT = 4032;
    private static final long MAX_LIMIT_SIZE = 300 * 1024L;

    private static final String COMPRESS_FILE_PREFIX = "compress-";

    private File mOutFileFile;

    public ImageCompressor(@NonNull File cachedRootDir) {
        if (cachedRootDir != null) {
            mOutFileFile = new File(cachedRootDir.getAbsolutePath() + File.separator + ".compress" + File.separator);
        }
    }

    public ImageCompressor(@NonNull Context context) {
        if (context != null) {
            String rootDir = BoxingFileHelper.getCacheDir(context);
            if (TextUtils.isEmpty(rootDir)) {
                throw new IllegalStateException("the cache dir is null");
            }
            mOutFileFile = new File(rootDir + File.separator + ".compress" + File.separator);
        }
    }

    public File compress(@NonNull File file) throws IOException, NullPointerException, IllegalArgumentException {
        return compress(file, MAX_LIMIT_SIZE);
    }

    /**
     * @param file file to compress.
     * @param maxsize the proximate max size for compression, not for the image with large ratio.
     * @return may be a little bigger than expected for performance.
     */
    public File compress(@NonNull File file, long maxsize) throws IOException, NullPointerException, IllegalArgumentException {
        if (!file.exists()) {
            throw new IllegalArgumentException("file not found : " + file.getAbsolutePath());
        }
        if (!isLegalFile(file)) {
            throw new IllegalArgumentException("file is not a legal file : " + file.getAbsolutePath());
        }
        if (mOutFileFile == null) {
            throw new NullPointerException("the external cache dir is null");
        }
        BitmapFactory.Options checkOptions = new BitmapFactory.Options();
        checkOptions.inJustDecodeBounds = true;
        String absPath = file.getAbsolutePath();
        int angle = BoxingExifHelper.getRotateDegree(absPath);
        BitmapFactory.decodeFile(absPath, checkOptions);

        if (checkOptions.outWidth <= 0 || checkOptions.outHeight <= 0) {
            throw new IllegalArgumentException("file is not a legal bitmap with 0 with or 0 height : " + file.getAbsolutePath());
        }
        int width = checkOptions.outWidth;
        int height = checkOptions.outHeight;
        File outFile = createCompressFile(file);
        if (outFile == null) {
            throw new NullPointerException("the compressed file create fail, the compressed path is null.");
        }
        if (!isLargeRatio(width, height)) {
            int[] display = getCompressDisplay(width, height);
            Bitmap bitmap = compressDisplay(absPath, display[0], display[1]);
            Bitmap rotatedBitmap = rotatingImage(angle, bitmap);
            if (bitmap != rotatedBitmap) {
                bitmap.recycle();
            }
            saveBitmap(rotatedBitmap, outFile);
            rotatedBitmap.recycle();
            compressQuality(outFile, maxsize, 20);
        } else {
            if (checkOptions.outHeight >= MAX_HEIGHT && checkOptions.outWidth >= MAX_WIDTH) {
                checkOptions.inSampleSize = 2;
            }
            checkOptions.inJustDecodeBounds = false;
            Bitmap originBitmap = BitmapFactory.decodeFile(absPath, checkOptions);
            Bitmap rotatedBitmap = rotatingImage(angle, originBitmap);
            if (originBitmap != rotatedBitmap) {
                originBitmap.recycle();
            }
            saveBitmap(originBitmap, outFile);
            rotatedBitmap.recycle();
            compressQuality(outFile, MAX_LIMIT_SIZE_LONG, 50);
        }
        BoxingLog.d("compress suc: " + outFile.getAbsolutePath());
        return outFile;
    }

    private Bitmap rotatingImage(int angle, Bitmap bitmap) {
        if (angle == 0) {
            return bitmap;
        }
        //rotate image
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        //create a new image
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void saveBitmap(Bitmap bitmap, File outFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        try {
            fos.flush();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                BoxingLog.d("IOException when saving a bitmap");
            }
        }
    }

    /**
     * @param width  must > 0
     * @param height must > 0
     */
    private int[] getCompressDisplay(int width, int height) {
        int thumbWidth = width % 2 == 1 ? width + 1 : width;
        int thumbHeight = height % 2 == 1 ? height + 1 : height;
        int[] results = new int[]{thumbWidth, thumbHeight};

        width = thumbWidth > thumbHeight ? thumbHeight : thumbWidth;
        height = thumbWidth > thumbHeight ? thumbWidth : thumbHeight;
        float scale = (float) width / height;
        if (scale <= 1 && scale >= 0.5625) {
            if (height < 1664) {
                thumbWidth = width;
                thumbHeight = height;
            } else if (height >= 1664 && height < 4990) {
                thumbWidth = width / 2;
                thumbHeight = height / 2;
            } else if (height >= 4990 && height < 10240) {
                thumbWidth = width / 4;
                thumbHeight = height / 4;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbWidth = width / multiple;
                thumbHeight = height / multiple;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (height < 1280) {
                thumbWidth = width;
                thumbHeight = height;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbWidth = width / multiple;
                thumbHeight = height / multiple;
            }
        } else {
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbWidth = width / multiple;
            thumbHeight = height / multiple;
        }
        results[0] = thumbWidth;
        results[1] = thumbHeight;
        return results;
    }

    /**
     * @param width  must > 0
     * @param height must > 0
     */
    private Bitmap compressDisplay(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;

        if (outH > height || outW > width) {
            int halfH = outH / 2;
            int halfW = outW / 2;
            while ((halfH / inSampleSize) > height && (halfW / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;

        int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    private void compressQuality(File outFile, long maxSize, int maxQuality) throws IOException {
        long length = outFile.length();
        int quality = 90;
        if (length > maxSize) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BoxingLog.d("source file size : " + outFile.length() + ",path : " + outFile);
            while (true) {
                compressPhotoByQuality(outFile, bos, quality);
                long size = bos.size();
                BoxingLog.d("compressed file size : " + size);
                if (quality <= maxQuality) {
                    break;
                }
                if (size < maxSize) {
                    break;
                } else {
                    quality -= 10;
                    bos.reset();
                }
            }
            OutputStream fos = new FileOutputStream(outFile);
            bos.writeTo(fos);
            bos.flush();
            fos.close();
            bos.close();
        }
    }

    private void compressPhotoByQuality(File file, final OutputStream os, final int quality) throws IOException, OutOfMemoryError {
        BoxingLog.d("start compress quality... ");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
            bitmap.recycle();
        } else {
            throw new NullPointerException("bitmap is null when compress by quality");
        }
    }

    private File createCompressFile(File file) throws IOException {
        File outFile = getCompressOutFile(file);
        if (!mOutFileFile.exists()) {
            mOutFileFile.mkdirs();
        }
        BoxingLog.d("compress out file : " + outFile);
        outFile.createNewFile();
        return outFile;
    }

    public @Nullable File getCompressOutFile(File file) {
        String path = getCompressOutFilePath(file);
        return TextUtils.isEmpty(path) ? null: new File(path);
    }

    public @Nullable File getCompressOutFile(String filePth) {
        String path = getCompressOutFilePath(filePth);
        return TextUtils.isEmpty(path) ? null: new File(path);
    }

    public @Nullable String getCompressOutFilePath(File file) {
        return getCompressOutFilePath(file.getAbsolutePath());
    }

    public @Nullable String getCompressOutFilePath(String filePath) {
        try {
            return mOutFileFile + File.separator + COMPRESS_FILE_PREFIX + signMD5(filePath.getBytes("UTF-8")) + ".jpg";
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public String signMD5(byte[] source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return signDigest(source, digest);
        } catch (NoSuchAlgorithmException e) {
            BoxingLog.d("have no md5");
        }
        return null;
    }

    private String signDigest(byte[] source, MessageDigest digest) {
        digest.update(source);
        byte[] data = digest.digest();
        int j = data.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (byte byte0 : data) {
            str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
            str[k++] = HEX_DIGITS[byte0 & 0xf];
        }
        return new String(str).toLowerCase(Locale.getDefault());
    }

    private boolean isLargeRatio(int width, int height) {
        return width / height >= 3 || height / width >= 3;
    }

    private boolean isLegalFile(File file) {
        return file != null && file.exists() && file.isFile() && file.length() > 0;
    }
}
