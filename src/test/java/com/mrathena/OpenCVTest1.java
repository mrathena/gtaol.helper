package com.mrathena;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

@Slf4j
public class OpenCVTest1 {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		long start = System.currentTimeMillis();
		template(Imgproc.TM_SQDIFF_NORMED);
		System.out.println(System.currentTimeMillis() - start);
	}

	/**
	 * OpenCV-4.1.0 模板匹配
	 * <table border="1" cellpadding="8">
	 * <tr><th>输入参数</th><th>参数解释</th></tr>
	 * <tr><td align="left">TM_SQDIFF是平方差匹配、TM_SQDIFF_NORMED是标准平方差匹配</td><td>利用平方差来进行匹配,最好匹配为0.匹配越差,匹配值越大。</td></tr>
	 * <tr><td align="left">TM_CCORR是相关性匹配、TM_CCORR_NORMED是标准相关性匹配</td><td>采用模板和图像间的乘法操作,数越大表示匹配程度较高, 0表示最坏的匹配效果。</td></tr>
	 * <tr><td align="left">TM_CCOEFF是相关性系数匹配、TM_CCOEFF_NORMED是标准相关性系数匹配</td><td>将模版对其均值的相对值与图像对其均值的相关值进行匹配,1表示完美匹配,-1表示糟糕的匹配,0表示没有任何相关性(随机序列)。</td></tr>
	 * <tr><td colspan="2">随着从简单的测量(平方差)到更复杂的测量(相关系数),我们可获得越来越准确的匹配(同时也意味着越来越大的计算代价)。</td></tr>
	 * <tr><td colspan="2">相关性是越接近1越大越好，平方差是越小越好，所以TM_SQDIFF在使用时和其他的是有所区别的。</td></tr>
	 * <tr><td colspan="2">模板匹配结果Mat要是32位的。</td></tr>
	 * </table>
	 *
	 * @return: void
	 * @date: 2019年5月7日12:16:55
	 */
	public static void template(int method) {
		// 1 获取待匹配图片
		Mat big = Imgcodecs.imread("C:\\Users\\mrathena\\Desktop\\3440.1440.real.png");
		// 2 获取匹配模板
		Mat small = Imgcodecs.imread("C:\\Users\\mrathena\\Desktop\\picture\\small.1.1.png");

		log.info("{},{},{},{}", big.cols(), big.width(), small.cols(), small.rows());
		log.info("{},{},{},{}", big.width(), big.height(), small.width(), small.height());

		int width = big.cols() - small.cols() + 1;
		int height = big.rows() - small.rows() + 1;

		// 3 创建32位模板匹配结果Mat
		Mat result = new Mat(width, height, CvType.CV_32FC1);
		// 4 调用 模板匹配函数
		Imgproc.matchTemplate(big, small, result, method);
		// 5 归一化
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		// 6 获取模板匹配结果
		Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
		// 7 绘制匹配到的结果
		double x, y;
		if (method == Imgproc.TM_SQDIFF_NORMED || method == Imgproc.TM_SQDIFF) {
			x = mmr.minLoc.x;
			y = mmr.minLoc.y;
		} else {
			x = mmr.maxLoc.x;
			y = mmr.maxLoc.y;
		}
		log.info("{},{}", x, y);
		Imgproc.rectangle(big, new Point(x, y), new Point(x + small.cols(), y + small.rows()), new Scalar(0, 0, 255), 2, Imgproc.LINE_AA);
		Imgproc.putText(big, "Match Success", new Point(x, y), Imgproc.FONT_HERSHEY_SCRIPT_COMPLEX, 1.0, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA);
		// 8 显示结果
		HighGui.imshow("模板匹配", big);
		HighGui.waitKey(0);

	}
}
