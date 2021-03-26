package net.runelite.client.plugins.gpu.util;

import static com.jogamp.opengl.math.FloatUtil.E;
import static com.jogamp.opengl.math.FloatUtil.TWO_PI;
import static com.jogamp.opengl.math.FloatUtil.pow;
import static com.jogamp.opengl.math.FloatUtil.sqrt;
import java.util.Arrays;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BlurKernel
{
	public float[] halfKernel;
	public int size;

	/**
	 * Generates a 1D gaussian blur half-kernel which only includes values starting from the midpoint of the full kernel
	 * @param kernelSize A kernel size of 5 will return 3 values, a kernel size of 2 will return 1
	 * @return
	 */
	public static BlurKernel calculateGaussian(int kernelSize)
	{
		float sigma = kernelSize / 2.f;

		float offset = kernelSize % 2 == 0 ? .5f : 0;

		float half = kernelSize / 2.f;

		float[] kernel = new float[(int) Math.ceil(half)];
		kernel[0] = calculateGaussianWeight(offset, sigma);
		while (++offset < half)
		{
			kernel[(int) offset] = calculateGaussianWeight(offset, sigma);
		}

		float sum = kernel[0];
		for (int i = kernelSize % 2; i < kernel.length; i++)
		{
			sum += kernel[i] * 2;
		}

		for (int i = 0; i < kernel.length; i++)
		{
			kernel[i] /= sum;
		}

		sum = kernel[0];
		for (int i = kernelSize % 2; i < kernel.length; i++)
		{
			sum += kernel[i] * 2;
		}
		System.out.println("sum: " + sum + ", kernel: " + Arrays.toString(kernel));

//		float fMidpoint = kernelSize / 2.f;
//		int halfSize = (int) Math.ceil(fMidpoint);
//
//		float[] kernel = new float[halfSize];
//		kernel[0] = calculateGaussianWeight(fMidpoint, sigma);
//		for (int i = 1; i < halfSize; i++)
//		{
//			float w = calculateGaussianWeight(fMidpoint + i, sigma);
//			kernel[i] = w;
//		}

		return new BlurKernel(kernel, kernelSize);
	}

	private static float calculateGaussianWeight(float x, float sigma)
	{
		float sigma2 = pow(sigma, 2);
		return 1.f / sqrt(TWO_PI * sigma2) * pow(E, -pow(x, 2) / (2 * sigma2));
	}
}