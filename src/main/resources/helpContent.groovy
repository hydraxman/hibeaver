////////////////////// - HiBeaver Help Content(UTF-8) - /////////////////////////////
//          A HiBeaver plugin setup case is shown as below
//          You can copy-paste them entirely and de-comment them as an initial setup
//          You can turn Help Content output off by setting the showHelp flag false
//          hi, 这里是HiBeaver帮助内容，你可以直接把这整个内容复制到build.gradle中，然后解除注释作为初始设置
//          如果嫌烦可以在下面配置showHelp = false 来关闭这个帮助内容的输出
//
//import com.bryansharp.gradle.hibeaver.utils.MethodLogAdapter
//import org.objectweb.asm.ClassVisitor
//import org.objectweb.asm.MethodVisitor
//import org.objectweb.asm.Opcodes
////or you can import like bellow:
////import org.objectweb.asm.*
//hiBeaver {
//    //this will determine the name of this hibeaver transform, no practical use.
//    hiBeaverModifyName = 'myHibeaverTest'
//    //turn this on to make it print help content, default value is true
//    showHelp = true
//    //this flag will decide whether the log of the modifying process be printed or not, default value is false
//    keepQuiet = false
//    //this is a kit feature of the plugin, set it true to see the time consume of this build
//    watchTimeConsume = false
//
//    //this is the most important part
//    //structure is like ['class':[[:],[:]],'class':[[:],[:]]], type is Map<String, List<Map<String, Object>>>
//    modifyMatchMaps = [
//            'classname of which to be modified': [
//                    // you can use javap -s command to get the description of one method
//                    // the adapter is a closure
//                    ['methodName': 'the name of the method', 'methodDesc': 'javap -s to get the description', 'adapter': {
//                        //the below args cannot be changed, to copy them entirely with nothing changed is recommended
//                        ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
//                            //return null to modify nothing
//                            return null;
//                    }]
//                    ,
//                    ['methodName': 'the name of the method2', 'methodDesc': 'javap -s to get the description', 'adapter': {
//                        ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
//                            return null;
//                    }]
//            ]
//            ,
//            //the next part is an real case
//            'okhttp3.internal.http.HttpEngine': [
//                    ['methodName': '<init>', 'methodDesc': null, 'adapter': {
//                        ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
//                            MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
//                            MethodVisitor adapter = new MethodLogAdapter(methodVisitor) {
//                                @Override
//                                void visitCode() {
//                                    super.visitCode();
//                                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
//                                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
//                                    //there is an convenient util MethodLogAdapter.className2Path("bruce.com.testhibeaver.MainActivity")
//                                    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "bruce/com/testhibeaver/MainActivity", "hookXM", "(Ljava/lang/Object;Ljava/lang/Object;)V");
//                                }
//                            }
//                            return adapter;
//                    }]
//            ]
//    ]
//}
//
// author BryanSharp bsp0911932@163.com
///////////////////// - HiBeaver Help Content END - /////////////////////////////

