package dev.kensa.render;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenderersTest {

    private Renderers renderers;

    @BeforeEach
    void setUp() {
        renderers = new Renderers();
    }

    @Test
    void handlesNull() {
        assertThat(renderers.renderValueOnly(null)).isEqualTo("NULL");
    }

    @Test
    void defaultsToObjectRendererIfNoSpecificRendererExists() {
        assertThat(renderers.renderValueOnly(100)).isEqualTo("100");
    }

    @Test
    void canFindRendererForSpecificType() {
        renderers.add(Integer.class, value -> "<" + value + ">");
        renderers.add(Boolean.class, value -> "<<" + value + ">>");

        assertThat(renderers.renderValueOnly(100)).isEqualTo("<100>");
        assertThat(renderers.renderValueOnly(true)).isEqualTo("<<true>>");
    }

    @Test
    void canFindRendererSpecifiedByInterface() {
        renderers.add(MyInterface.class, value -> "<" + value.toString() + ">");

        assertThat(renderers.renderValueOnly(new MyClass("foo"))).isEqualTo("<foo>");
    }

    @Test
    void canFindRendererSpecifiedBySuperclass() {
        renderers.add(MySuperclass.class, value -> "<" + value.toString() + ">");

        assertThat(renderers.renderValueOnly(new MyClass("boo"))).isEqualTo("<boo>");
    }

    @Test
    void usesMostSpecificRendererWhenMultipleRenderersForHierarchySpecified() {
        renderers.add(MySuperclass.class, value -> "<" + value.toString() + ">");
        renderers.add(MyClass.class, value -> "<<<" + value.toString() + ">>>");
        renderers.add(MyInterface.class, value -> "<<" + value.toString() + ">>");

        assertThat(renderers.renderValueOnly(new MyClass("boo"))).isEqualTo("<<<boo>>>");

    }

    interface MyInterface {
    }

    static class MySuperclass {
    }

    static class MyClass extends MySuperclass implements MyInterface {
        private final String value;

        MyClass(String value) {this.value = value;}

        @Override
        public String toString() {
            return value;
        }
    }
}