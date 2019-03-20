package sample;

import java.io.IOException;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IF_ICMPGE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;

public class SimpleIf {

        public static void main(String[] args) {
                //Create a ClassGen for our brand new class.
                ClassGen classGen=new ClassGen("com.geekyarticles.bcel.SyntheticClass", "java.lang.Object", "SyntheticClass.java", Constants.ACC_PUBLIC, null);

                //Get a reference to the constant pool of the class. This will be modified as we add methods, fields etc. Note that it already constains
                //a few constants.
                ConstantPoolGen constantPoolGen=classGen.getConstantPool();

                //The list of instructions for a method.
                InstructionList instructionList=new InstructionList();

                //Add the appropriate instructions.

                //Get the reference to static field out in class java.lang.System.
                instructionList.append(new GETSTATIC(constantPoolGen.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")));

                instructionList.append(new ALOAD(0));//the argument of the main function
                instructionList.append(new BIPUSH((byte)0));//Push 0.
                instructionList.append(new AALOAD());//Got value of the variable as a String;
                instructionList.append(new INVOKESTATIC(constantPoolGen.addMethodref("java.lang.Integer", "parseInt", "(Ljava/lang/String;)I")));//Now we got the value as int

                instructionList.append(new BIPUSH((byte)5));

                BranchHandle ifHandle=instructionList.append(new IF_ICMPGE(null)); // We do not yet know the position of the target. Will set it later.
                //Push the String to print
                instructionList.append(new LDC(constantPoolGen.addString("Less than five")));
                //Invoke println. we already have the object ref in the stack
                instructionList.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));

                //Not to fall through the else part also. jump to the end
                BranchHandle gotoHandle=instructionList.append(new GOTO(null));// We do not yet know the position of the target. Will set it later.

                //Push the String to print. This would be the target of the if_icmpge
                InstructionHandle matchHandle=instructionList.append(new LDC(constantPoolGen.addString("Greater than or equal to five")));
                //Invoke println. we already have the object ref in the stack
                instructionList.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));

                //Return from the method. This would be the target of the goto.
                InstructionHandle returnHandle=instructionList.append(new RETURN());

                ifHandle.setTarget(matchHandle);
                gotoHandle.setTarget(returnHandle);



                MethodGen methodGen=new MethodGen(Constants.ACC_PUBLIC|Constants.ACC_STATIC, Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)}, new String[]{"args"}, "main", "com.geekyarticles.bcel.SyntheticClass", instructionList, constantPoolGen);

                methodGen.setMaxLocals();//Calculate the maximum number of local variables.
                methodGen.setMaxStack();//Very important: must calculate the maximum size of the stack.

                classGen.addMethod(methodGen.getMethod()); //Add the method to the class

                //Print a few things.
                System.out.println("********Constant Pool**********");
                System.out.println(constantPoolGen.getFinalConstantPool());
                System.out.println("********Method**********");
                System.out.println(methodGen);
                System.out.println("********Instruction List**********");
                System.out.println(instructionList);


                //Now generate the class
                JavaClass javaClass=classGen.getJavaClass();
                try {
                        //Write the class byte code into a file
                        javaClass.dump("com/geekyarticles/bcel/SyntheticClass.class");

                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

                //That's it.

        }

}