package sample;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.TABLESWITCH;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class QuicksortCFG {

        // Construct format for output DOT file
        private static final String[] fileHeader = new String[]{
                "control_flow_graph {",
                "",
                "	node [shape = rectangle]; entry exit;",
                "	node [shape = circle];",
                ""
        };
        private static final String[] fileFooter = new String[]{
                "}"
        };

        private static final String entryNode = "entry";
        private static final String exitNode = "exit";
        private static final String lineFormat = "	%1$s -> %2$s;%n";
        private static final String lineLabelFormat = "	%1$s -> %2$s [label = \"%3$s\"];%n";

        // Map line position with instruction.
        Map<Integer, InstructionHandle> instructionHandleMap = new HashMap<>();
        InstructionHandle[] instructionHandleArray;

        private Map<Method, Map<InstructionHandle, List<InstructionHandle>>> methodCFGMap = new HashMap<>();

        /**
         * Constructor
         * Loads an instruction list and creates a new CFG.
         *
         * @param instructions Instruction list from the method to create the CFG from.
         */
        public QuicksortCFG(InstructionList instructions) {

                instructionHandleArray = instructions.getInstructionHandles();
                for (int i = 0; i < instructionHandleArray.length; i++) {
                        int pos = instructionHandleArray[i].getPosition();
                        instructionHandleMap.put(pos, instructionHandleArray[i]);
                }
        }

        /**
         * Generates a DOT file of CFG
         * To be called in generateMethodCFG
         * @param _out OutputStream to write the dotty file to.
         */
        private Map<InstructionHandle, List<InstructionHandle>> generateDot(OutputStream _out) {

                Map<InstructionHandle, List<InstructionHandle>> handleListMap = new HashMap<>();
                PrintStream printStream = new PrintStream(_out);

                for (String s : fileHeader) {
                        printStream.print(s);
                        printStream.print("\n");
                }

                printStream.print("	" + "entry" + " -> " + instructionHandleArray[0].getPosition() + ";");
                printStream.print('\n');

                //Look up and map corresponding instruction type
                for (int i = 0; i < instructionHandleArray.length - 1; i++) {

                        Instruction instruction = instructionHandleArray[i].getInstruction();

                        int start, end;

                        start = instructionHandleArray[i].getPosition();
                        end = instructionHandleArray[i].getPosition();

                        //BRANCH
                        if (instruction instanceof BranchInstruction) {
                                //IF
                                if (instruction instanceof IfInstruction) {
                                        InstructionHandle target = ((IfInstruction) instruction).getTarget();
                                        printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + ((IfInstruction) instruction).getTarget().getPosition() + ";");
                                        printStream.print("\n");

                                        putData(start, end, handleListMap);

                                        target = instructionHandleArray[i + 1];
                                        end = target.getPosition();

                                        printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + instructionHandleArray[i + 1].getPosition() + ";");
                                        printStream.print("\n");
                                        //end = instructionHandleArray[i + 1].getPosition();
                                        putData(start, end, handleListMap);
                                }

                                //GOTO
                                else if (instruction instanceof GotoInstruction) {
                                        InstructionHandle target = ((GotoInstruction) instruction).getTarget();
                                        printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + ((GotoInstruction) instruction).getTarget().getPosition() + ";");
                                        printStream.print("\n");
                                        end = target.getPosition();
                                        putData(start, end, handleListMap);
                                }

                                //LOOK UP SWITCH
                                else if (instruction instanceof LOOKUPSWITCH) {
                                        InstructionHandle targets[] = ((LOOKUPSWITCH) instruction).getTargets();

                                        for (int j = 0; j < targets.length; j++) {

                                                printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + targets[j].getPosition() + ";");
                                                printStream.print("\n");
                                                end = targets[j].getPosition();
                                                putData(start, end, handleListMap);
                                        }
                                        InstructionHandle target = ((LOOKUPSWITCH) instruction).getTarget();
                                        printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + ((LOOKUPSWITCH) instruction).getTarget().getPosition() + ";");
                                        printStream.print("\n");
                                        end = ((LOOKUPSWITCH) instruction).getTarget().getPosition();
                                        putData(start, end, handleListMap);
                                }

                                //TABLE SWITCH
                                else if (instruction instanceof TABLESWITCH) {
                                        InstructionHandle hd[] = ((TABLESWITCH) instruction).getTargets();

                                        for (int j = 0; j < hd.length; j++) {

                                                printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + hd[j].getPosition() + ";");
                                                printStream.print("\n");
                                                end = hd[j].getPosition();
                                                putData(start, end, handleListMap);
                                        }
                                        InstructionHandle target = ((TABLESWITCH) instruction).getTarget();
                                        printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + ((TABLESWITCH) instruction).getTarget().getPosition() + ";");
                                        printStream.print("\n");
                                        end = ((TABLESWITCH) instruction).getTarget().getPosition();
                                        putData(start, end, handleListMap);

                                }

                        }
                        //RETURN
                        else {
                                if (instruction instanceof ReturnInstruction) {

                                        printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + "exit;");
                                        printStream.print("\n");
                                        putData(start, end, handleListMap);

                                } else {
                                        printStream.print("	" + instructionHandleArray[i].getPosition() + " -> " + instructionHandleArray[i + 1].getPosition() + ";");
                                        printStream.print("\n");
                                        end = instructionHandleArray[i + 1].getPosition();
                                        putData(start, end, handleListMap);

                                }

                        }

                }
                int length = instructionHandleArray.length;
                printStream.print("	" + instructionHandleArray[length - 1].getPosition() + " -> " + "exit" + ";");
                printStream.print('\n');

                putData(instructionHandleArray[length - 1].getPosition(), -1, handleListMap);

                for (String s : fileFooter) {
                        printStream.print(s);
                        printStream.print("\n");
                }

                printStream.close();

                return handleListMap;
        }

        public Map<Method, Map<InstructionHandle, List<InstructionHandle>>> generateMethodCFG(String className) {

                JavaClass cls = null;
                try {
                        cls = (new ClassParser(className)).parse();
                } catch (IOException e) {
                        System.exit(1);
                }

                // Search for main method.
                Method method = null;
                for (Method m : cls.getMethods()) {
                        method = m;
                        String methodName = m.getName();
                        System.out.println("Source code line number" + method.getCode().getLineNumberTable().getSourceLine(0)
                                + " with method " + methodName);

                        QuicksortCFG cfg = new QuicksortCFG(new InstructionList(method.getCode().getCode()));

                        try {
                                OutputStream output = new FileOutputStream("output.dotty");
                                Map<InstructionHandle, List<InstructionHandle>> tempmap = cfg.generateDot(output);
                                output.close();
                                methodCFGMap.put(method, tempmap);
                        } catch (IOException e) {
                                System.exit(1);
                        }
                }
                if (method == null) {
                        System.out.println( "No main method found in " + className + "." );
                        System.exit(1);
                }

                return methodCFGMap;
        }

        /**
         * Helper method to put data in handleListMap
         * @param start Starting instruction of specific instruction
         * @param end Ending instruction of specific instruction
         * @param handleListMap Map<InstructionHandle, List<InstructionHandle>>
         */
        private void putData(int start, int end, Map<InstructionHandle, List<InstructionHandle>> handleListMap) {
                if (handleListMap.containsKey(start)) {
                        List a = handleListMap.get(start);
                        a.add(end);
                } else {
                        List<InstructionHandle> temp = new ArrayList<>();
                        temp.add(instructionHandleArray[end]);
                        handleListMap.put(instructionHandleArray[start], temp);
                }
        }

        /**
         * Main method. Generate a DOT file with the CFG representing a given class file.
         *
         * @param args Expects two arguments: <input-class-file> <output-dotty-file>
         */
        public static void main(String[] args) {

                PrintStream error = System.err;
                PrintStream debug = new PrintStream(new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {

                        }
                });

                // Check arguments.
                if (args.length != 2) {
                        error.println("Wrong number of arguments.");
                        error.println("Usage: 2 arguments: <input-class-file> <output-dot-file>");
                        System.exit(1);
                }
                String inputClassFilename = args[0];
                String outputDottyFilename = args[1];

                // Parse class file.
                debug.println("Parsing " + inputClassFilename + ".");
                JavaClass jclass = null;
                try {
                        jclass = (new ClassParser(inputClassFilename)).parse();
                } catch (IOException e) {
                        e.printStackTrace(debug);
                        error.println("Error while parsing " + inputClassFilename + ".");
                        System.exit(1);
                }

                // Search for main method.
                debug.println("Searching for main method:");
                Method mainMethod = null;
                for (Method m : jclass.getMethods()) {
                        debug.println("   " + m.getName());
                        if ("main".equals(m.getName())) {
                                mainMethod = m;
                                break;
                        }
                }
                if (mainMethod == null) {
                        error.println("No main method found in " + inputClassFilename + ".");
                        System.exit(1);
                }

                //	InstructionList il=new InstructionList(mainMethod.getCode().getCode());
                //System.out.println(il.toString());
                // Create CFG.
                debug.println("Creating CFG object.");
                QuicksortCFG cfg = new QuicksortCFG(new InstructionList(mainMethod.getCode().getCode()));

                // Output Dotty file.
                debug.println("Generating Dotty file.");
                try {
                        OutputStream output = new FileOutputStream(outputDottyFilename);
                        cfg.generateMethodCFG(inputClassFilename);
                        output.close();
                } catch (IOException e) {
                        e.printStackTrace(debug);
                        error.println("Error while writing to " + outputDottyFilename + ".");
                        System.exit(1);
                }

                debug.println("Done.");
        }

}
