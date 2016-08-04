import java.io.PrintWriter;
import java.util.ArrayList;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;


public class Test1 {

    @Test
    public void test1() throws Exception {
        new ArrayList<String>().stream().forEach(this::lambdaHere);

        ClassReader asmReader = new ClassReader("Test1");
        asmReader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

    private void lambdaHere(String o) {

    }
}
