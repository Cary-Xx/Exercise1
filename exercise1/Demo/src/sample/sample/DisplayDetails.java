package sample;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.io.IOException;
import java.util.Arrays;

public class DisplayDetails {
        private static int sampleField;
        /**
         * @param args
         * @throws IOException
         * @throws ClassFormatException
         */
        public static void main(String[] args) throws ClassFormatException, IOException {

                /*An existing class can be parsed with ClassParser */
                ClassParser parser = new ClassParser(DisplayDetails.class.getResourceAsStream("/com/geekyarticles/bcel/DisplayDetails.class"), "DisplayDetails.class");
                JavaClass javaClass = parser.parse();

                System.out.println("*******Constant Pool*********");
                System.out.println(javaClass.getConstantPool());

                System.out.println("*******Fields*********");
                System.out.println(Arrays.toString(javaClass.getFields()));
                System.out.println();

                System.out.println("*******Methods*********");
                System.out.println(Arrays.toString(javaClass.getMethods()));

                for (Method method : javaClass.getMethods()) {
                        System.out.println(method);
                        System.out.println(method.getCode());
                }
        }

}