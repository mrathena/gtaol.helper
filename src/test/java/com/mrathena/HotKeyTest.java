package com.mrathena;

import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HotKeyTest {

	private static final JIntellitype jIntellitype = JIntellitype.getInstance();

	private static final int KEY_F10 = 121;
	private static final int KEY_F11 = 122;
	private static final int HOTKEY_F10 = 65537;
	private static final int HOTKEY_F11 = 65538;

	public static void main(String[] args) {
		log.info("init");
		jIntellitype.registerHotKey(HOTKEY_F10, 0, KEY_F10);
		jIntellitype.registerHotKey(HOTKEY_F11, 0, KEY_F11);
		JIntellitype.getInstance().addHotKeyListener(identifier -> {
			if (HOTKEY_F10 == identifier) {
				log.info("F10被按下");
				log.info("F10执行结束");
			} else if (HOTKEY_F11 == identifier) {
				log.info("will quit after unregister all hotkeys");
				System.exit(0);
			}
		});
		log.info("init completed");
		Thread quitThread = new Thread(() -> {
			log.info("before quit");
			jIntellitype.unregisterHotKey(HOTKEY_F10);
			jIntellitype.unregisterHotKey(HOTKEY_F11);
			log.info("quit");
		}, "Quit-Thread");
		Runtime.getRuntime().addShutdownHook(quitThread);
	}

}
