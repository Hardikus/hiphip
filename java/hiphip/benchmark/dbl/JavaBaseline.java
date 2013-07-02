package hiphip.benchmark.dbl;

import clojure.lang.IFn;

public class JavaBaseline {
  public static int alength(double [] arr) {
    return arr.length;
  }

  public static double aget(double [] arr, int idx) {
    return arr[idx];
  }

  public static void aset(double [] arr, int idx, double v) {
    arr[idx] = v;
  }

  public static void ainc(double [] arr, int idx, int v) {
    arr[idx]+=v;
  }
  
  public static double[] aclone(double [] arr) {
    return arr.clone();
  }
  
  // tests areduce and dot-product 
  public static double dot_product(double[] arr1, double[] arr2) {
    double s = 0;
    for (int i = 0; i < arr1.length; i++) {
      s += arr1[i] * arr2[i];
    }
    return s;
  }

  
  // tests doarr and afill!
  public static double[] multiply_in_place_pointwise(double[] xs, double[] ys) {
    for(int i = 0; i < xs.length; i++) {
      xs[i] *= ys[i];
    }
    return xs;
  }

  // tests afill!
  public static double[] multiply_in_place_by_idx(double[] xs) {
    for(int i = 0; i < xs.length; i++) {
      xs[i] *= i;
    }
    return xs;
  }

  // tests amake
  public static double[] acopy_inc(int len, double[] ys) {
    double[] ret = new double[len];
    for(int i = 0; i < len; i++) {
      ret[i] = ys[i] + 1;
    }
    return ret;
  }

  public static double[] amap_inc(double[] arr) {
    double[] ret = new double[arr.length];
    for (int i = 0; i < arr.length; i++) {
      ret[i] = arr[i] + 1;
    }
    return ret;
  }

  public static double[] amap_plus_idx(double[] arr) {
    double[] newarr = new double[arr.length];
    for (int i = 0; i < arr.length; i++) {
      newarr[i] = arr[i] + i;
    }
    return newarr;
  }

  public static double asum(double[] arr) {
    double s = 0;
    for (double d : arr) {
      s += d;
    }
    return s;
  }

  public static double asum_square(double[] arr) {
    double s = 0;
    for (double d : arr) {
      s += d * d;
    }
    return s;
  }

  public static double aproduct(double[] arr) {
    double s = 1;
    for (double d : arr) {
      s *= d;
    }
    return s;
  }

  public static double amax(double[] arr) {
    double m = Double.MIN_VALUE;
    for (double d : arr)
      if (d > m) m = d;
    return m;
  }

  public static double amin(double[] arr) {
    double m = Double.MAX_VALUE;
    for (double d : arr)
      if (d < m) m = d;
    return m;
  }

  public static double amean(double[] arr) {
    return asum(arr) / arr.length;
  }
}
