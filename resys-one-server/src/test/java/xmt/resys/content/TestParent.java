package xmt.resys.content;

import java.lang.reflect.Field;

public class TestParent {
    public static void main(String[] args) throws InterruptedException {
        new B();
    }

}

class A {
    public String s = "123";

    public A() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName());
        }
    }
}

class B extends A {
    public String b = "123";

    public B() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName());
        }
    }
}