package classdemo1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyLoaderTest {
    MyLoader ml;
    List<String> input;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ml = new MyLoader();
    }

    @org.junit.jupiter.api.Test
    void myLoader() throws IOException {
        ml.myLoading("Test");
    }
}