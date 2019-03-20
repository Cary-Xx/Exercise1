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
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;

public class SimpleWhile {

        /**
         * @param args
         */
        public static void main(String[] args) {
                //Create a ClassGen for our brand new class.
                ClassGen classGen=new ClassGen("com.geekyarticles.bcel.SyntheticClass", "java.lang.Object", "SyntheticClass.java", Constants.ACC_PUBLIC, null);

                //Get a reference to the constant pool of the class. This will be modified as we add methods, fields etc. Note that it already constains
                //a few constants.
                ConstantPoolGen constantPoolGen=classGen.getConstantPool();

                //The list of instructions for a method.
                InstructionList instructionList=new InstructionList();

                //Add the appropriate instructions.



                instructionList.append(new ALOAD(0));//the argument of the main function
                instructionList.append(new BIPUSH((byte)0));//Push 0.
                instructionList.append(new DUP());//A simple trick. Just duplicates the top of the stack.
                instructionList.append(new ISTORE(1));//local variable 0 is the argument of the main method. So we create a new local variable 1. It will work as loop counter
                instructionList.append(new AALOAD());//Got value of the variable as a String;
                instructionList.append(new INVOKESTATIC(constantPoolGen.addMethodref("java.lang.Integer", "parseInt", "(Ljava/lang/String;)I")));//Now we got the value as int
                instructionList.append(new ISTORE(2));// The limit to the loop counter.

                InstructionHandle loopStart=instructionList.append(new ILOAD(1));
                instructionList.append(new ILOAD(2));
                BranchHandle ifCheck=instructionList.append(new IF_ICMPLT(null));//If less go inside the loop;
                //This is when the loop ends
                BranchHandle gotoJump=instructionList.append(new GOTO(null));//Goto the return

                //Print the loop counter
                //Get the reference to static field out in class java.lang.System.
                InstructionHandle insideLoop=instructionList.append(new GETSTATIC(constantPoolGen.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")));
                instructionList.append(new ILOAD(1));
                instructionList.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(I)V")));
//
//        //Increment the counter, the variable number is 1.
                instructionList.append(new IINC(1, 1));
//
                instructionList.append(new GOTO(loopStart));//In this case, we already know where to go.

                InstructionHandle returnHandle=instructionList.append(new RETURN());

                //Set the handles
                ifCheck.setTarget(insideLoop);
                gotoJump.setTarget(returnHandle);

                //The usual method generation
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