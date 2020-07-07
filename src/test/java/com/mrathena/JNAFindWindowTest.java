package com.mrathena;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JNAFindWindowTest {

	/**
	 * DLL动态库调用方法
	 * 读取调用CDecl方式导出的DLL动态库方法
	 */
	public interface CLibrary extends Library {
		// DLL文件默认路径为项目根目录，若DLL文件存放在项目外，请使用绝对路径。（此处：(Platform.isWindows()?"msvcrt":"c")指本地动态库msvcrt.dll）
		CLibrary INSTANCE = Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"), CLibrary.class);

		// 声明将要调用的DLL中的方法,可以是多个方法(此处示例调用本地动态库msvcrt.dll中的printf()方法)
		void printf(String format, Object... args);
	}

	public interface CLibrary2 extends Library {
		CLibrary2 INSTANCE = Native.load((Platform.isWindows() ? "msvcrt" : "c"), CLibrary2.class);

		void printf(String format, Object... args);
	}

	public static void main(String[] args) {
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow("WeChatMainWndForPC", "微信");
		System.out.println(hwnd);
	}

}
