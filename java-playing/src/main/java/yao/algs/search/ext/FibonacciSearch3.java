package yao.algs.search.ext;

/**
 * 斐波那契查找（黄金分割法查找）
 * ！！！斐波那契数组长度有限
 */
public class FibonacciSearch3 {

    public final static int MAXSIZE = 20;

    public static int[] createFibonacci() {
        int[] f = new int[20];
        f[0] = 1;
        f[1] = 1;
        for (int i = 2; i < MAXSIZE; i++) {
            f[i] = f[i - 1] + f[i - 2];
        }
        return f;
    }

    public static int search(int[] a, int key) {
        int low = 0;
        int high = a.length - 1;
        int mid = 0;

        // 创建斐波那契数列
        int[] f = createFibonacci();

        //斐波那契分割数值下标
        int k = 0;
        //计算斐波那契数列的下标k，使f[k]一定大于输入数组长度
        while (a.length > f[k] - 1) {
            k++;
        }

        //创建长度满足f[k]-1的辅助数组便于分割
        int[] temp = new int[f[k] - 1];
        for (int j = 0; j < a.length; j++) {
            temp[j] = a[j];
        }

        //辅助数组补充至f[k]-1个元素，补充的元素值为最后一个元素的值
        for (int i = a.length; i < f[k] - 1; i++) {
            temp[i] = temp[high];
        }

//        for (int j : temp) {
//            System.out.print(j + " ");
//        }
//        System.out.println();

        while (low <= high) {
            //low是起始位置，前半部分有f[k-1]个元素，由于下标从low（一般是0）开始，再减1就可获取黄金分割位置元素的下标
            //总共f[k]-1个元素 = f[k-1]-1 +  1*mid  + f[k-2]-1
            //mid是引用指针
            mid = low + f[k - 1] - 1;

            if (key < temp[mid]) {
                // 查找前半部分，高位指针移动，high移动到了low+f[k-1]-2
                high = mid - 1;
                // （全部元素） = （前半部分）+（后半部分）
                //   f[k]  =  f[k-1] + f[k-2]
                // 因为前半部分有f[k-1]个元素，所以 k = k-1
                k = k - 1;
            } else if (key > temp[mid]) {
                // 查找后半部分，高位指针移动，low移动到了low+f[k-1]
                low = mid + 1;
                // （全部元素） = （前半部分）+（后半部分）
                //   f[k]  =  f[k-1] + f[k-2]
                // 因为后半部分有f[k-1]个元素，所以 k = k-2
                k = k - 2;
            } else {

                //如果为真则找到相应的位置
                if (mid <= high) {
                    return mid;
                } else {
                    //出现这种情况是查找到补充的元素
                    //而补充的元素与high位置的元素一样
                    return high;
                }
            }
        }
        return -1;
    }

//
//    public static void main(String[] args) {
//        int[] f = createFibonacci();
//        for (int i : f) {
//            System.out.print(i + " ");
//        }
//        System.out.println();
//
//        int[] data = {1, 5, 15, 22, 25, 31, 39, 42, 47, 49, 59, 68, 88};
//
//        int search = 39;
//        int position = search(data, search);
//        System.out.println("值" + search + "的元素位置为：" + position);
//    }

}
