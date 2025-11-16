package org.seed.seedstech.lib;

public class Lib
{
	public static String getFluidAmountString(int amount)
	{
		if (amount >= 10000)
		{
			int bucket = amount / 1000;
			int milliBucket = (amount / 100) % 10;
			if (milliBucket == 0)
				return String.format("%dB", bucket);
			return String.format("%d.%dB", bucket, milliBucket);
		}
		return String.format("%dmB", amount);
	}
}
