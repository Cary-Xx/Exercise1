package sample;

public class StringBuilder
{
        private String buildString$impl(int length) {
                String result = "";
                for (int i = 0; i < length; i++) {
                        result += (char)(i%26 + 'a');
                }
                return result;
        }

        private String buildString(int length) {
                long start = System.currentTimeMillis();
                String result = buildString$impl(length);
                System.out.println("Call to buildString$impl took " +
                        (System.currentTimeMillis()-start) + " ms.");
                return result;
        }

        public static void main(String[] argv) {
                StringBuilder inst = new StringBuilder();
                for (int i = 0; i < argv.length; i++) {
                        String result = inst.buildString(Integer.parseInt(argv[i]));
                        System.out.println("Constructed string of length " +
                                result.length());
                }
        }
}