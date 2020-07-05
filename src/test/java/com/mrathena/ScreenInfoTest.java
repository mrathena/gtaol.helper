package com.mrathena;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class ScreenInfoTest {

	public static void main(String[] args) {

		// PPI 和 DPI
		//PPI (Pixels Per Inch) : 即每一英寸长度上有多少个像素点；
		//DPI (Dots Per Inch): 即每一英寸上有多少个点；
		//
		//这里一个是像素点（Pixel），一个是点（Dot），区别就在这里。像素点（Pixel）是一个最小的基本单位，是固定不变的。而点(Dot) 则不同，它可以根据输出或者显示需要来改变的，可以是 1Dot = 1Pixel，也可以是 1Dot = N Pixel。
		//
		//在电脑里，分辩率是可以调节的，这里用的就是Dot ，密度用DPI描述；只有当使用屏幕最大分辩率的时候（即 1Dot = 1Pixel），这个时候 DPI = PPI。
		Dimension pxSize = Toolkit.getDefaultToolkit().getScreenSize();
		int pxWidth = (int) pxSize.getWidth();
		int pxHeight = (int) pxSize.getHeight();
		// 屏幕的物理大小还需要知道屏幕的dpi 意思是说一英寸多少个象素
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		// 然后用象素除以dpi 就可以得到多少英寸了 你要是再不知道英寸怎么转换厘米 那我也不知道怎么帮忙了
		int size = (int) (Math.sqrt(pxWidth * pxWidth + pxHeight * pxHeight) / dpi * 2.54);
		// 计算英寸, 1英寸=2.54厘米
		log.info("{},{},{},{}", pxWidth, pxHeight, dpi, size);

		//
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();

		for (int i = 0; i < devices.length; i++) {
			System.out.println("Width:" + devices[i].getDisplayMode().getWidth());
			System.out.println("Height:" + devices[i].getDisplayMode().getHeight());
			System.out.println(devices[i].getDisplayMode());
		}

	}

}
