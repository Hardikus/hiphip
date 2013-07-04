package hiphip.benchmark.float_;

import clojure.lang.IFn;

public class JavaBaseline {
  public static int alength(float [] arr) {
    return arr.length;
  }

  public static float aget(float [] arr, int idx) {
    return arr[idx];
  }

  public static float aset(float [] arr, int idx, float v) {
    arr[idx] = v;
    return v;
  }

  public static float ainc(float [] arr, int idx, int v) {
    return arr[idx]+=v;
  }
  
  public static float[] aclone(float [] arr) {
    return arr.clone();
  }
  
  // tests areduce and dot-product 
  public static float dot_product(float[] arr1, float[] arr2) {
    float s = 0;
    for (int i = 0; i < arr1.length; i++) {
      s += arr1[i] * arr2[i];
    }
    return s;
  }

  
  // tests doarr and afill!
  public static float[] multiply_in_place_pointwise(float[] xs, float[] ys) {
    for(int i = 0; i < xs.length; i++) {
      xs[i] *= ys[i];
    }
    return xs;
  }

  // tests afill!
  public static float[] multiply_in_place_by_idx(float[] xs) {
    for(int i = 0; i < xs.length; i++) {
      xs[i] *= i;
    }
    return xs;
  }

  // tests amake
  public static float[] acopy_inc(int len, float[] ys) {
    float[] ret = new float[len];
    for(int i = 0; i < len; i++) {
      ret[i] = ys[i] + 1;
    }
    return ret;
  }

  public static float[] amap_inc(float[] arr) {
    float[] ret = new float[arr.length];
    for (int i = 0; i < arr.length; i++) {
      ret[i] = arr[i] + 1;
    }
    return ret;
  }

  public static float[] amap_plus_idx(float[] arr) {
    float[] newarr = new float[arr.length];
    for (int i = 0; i < arr.length; i++) {
      newarr[i] = arr[i] + i;
    }
    return newarr;
  }

  public static float asum(float[] arr) {
    float s = 0;
    for (float d : arr) {
      s += d;
    }
    return s;
  }

  public static float asum_square(float[] arr) {
    float s = 0;
    for (float d : arr) {
      s += d * d;
    }
    return s;
  }

  public static float aproduct(float[] arr) {
    float s = 1;
    for (float d : arr) {
      s *= d;
    }
    return s;
  }

  public static float amax(float[] arr) {
    float m = Float.MIN_VALUE;
    for (float d : arr)
      if (d > m) m = d;
    return m;
  }

  public static float amin(float[] arr) {
    float m = Float.MAX_VALUE;
    for (float d : arr)
      if (d < m) m = d;
    return m;
  }

  public static float amean(float[] arr) {
    return asum(arr) / arr.length;
  }
}
