package space.yaooxx.geohash;

import java.util.*;

/**
 * Geohash编码转换工具
 */
public class GeohashHelper {

    //Geohash默认格网划分次数为8次，误差19m左右
    private static int DEFAULT_LEVEL = 8;

    /**
     * 经纬度转geohash编码
     *
     * @param lng
     * @param lat
     * @param level 指定编码长度
     * @return geohash编码字符串
     */
    public static String convertLnglat2Geohash(double lng, double lat, int level) {

        //经纬二进制隔行扫描后的总长度
        int len = level * 5;

        double lngMax = 180;
        double lngMin = -180;
        double latMax = 90;
        double latMin = -90;

        int[] geoBin = new int[len];

        for (int i = 0; i < len; i++) {

            if ((i | 1) != i) {
                ///偶数 经度
                double lngMid = (lngMax + lngMin) / 2;
                if (lng > lngMid) {
                    geoBin[i] = 1;
                    lngMin = lngMid;
                } else {
                    geoBin[i] = 0;
                    lngMax = lngMid;
                }
            } else {

                ///奇数 纬度
                double latMid = (latMax + latMin) / 2;
                if (lat > latMid) {
                    geoBin[i] = 1;
                    latMin = latMid;
                } else {
                    geoBin[i] = 0;
                    latMax = latMid;
                }
            }
        }
        return Base32.encode(geoBin);
    }

    public static String convertLnglat2Geohash(double lng, double lat) {
        return convertLnglat2Geohash(lng, lat, DEFAULT_LEVEL);
    }

    public static Point toGPS(String geohash) {
        int[] geoBin = Base32.decode(geohash);
        int len = geoBin.length;

        double lngMax = 180;
        double lngMin = -180;
        double latMax = 90;
        double latMin = -90;

        int[] lngBin = new int[len / 2];
        int[] latBin = new int[len / 2];

        for (int i = 0; i < len; i++) {
            //偶数 经度
            if (i % 2 == 0) {
                if (geoBin[i] == 1) {
                    lngMin = (lngMax + lngMin) / 2;
                } else {
                    lngMax = (lngMax + lngMin) / 2;
                }
                lngBin[i / 2] = geoBin[i];
                //奇数 纬度
            } else {
                if (geoBin[i] == 1) {
                    latMin = (latMax + latMin) / 2;
                } else {
                    latMax = (latMax + latMin) / 2;
                }
                latBin[i / 2] = geoBin[i];
            }
        }
        return new Point((lngMax + lngMin) / 2, (latMax + latMin) / 2, lngBin, latBin);
    }

    public static List<String> getRound8(String geohash) {
        List<String> l = new ArrayList<>();
        Point point = toGPS(geohash);

        int[] midLatBin = point.latBin;
        int[] lowLatBin = decrBin(point.latBin);
        int[] highLatBin = incrBin(point.latBin);

        int[] midLngBin = point.lngBin;
        int[] lowLngBin = decrBin(point.lngBin);
        int[] highLngBin = incrBin(point.lngBin);


        l.add(Base32.encode(mergeLngLat(lowLngBin, lowLatBin)));
        l.add(Base32.encode(mergeLngLat(lowLngBin, midLatBin)));
        l.add(Base32.encode(mergeLngLat(lowLngBin, highLatBin)));

        l.add(Base32.encode(mergeLngLat(midLngBin, lowLatBin)));
        l.add(Base32.encode(mergeLngLat(midLngBin, highLatBin)));

        l.add(Base32.encode(mergeLngLat(highLngBin, lowLatBin)));
        l.add(Base32.encode(mergeLngLat(highLngBin, midLatBin)));
        l.add(Base32.encode(mergeLngLat(highLngBin, highLatBin)));


        return l;
    }

    private static int[] mergeLngLat(int[] lngBin, int[] latBin) {
        int[] geoBin = new int[latBin.length + lngBin.length];
        for (int i = 0; i < geoBin.length; i++) {
            if (i % 2 == 0) {
                geoBin[i] = lngBin[i / 2];
            } else {
                geoBin[i] = latBin[i / 2];
            }
        }
        return geoBin;
    }

    private static int[] decrBin(int[] bin) {
        int[] newBin = bin.clone();
        for (int i = bin.length - 1; i >= 0; i--) {
            if (newBin[i] == 1) {
                newBin[i] = 0;
                break;
            } else {
                newBin[i] = 1;
            }
        }
        return newBin;
    }

    private static int[] incrBin(int[] bin) {
        int[] newBin = bin.clone();
        for (int i = bin.length - 1; i >= 0; i--) {
            if (newBin[i] == 0) {
                newBin[i] = 1;
                break;
            } else {
                newBin[i] = 0;
            }
        }
        return newBin;
    }

    public static class Point {
        double lat;
        double lng;
        private int[] latBin;
        private int[] lngBin;

        Point(double lng, double lat, int[] lngBin, int[] latBin) {
            this.lng = lng;
            this.lat = lat;
            this.lngBin = lngBin;
            this.latBin = latBin;
        }

        @Override
        public String toString() {
            return "lng:" + lng + ", lat:" + lat + ", lngBin:" + Arrays.toString(lngBin).replace(", ", "") + ", latBin:" + Arrays.toString(latBin).replace(", ", "");
        }
    }

    private static final class Base32 {

        private static final char[] base32Table = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        private static final Map<Character, Integer> base32Map = new HashMap<>();

        static {
            for (int i = 0; i < 32; i++) {
                base32Map.put(base32Table[i], i);
            }
        }

        public static int[] decode(String str) {
            int len = str.length();
            int[] bin = new int[len * 5];

            for (int i = 0, j = 0; i < len; i++, j += 5) {
                int idx = base32Map.get(str.charAt(i));

                String binStr = Integer.toBinaryString(idx);
                int binLen = binStr.length();
                for (int k = 0; k < 5; k++) {
                    bin[j + k] = k < (5 - binLen) ? 0 : binStr.charAt(k - (5 - binLen)) - '0';
                }
            }

            return bin;
        }

        public static String encode(int[] bin) {

            int len = bin.length;

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < len; i += 5) {

                int idx = (bin[i] << 4)
                        + ((i + 1 < len) ? bin[i + 1] << 3 : 0)
                        + ((i + 2 < len) ? bin[i + 2] << 2 : 0)
                        + ((i + 3 < len) ? bin[i + 3] << 1 : 0)
                        + ((i + 4 < len) ? bin[i + 4] : 0);
                sb.append(base32Table[idx]);
            }

            return sb.toString();
        }
    }

}
