package org.springframework.data.tarantool.core.mapping;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MapIdFactoryTest {

    @Test
    void shouldGetObjectFromExtendingMapIdAndSerializableInterface() {
        Random r = new Random();
        String s = "" + r.nextInt();
        Integer i = r.nextInt();

        ExtendingMapIdAndSerializableInterface id = MapIdFactory.id(ExtendingMapIdAndSerializableInterface.class);
        assertThat(id.string()).isNull();
        assertThat(id.number()).isNull();
        assertThat(id.getString()).isNull();
        assertThat(id.getNumber()).isNull();

        id.setNumber(i);
        assertThat(id.getNumber()).isEqualTo(i);
        assertThat(id.number()).isEqualTo(i);
        assertThat(id.get("number")).isEqualTo(i);

        ExtendingMapIdAndSerializableInterface returned = id.number(i = r.nextInt());
        assertThat(id).isSameAs(returned);
        assertThat(id.getNumber()).isEqualTo(i);
        assertThat(id.number()).isEqualTo(i);
        assertThat(id.get("number")).isEqualTo(i);

        id.put("number", i = r.nextInt());
        assertThat(id.getNumber()).isEqualTo(i);
        assertThat(id.number()).isEqualTo(i);
        assertThat(id.get("number")).isEqualTo(i);

        id.setString(s);
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(id.get("string")).isEqualTo(s);

        returned = id.string(s = "" + r.nextInt());
        assertThat(id).isSameAs(returned);
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(id.get("string")).isEqualTo(s);

        returned = id.withString(s = "" + r.nextInt());
        assertThat(id).isSameAs(returned);
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(id.get("string")).isEqualTo(s);

        id.put("string", s = "" + r.nextInt());
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(id.get("string")).isEqualTo(s);

        id.setString(null);
        assertThat(id.getString()).isNull();
        assertThat(id.string()).isNull();
        assertThat(id.get("string")).isNull();

        id.setNumber(null);
        assertThat(id.getNumber()).isNull();
        assertThat(id.number()).isNull();
        assertThat(id.get("number")).isNull();
    }

    @Test
    void shouldGetObjectFromExtendingNothingInterface() {
        Random r = new Random();
        String s = "" + r.nextInt();
        Integer i = r.nextInt();

        ExtendingNothingInterface id = MapIdFactory.id(ExtendingNothingInterface.class);
        assertThat(id instanceof Serializable).isTrue();
        assertThat(id instanceof MapId).isTrue();
        MapId mapid = (MapId) id;

        assertThat(id.string()).isNull();
        assertThat(id.number()).isNull();
        assertThat(id.getString()).isNull();
        assertThat(id.getNumber()).isNull();

        id.setNumber(i);
        assertThat(id.getNumber()).isEqualTo(i);
        assertThat(id.number()).isEqualTo(i);
        assertThat(mapid.get("number")).isEqualTo(i);

        ExtendingNothingInterface returned = id.number(i = r.nextInt());
        assertThat(id).isSameAs(returned);
        assertThat(id.getNumber()).isEqualTo(i);
        assertThat(id.number()).isEqualTo(i);
        assertThat(mapid.get("number")).isEqualTo(i);

        mapid.put("number", i = r.nextInt());
        assertThat(id.getNumber()).isEqualTo(i);
        assertThat(id.number()).isEqualTo(i);
        assertThat(mapid.get("number")).isEqualTo(i);

        id.setString(s);
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(mapid.get("string")).isEqualTo(s);

        returned = id.string(s = "" + r.nextInt());
        assertThat(id).isSameAs(returned);
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(mapid.get("string")).isEqualTo(s);

        returned = id.withString(s = "" + r.nextInt());
        assertThat(id).isSameAs(returned);
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(mapid.get("string")).isEqualTo(s);

        mapid.put("string", s = "" + r.nextInt());
        assertThat(id.getString()).isEqualTo(s);
        assertThat(id.string()).isEqualTo(s);
        assertThat(mapid.get("string")).isEqualTo(s);

        id.setString(null);
        assertThat(id.getString()).isNull();
        assertThat(id.string()).isNull();
        assertThat(mapid.get("string")).isNull();

        id.setNumber(null);
        assertThat(id.getNumber()).isNull();
        assertThat(id.number()).isNull();
        assertThat(mapid.get("number")).isNull();
    }

    @Test
    void shouldValidateInterfaces() {
        Class<?>[] interfaces = new Class<?>[]{IdClass.class, LiteralGet.class, GetterReturningVoid.class,
                GetReturningVoid.class, MethodWithMoreThanOneArgument.class, LiteralSet.class, LiteralWith.class,
                SetterMethodNotReturningVoidOrThis.class, SetMethodNotReturningVoidOrThis.class,
                WithMethodNotReturningVoidOrThis.class};

        assertThatThrownBy(() -> {
            for (Class<?> idInterface : interfaces) {
                IdInterfaceValidator.validate(idInterface);
            }
        }).isInstanceOf(IdInterfaceExceptions.class)
                .extracting("exceptions", InstanceOfAssertFactories.ITERABLE)
                .hasSize(1);
    }

    private interface ExtendingNothingInterface {
        ExtendingNothingInterface string(String s);

        void setString(String s);

        ExtendingNothingInterface withString(String s);

        String string();

        String getString();

        ExtendingNothingInterface number(Integer i);

        void setNumber(Integer i);

        Integer number();

        Integer getNumber();
    }

    private interface ExtendingMapIdAndSerializableInterface extends MapId, Serializable {
        ExtendingMapIdAndSerializableInterface string(String s);

        void setString(String s);

        ExtendingMapIdAndSerializableInterface withString(String s);

        String string();

        String getString();

        ExtendingMapIdAndSerializableInterface number(Integer i);

        void setNumber(Integer i);

        Integer number();

        Integer getNumber();
    }

    private static class IdClass {
    }

    private interface LiteralGet {
        String get();
    }

    private interface GetterReturningVoid {
        void getString();
    }

    private interface GetReturningVoid {
        void string();
    }

    private interface MethodWithMoreThanOneArgument {
        void method(Object a, Object b);
    }

    private interface LiteralSet {
        void set(String s);
    }

    private interface LiteralWith {
        void with(String s);
    }

    private interface SetterMethodNotReturningVoidOrThis {
        String string(String s);
    }

    private interface SetMethodNotReturningVoidOrThis {
        String setString(String s);
    }

    private interface WithMethodNotReturningVoidOrThis {
        String withString(String s);
    }
}
