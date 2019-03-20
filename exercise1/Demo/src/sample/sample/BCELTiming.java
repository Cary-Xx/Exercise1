package sample;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import java.io.FileOutputStream;
import java.io.IOException;

public class BCELTiming {

        private static void addWrapper(ClassGen cgen, Method method) {

                // set up the construction tools
                InstructionFactory ifact = new InstructionFactory(cgen);
                InstructionList iList = new InstructionList();
                ConstantPoolGen pgen = cgen.getConstantPool();
                String cname = cgen.getClassName();
                MethodGen wrapgen = new MethodGen(method, cname, pgen);
                wrapgen.setInstructionList(iList);

                // rename a copy of the original method
                MethodGen methGen = new MethodGen(method, cname, pgen);
                cgen.removeMethod(method);
                String iname = methGen.getName() + "$impl";
                methGen.setName(iname);
                cgen.addMethod(methGen.getMethod());

                // compute the size of the calling parameters
                Type[] types = methGen.getArgumentTypes();
                int slot = methGen.isStatic() ? 0 : 1;
                for (int i = 0; i < types.length; i++) {
                        slot += types[i].getSize();
                }

                // save time prior to invocation
                iList.append(ifact.createInvoke("java.lang.System", "currentTimeMillis",
                        Type.LONG, Type.NO_ARGS, Constants.INVOKESTATIC));
                iList.append(InstructionFactory.createStore(Type.LONG, slot));

                // call the wrapped method
                int offset = 0;
                short invoke = Constants.INVOKESTATIC;
                if (!methGen.isStatic()) {
                        iList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
                        offset = 1;
                        invoke = Constants.INVOKEVIRTUAL;
                }
                for (int i = 0; i < types.length; i++) {
                        Type type = types[i];
                        iList.append(InstructionFactory.createLoad(type, offset));
                        offset += type.getSize();
                }
                Type result = methGen.getReturnType();
                iList.append(ifact.createInvoke(cname, iname, result, types, invoke));

                // store result for return later
                if (result != Type.VOID) {
                        iList.append(InstructionFactory.createStore(result, slot + 2));
                }

                // print time required for method call
                iList.append(ifact.createFieldAccess("java.lang.System", "out",
                        new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
                iList.append(InstructionConstants.DUP);
                iList.append(InstructionConstants.DUP);
                String text = "Call to method " + methGen.getName() + " took ";
                iList.append(new PUSH(pgen, text));
                iList.append(ifact.createInvoke("java.io.PrintStream", "print",
                        Type.VOID, new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));
                iList.append(ifact.createInvoke("java.lang.System",
                        "currentTimeMillis", Type.LONG, Type.NO_ARGS,
                        Constants.INVOKESTATIC));
                iList.append(InstructionFactory.createLoad(Type.LONG, slot));
                iList.append(InstructionConstants.LSUB);
                iList.append(ifact.createInvoke("java.io.PrintStream", "println",
                        Type.VOID, new Type[]{Type.LONG}, Constants.INVOKEVIRTUAL));
                iList.append(new PUSH(pgen, " ms. "));
                iList.append(ifact.createInvoke("java.io.PrintStream", "println",
                        Type.VOID, new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));

                // return the result from wrapped method call
                if (result != Type.VOID) {
                        iList.append(InstructionFactory.createLoad(result, slot + 2));
                }
                iList.append(InstructionFactory.createReturn(result));

                // finalize the constructed method
                wrapgen.stripAttributes(true);
                wrapgen.setMaxStack();
                wrapgen.setMaxLocals();
                cgen.addMethod(wrapgen.getMethod());
                iList.dispose();
        }

        public static void main(String[] argv) {
                if (argv.length == 2 && argv[0].endsWith(" .class")) {
                        try {
                                JavaClass jclas = new ClassParser(argv[0]).parse();
                                ClassGen cgen = new ClassGen(jclas);
                                Method[] methods = jclas.getMethods();
                                int index;
                                for (index = 0; index < methods.length; index++) {
                                        if (methods[index].getName().equals(argv[1])) {
                                                break;
                                        }
                                }
                                if (index < methods.length) {
                                        addWrapper(cgen, methods[index]);
                                        FileOutputStream fos = new FileOutputStream(argv[0]);
                                        cgen.getJavaClass().dump(fos);
                                        fos.close();
                                } else {
                                        System.err.println(" Method " + argv[1] +
                                                " not found in " + argv[0]);
                                }
                        } catch (IOException ex) {
                                ex.printStackTrace(System.err);
                        }
                } else {
                        System.out.println("Usage: BCELTiming Class-file method-name");
                }
        }

}
