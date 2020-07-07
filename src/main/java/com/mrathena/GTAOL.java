package com.mrathena;

import com.melloware.jintellitype.JIntellitype;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GTAOL {

	private static final JIntellitype jIntellitype = JIntellitype.getInstance();
	private static final Map<Integer, HotKey> hotKeyMap = new HashMap<>(8);

	public static void main(String[] args) {

		log.info("init: create hotkey f10");
		HotKey f10 = new HotKey(Key.F10, () -> {
			try {
				log.info("");
				long start = System.currentTimeMillis();

				// 判断窗口是否存在
				WinDef.HWND hwnd = User32.INSTANCE.FindWindow("grcWindow", "Grand Theft Auto V");
				if (null == hwnd) {
					throw new RuntimeException("windows not exist");
				}

				// 获取窗体信息
				WinUser.WINDOWINFO windowInfo = new WinUser.WINDOWINFO();
				if (!User32.INSTANCE.GetWindowInfo(hwnd, windowInfo)) {
					throw new RuntimeException("get windows info failure");
				}

				WinDef.RECT rcWindow = windowInfo.rcWindow;
				WinDef.RECT rcClient = windowInfo.rcClient;
				log.info("窗体, left:{}, top:{}, right:{}, bottom:{}, width:{}, height:{}", rcWindow.left, rcWindow.top, rcWindow.right, rcWindow.bottom, rcWindow.right - rcWindow.left, rcWindow.bottom - rcWindow.top);
				log.info("可视区: left:{}, top:{}, right:{}, bottom:{}, width:{}, height:{}", rcClient.left, rcClient.top, rcClient.right, rcClient.bottom, rcClient.right - rcClient.left, rcClient.bottom - rcClient.top);

				// 获取屏幕信息
				int width = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CXSCREEN);
				int height = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CYSCREEN);
				log.info("显示器: width:{}, height:{}", width, height);

				boolean isNonBorderWindow = windowInfo.rcWindow.top == windowInfo.rcClient.top;
				boolean isFullScreenWindow = windowInfo.dwWindowStatus == 1;
				boolean isMatchPhysicalResolution = rcClient.bottom - rcClient.top == height;
				log.info("isNonBorderWindow:{}, isFullScreenWindow:{}, isMatchPhysicalResolution:{}", isNonBorderWindow, isFullScreenWindow, isMatchPhysicalResolution);

				BufferedImage screenshot = (new Robot()).createScreenCapture(new Rectangle(0, 0, width, height));
				ImageIO.write(screenshot, "png", new File("C:\\Users\\mrathena\\Desktop\\test.png"));
				LockImage lockImage = LockImage.getNewLockImage(width, height);
				Rectangle bigRectangle = lockImage.getBigRectangle();
				List<Rectangle> smallRectangleList = lockImage.getSmallRectangleList();

				ExecutorService executorService = Executors.newFixedThreadPool(9);
				CountDownLatch countDownLatch = new CountDownLatch(9);
				executorService.execute(() -> {
					try {
						BufferedImage subImage = screenshot.getSubimage((int) bigRectangle.getX(), (int) bigRectangle.getY(), (int) bigRectangle.getWidth(), (int) bigRectangle.getHeight());
						ImageIO.write(subImage, "png", new File("C:\\Users\\mrathena\\Desktop\\test.png"));
					} catch (Throwable cause) {
						log.error("", cause);
					} finally {
						countDownLatch.countDown();
					}
				});
				for (int i = 1; i <= smallRectangleList.size(); i++) {
					int index = i;
					executorService.execute(() -> {
						try {
							Rectangle rectangle = smallRectangleList.get(index);
							BufferedImage subImage = screenshot.getSubimage((int) rectangle.getX(), (int) rectangle.getY(), (int) rectangle.getWidth(), (int) rectangle.getHeight());
							ImageIO.write(subImage, "png", new File("C:\\Users\\mrathena\\Desktop\\test." + index + ".png"));
						} catch (Throwable cause) {
							log.error("", cause);
						} finally {
							countDownLatch.countDown();
						}
					});
				}
				countDownLatch.await(10, TimeUnit.SECONDS);

				// 游戏存在开始找图
				log.info("over in {}ms", System.currentTimeMillis() - start);
			} catch (Throwable cause) {
				log.error("", cause);
			}
		}).register();
		hotKeyMap.put(f10.getIdentifier(), f10);

		log.info("init: create hotkey f11");
		HotKey f11 = new HotKey(Key.F11, () -> {
			log.info("");
			log.info("main thread will quit after unregister all hotkeys");
			System.exit(0);
		}).register();
		hotKeyMap.put(f11.getIdentifier(), f11);

		log.info("init: add hotkey listener");
		JIntellitype.getInstance().addHotKeyListener(identifier -> {
			try {
				hotKeyMap.get(identifier).getHook().run();
			} catch (Throwable cause) {
				log.error("", cause);
			}
		});

		log.info("init: add shutdown listener");
		Thread quitThread = new Thread(() -> {
			log.info("");
			log.info("quit: unregister hotkeys");
			hotKeyMap.values().forEach(HotKey::unregister);
			log.info("quit: bye");
		}, "Shutdown-Event-Hook-Thread");
		Runtime.getRuntime().addShutdownHook(quitThread);

		log.info("init: completed");
	}


	/**
	 * 热键类
	 */
	@Getter
	@Setter
	public static class HotKey {
		private final int interval = 65536;
		private final Key key;
		private final int identifier;
		private final Runnable hook;

		public HotKey(Key key, Runnable hook) {
			this.key = key;
			this.identifier = key.value + interval;
			this.hook = hook;
		}

		private HotKey register() {
			jIntellitype.registerHotKey(identifier, 0, key.value);
			return this;
		}

		private void unregister() {
			jIntellitype.unregisterHotKey(identifier);
		}
	}

	/**
	 * 键值
	 */
	@Getter
	@AllArgsConstructor
	public enum Key {
		F10(121),
		F11(122);
		private final int value;
	}

	/**
	 * 窗体
	 */
	@Getter
	@Setter
	@ToString
	public static class Window {
		private int left;
		private int top;
		private int right;
		private int bottom;
		private int width;
		private int height;
		private int isNonBorderWindow;
		private int isFullScreenWindow;
	}

	/**
	 * 点
	 */
	@Getter
	@Setter
	@AllArgsConstructor
	public static class Point {
		private int x;
		private int y;
	}

	/**
	 * 默认值为34寸物理分辨率3440*1440显示器在游戏分辨率3440*1440下的数据
	 * 锁图比例为16:9,即2560*1440,计算比例时需要按这个来
	 */
	@Getter
	@Setter
	@ToString
	public static class LockImage {
		private Point center = new Point(1280, 720);

		private int bigLeftToCenter = 96;
		private int bigTopToCenter = 512;
		private int bigWidth = 704;
		private int bigHeight = 704;
		private int maxSmallLeftToCenter = 640;
		private int maxSmallTopToCenter = 350;
		private int smallWidth = 142;
		private int smallHeight = 142;
		private int smallSpaceX = 50;
		private int smallSpaceY = 50;

		private Point big = new Point(1184, 208);
		private Point small1 = new Point(641, 370);
		private Point small2 = new Point(833, 370);
		private Point small3 = new Point(641, 562);
		private Point small4 = new Point(833, 562);
		private Point small5 = new Point(641, 754);
		private Point small6 = new Point(833, 754);
		private Point small7 = new Point(641, 946);
		private Point small8 = new Point(833, 946);

		public static LockImage getNewLockImage(int newWidth, int newHeight) {
			if (newWidth == 2560 && newHeight == 1440) {
				return new LockImage();
			}
			double xRate = newWidth / 2560D;
			double yRate = newHeight / 1440D;
			if (newHeight == 1440 && ((double) newWidth / newHeight > (double) 16 / 9)) {
				LockImage lockImage = new LockImage();
				int move = (newWidth - 2560) / 2;
				lockImage.getBig().setX(move + lockImage.getBig().getX());
				lockImage.getSmall1().setX(move + lockImage.getSmall1().getX());
				lockImage.getSmall2().setX(move + lockImage.getSmall2().getX());
				lockImage.getSmall3().setX(move + lockImage.getSmall3().getX());
				lockImage.getSmall4().setX(move + lockImage.getSmall4().getX());
				lockImage.getSmall5().setX(move + lockImage.getSmall5().getX());
				lockImage.getSmall6().setX(move + lockImage.getSmall6().getX());
				lockImage.getSmall7().setX(move + lockImage.getSmall7().getX());
				lockImage.getSmall8().setX(move + lockImage.getSmall8().getX());
				return lockImage;
			}
			LockImage lockImage = new LockImage();
			if (xRate != 1.0) {
				lockImage.setBigWidth((int) (xRate * lockImage.getBigWidth()));
				lockImage.setSmallWidth((int) (xRate * lockImage.getSmallWidth()));
				lockImage.setSmallSpaceX((int) (xRate * lockImage.getSmallSpaceX()));
				lockImage.getBig().setX((int) (xRate * lockImage.getBig().getX()));
				lockImage.getSmall1().setX((int) (xRate * lockImage.getSmall1().getX()));
				lockImage.getSmall2().setX((int) (xRate * lockImage.getSmall2().getX()));
				lockImage.getSmall3().setX((int) (xRate * lockImage.getSmall3().getX()));
				lockImage.getSmall4().setX((int) (xRate * lockImage.getSmall4().getX()));
				lockImage.getSmall5().setX((int) (xRate * lockImage.getSmall5().getX()));
				lockImage.getSmall6().setX((int) (xRate * lockImage.getSmall6().getX()));
				lockImage.getSmall7().setX((int) (xRate * lockImage.getSmall7().getX()));
				lockImage.getSmall8().setX((int) (xRate * lockImage.getSmall8().getX()));
			}
			if (yRate != 1.0) {
				lockImage.setBigHeight((int) (yRate * lockImage.getBigHeight()));
				lockImage.setSmallHeight((int) (yRate * lockImage.getSmallHeight()));
				lockImage.setSmallSpaceY((int) (yRate * lockImage.getSmallSpaceY()));
				lockImage.getBig().setY((int) (yRate * lockImage.getBig().getY()));
				lockImage.getSmall1().setY((int) (yRate * lockImage.getSmall1().getY()));
				lockImage.getSmall2().setY((int) (yRate * lockImage.getSmall2().getY()));
				lockImage.getSmall3().setY((int) (yRate * lockImage.getSmall3().getY()));
				lockImage.getSmall4().setY((int) (yRate * lockImage.getSmall4().getY()));
				lockImage.getSmall5().setY((int) (yRate * lockImage.getSmall5().getY()));
				lockImage.getSmall6().setY((int) (yRate * lockImage.getSmall6().getY()));
				lockImage.getSmall7().setY((int) (yRate * lockImage.getSmall7().getY()));
				lockImage.getSmall8().setY((int) (yRate * lockImage.getSmall8().getY()));
			}
			return lockImage;
		}

		private Rectangle getBigRectangle() {
			return new Rectangle(big.x, big.y, bigWidth, bigHeight);
		}

		private List<Rectangle> getSmallRectangleList() {
			List<Rectangle> list = new LinkedList<>();
			list.add(new Rectangle(small1.x, small1.y, smallWidth, smallHeight));
			list.add(new Rectangle(small2.x, small2.y, smallWidth, smallHeight));
			list.add(new Rectangle(small3.x, small3.y, smallWidth, smallHeight));
			list.add(new Rectangle(small4.x, small4.y, smallWidth, smallHeight));
			list.add(new Rectangle(small5.x, small5.y, smallWidth, smallHeight));
			list.add(new Rectangle(small6.x, small6.y, smallWidth, smallHeight));
			list.add(new Rectangle(small7.x, small7.y, smallWidth, smallHeight));
			list.add(new Rectangle(small8.x, small8.y, smallWidth, smallHeight));
			return list;
		}

	}

}
