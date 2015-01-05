package com.dozingcatsoftware.vectorpinball.util;

public class MathUtils {

    public static final double TAU = 2 * Math.PI;

	public static float asFloat(Object obj, float defvalue) {
		if (obj instanceof Number) return ((Number)obj).floatValue();
		return defvalue;
	}

	public static float asFloat(Object obj) {
		return asFloat(obj, 0);
	}

    public static double asDouble(Object obj, double defvalue) {
        if (obj instanceof Number) return ((Number)obj).doubleValue();
        return defvalue;
    }

    public static double asDouble(Object obj) {
        return asDouble(obj, 0);
    }

    public static int asInt(Object obj, int defvalue) {
        if (obj instanceof Number) return ((Number)obj).intValue();
        return defvalue;
    }

    public static int asInt(Object obj) {
        return asInt(obj, 0);
    }

	public static float toRadians(float degrees) {
		return (float)(TAU/360) * degrees;
	}

	public static float toDegrees(float radians) {
	    return (float)(radians * 360/TAU);
	}

    public static double toRadians(double degrees) {
        return (TAU/360) * degrees;
    }

    public static double toDegrees(double radians) {
        return (radians * 360/TAU);
    }
}
