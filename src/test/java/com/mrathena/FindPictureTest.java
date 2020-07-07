package com.mrathena;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FindPictureTest {

	static ExecutorService executorService;

	static {
		executorService = Executors.newFixedThreadPool(4);
	}

	public static void main(String[] args) throws Throwable {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		long start = System.currentTimeMillis();
		System.out.println(findSmall(1));
		System.out.println(System.currentTimeMillis() - start);
	}

	public static int findBig(int index) {
		// 1 获取待匹配图片
		Mat big = Imgcodecs.imread("C:\\Users\\mrathena\\Desktop\\3440.1440.real.png");
		// 2 获取匹配模板
		Mat small = Imgcodecs.imread("C:\\Users\\mrathena\\Desktop\\picture\\big." + index + ".png");

		log.info("{},{},{},{}", big.cols(), big.width(), small.cols(), small.rows());
		log.info("{},{},{},{}", big.width(), big.height(), small.width(), small.height());

		int width = big.cols() - small.cols() + 1;
		int height = big.rows() - small.rows() + 1;

		// 3 创建32位模板匹配结果Mat
		Mat result = new Mat(width, height, CvType.CV_32FC1);
		// 4 调用 模板匹配函数
		int method = Imgproc.TM_SQDIFF_NORMED;
		Imgproc.matchTemplate(big, small, result, method);
		// 5 归一化
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		// 6 获取模板匹配结果
		Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
		// 7 绘制匹配到的结果

		return mmr.minLoc.x > 0 ? index : 0;
	}

	public static List<Point> findSmall(int bigIndex) throws Throwable {
		List<Point> pointList = Collections.synchronizedList(new LinkedList<>());
//		Mat big = Imgcodecs.imread("C:\\Users\\mrathena\\Desktop\\3440.1440.real.png");
		Mat big = Imgcodecs.imread("C:\\Users\\mrathena\\Desktop\\1111.png");
		CountDownLatch countDownLatch = new CountDownLatch(4);
		for (int i = 1; i <= 4; i++) {
			int smallIndex = i;
			executorService.execute(() -> {
				try {
					// 2 获取匹配模板
					long start = System.currentTimeMillis();
					Mat small = Imgcodecs.imread("C:\\Users\\mrathena\\Desktop\\picture\\small." + bigIndex + "." + smallIndex + ".png");

					int width = big.cols() - small.cols() + 1;
					int height = big.rows() - small.rows() + 1;

					// 3 创建32位模板匹配结果Mat
					Mat result = new Mat(width, height, CvType.CV_32FC1);
					// 4 调用 模板匹配函数
					int method = Imgproc.TM_SQDIFF_NORMED;
					Imgproc.matchTemplate(big, small, result, method);
					// 5 归一化
					Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
					// 6 获取模板匹配结果
					Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
					// 7 绘制匹配到的结果

					log.info("small.{}.{}, ({},{}), {}ms", bigIndex, smallIndex, mmr.minLoc.x, mmr.minLoc.y, System.currentTimeMillis() - start);
					pointList.add(mmr.minLoc);
				} finally {
					countDownLatch.countDown();
				}
			});
		}
		countDownLatch.await(5, TimeUnit.SECONDS);
		Comparator<Point> comparator = Comparator.comparing(point -> point.x);
		comparator = comparator.thenComparing(point -> point.y);
		pointList.sort(comparator);
		log.info("{}", pointList);
		return pointList;
	}

}
