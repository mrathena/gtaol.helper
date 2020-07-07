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

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GTAOL {

	private static final JIntellitype jIntellitype = JIntellitype.getInstance();
	private static final Map<Integer, HotKey> hotKeyMap = new HashMap<>(8);

	public static void main(String[] args) {

		log.info("init: create hotkey f10");
		HotKey f10 = new HotKey(Key.F10, () -> {

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

			// 游戏存在开始找图
			log.info("over in {}ms", System.currentTimeMillis() - start);
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
	@ToString
	@AllArgsConstructor
	public static class Point {
		private final int x;
		private final int y;
	}

}
