package com.mrathena;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class ImageCutTest {

	private final static String[] imgExts = new String[]{"jpg", "jpeg", "png", "bmp"};

	public static void main(String[] args) {

		save(crop("C:\\Users\\mrathena\\Desktop\\2560.1440.real.png",1184,208, 704,704), "C:\\Users\\mrathena\\Desktop\\big.1.png");

		int baseSmallLeft = 641, baseSmallTop = 370, baseSmallLength = 142, baseSpace = 50;
		for (int i = 0; i < 8; i ++) {
			int left = (i % 2 == 0) ? baseSmallLeft : baseSmallLeft + baseSmallLength + baseSpace;
			int top = baseSmallTop + (i / 2) * (baseSmallLength + baseSpace);
			save(crop("C:\\Users\\mrathena\\Desktop\\2560.1440.real.png",left, top, baseSmallLength, baseSmallLength), "C:\\Users\\mrathena\\Desktop\\small.1." + (i + 1) + ".png");
		}

	}

	public static String getExtName (String fileName){
		if (StringUtils.isEmpty(fileName)) return null;
		int idx = fileName.lastIndexOf('.');
		if (idx != -1 && (idx + 1) < fileName.length()) {
			return fileName.substring(idx + 1);
		} else {
			return null;
		}
	}
	//通过文件扩展名，是否为支持的图片文件
	public static boolean isImageExtName (String fileName){
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		fileName = fileName.trim().toLowerCase();
		String ext = getExtName(fileName);
		if (StringUtils.isEmpty(ext)) return false;
		for (String str : imgExts) {
			if (str.equals(ext)) {
				return true;
			}
		}
		return false;
	}

	public static final boolean notImageExtName (String fileName){
		return !isImageExtName(fileName);
	}
	public static BufferedImage loadImageFils (String sourceImageFileName){
		if (notImageExtName(sourceImageFileName)) {
			throw new IllegalArgumentException("只支持如下几种类型的图像文件：jpg、jpeg、png、bmp");
		}
		File sourceImageFile = new File(sourceImageFileName);
		if (!sourceImageFile.exists() || !sourceImageFile.isFile()) {
			throw new IllegalArgumentException("文件不存在");
		}
		try {
			return ImageIO.read(sourceImageFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void zoom ( int maxWidth, File srcFile, String saveFile){
		float quality = 0.8f;
		try {
			BufferedImage srcImage = ImageIO.read(srcFile);
			int srcWidth = srcImage.getWidth();
			int srcHeight = srcImage.getHeight();
			if (srcWidth <= maxWidth) {
				saveWithQuality(srcImage, quality, saveFile);
			} else {
				float scalingRatio = (float) maxWidth / (float) srcWidth;
				float maxHeight = ((float) srcHeight * scalingRatio);
				BufferedImage ret = resize(srcImage, maxWidth, (int) maxHeight);
				saveWithQuality(ret, quality, saveFile);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage crop (String sourceImageFile,int left, int top, int width, int height){
		if (notImageExtName(sourceImageFile)) {
			throw new IllegalArgumentException("只支持如下几种类型的图像文件：jpg、jpeg、png、bmp");
		}
		try {
			BufferedImage bi = ImageIO.read(new File(sourceImageFile));
			width = Math.min(width, bi.getWidth());
			height = Math.min(height, bi.getHeight());
			if (width <= 0) width = bi.getWidth();
			if (height <= 0) height = bi.getHeight();

			left = Math.min(Math.max(0, left), bi.getWidth() - width);
			top = Math.min(Math.max(0, top), bi.getHeight() - height);

			return bi.getSubimage(left, top, width, height);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void save (BufferedImage bi, String outputImageFile){
		FileOutputStream newImage = null;
		try {
			ImageIO.write(bi, getExtName(outputImageFile), new File(outputImageFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (newImage != null) {
				try {
					newImage.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static BufferedImage resize (BufferedImage bi, int toWidth, int toHeight){
		Graphics g = null;
		try {
			Image scaledImage = bi.getScaledInstance(toWidth, toHeight, Image.SCALE_SMOOTH);
			BufferedImage ret = new BufferedImage(toWidth, toHeight, BufferedImage.TYPE_INT_RGB);
			g = ret.getGraphics();
			g.drawImage(scaledImage, 0, 0, null);
			return ret;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (g != null) {
				g.dispose();
			}
		}
	}

	public static void saveWithQuality (BufferedImage im,float quality, String outputImageFile){
		FileOutputStream newImage = null;
		try {
			newImage = new FileOutputStream(outputImageFile);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newImage);
			JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(im);
			jep.setQuality(quality, true);
			encoder.encode(im, jep);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (newImage != null) {
				try {newImage.close();} catch (IOException e) {throw new RuntimeException(e);}
			}
		}
	}

}
