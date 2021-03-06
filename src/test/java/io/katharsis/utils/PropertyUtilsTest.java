package io.katharsis.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onNullBeanGetShouldThrowException() throws Exception {
        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.getProperty(null, "privatePropertyWithMutators");
    }

    @Test
    public void onNullFieldGetShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.getProperty(bean, null);
    }

    @Test
    public void onBooleanPrimitiveWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();
        bean.setBooleanPrimitivePropertyWithMutators(true);

        // WHEN
        Object result = PropertyUtils
            .getProperty(bean, "booleanPrimitivePropertyWithMutators");

        // THEN
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void onBooleanWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();
        bean.setBooleanPropertyWithMutators(true);

        // WHEN
        Object result = PropertyUtils.getProperty(bean, "booleanPropertyWithMutators");

        // THEN
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void onStringPublicWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();
        bean.publicProperty = "value";

        // WHEN
        Object result = PropertyUtils.getProperty(bean, "publicProperty");

        // THEN
        assertThat(result).isEqualTo("value");
    }

    @Test
    public void onStringProtectedGetWithMutatorsShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        PropertyUtils.getProperty(bean, "protectedProperty");
    }

    @Test
    public void onInheritedStringPrivateWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new ChildBean();
        bean.setPrivatePropertyWithMutators("value");

        // WHEN
        Object result = PropertyUtils.getProperty(bean, "privatePropertyWithMutators");

        // THEN
        assertThat(result).isEqualTo("value");
    }

    @Test
    public void onJacksonPropertyWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();
        bean.setJacksonProperty("value");

        // WHEN
        Object result = PropertyUtils.getProperty(bean, "annotatedJacksonProperty");

        // THEN
        assertThat(result).isEqualTo("value");
    }

    @Test
    public void onMethodAccessorOnlyShouldReturnValue() throws Exception {
        // GIVEN
        GetterTest bean = new GetterTest();

        // WHEN
        Object result = PropertyUtils.getProperty(bean, "property");

        // THEN
        assertThat(result).isEqualTo("valueProperty");
    }

    @Test
    public void onJacksonPropertyWithAccessorOnlyShouldReturnValue() throws Exception {
        // GIVEN
        GetterTest bean = new GetterTest();

        // WHEN
        Object result = PropertyUtils.getProperty(bean, "jacksonProperty");

        // THEN
        assertThat(result).isEqualTo("valueJackson");
    }

    @Test
    public void onNullBeanSetShouldThrowException() throws Exception {
        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.setProperty(null, "privatePropertyWithMutators", null);
    }

    @Test
    public void onNullFieldSetShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.setProperty(bean, null, null);
    }

    @Test
    public void onBooleanPrimitiveWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // WHEN
        PropertyUtils.setProperty(bean, "booleanPrimitivePropertyWithMutators", true);

        // THEN
        assertThat(bean.isBooleanPrimitivePropertyWithMutators()).isEqualTo(true);
    }

    @Test
    public void onBooleanWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // WHEN
        PropertyUtils.setProperty(bean, "booleanPropertyWithMutators", true);

        // THEN
        assertThat(bean.getBooleanPropertyWithMutators()).isEqualTo(true);
    }

    @Test
    public void onStringPublicWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // WHEN
        PropertyUtils.setProperty(bean, "publicProperty", "value");

        // THEN
        assertThat(bean.publicProperty).isEqualTo("value");
    }

    @Test
    public void onStringProtectedSetWithMutatorsShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        PropertyUtils.setProperty(bean, "protectedProperty", null);
    }

    @Test
    public void onInheritedStringPrivateWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new ChildBean();

        // WHEN
        PropertyUtils.setProperty(bean, "privatePropertyWithMutators", "value");

        // THEN
        assertThat(bean.getPrivatePropertyWithMutators()).isEqualTo("value");
    }

    @Test
    public void onJacksonPropertyWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // WHEN
        PropertyUtils.setProperty(bean, "annotatedJacksonProperty", "value");

        // THEN
        assertThat(bean.getJacksonProperty()).isEqualTo("value");
    }

    @Test
    public void onDifferentFieldAndMutatorNamesShouldSetValue() throws Exception {
        // GIVEN
        SetterTest bean = new SetterTest();

        // WHEN
        PropertyUtils.setProperty(bean, "property", "value");

        // THEN
        assertThat(bean.getProperty()).isEqualTo("value");
    }

    @Test
    public void onNonExistingPropertyShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        PropertyUtils.getProperty(bean, "nonExistingProperty");
    }

    public static class Bean {
        private String privatePropertyWithMutators;
        private boolean booleanPrimitivePropertyWithMutators;
        private Boolean booleanPropertyWithMutators;
        public String publicProperty;
        @JsonProperty("annotatedJacksonProperty")
        private String jacksonProperty;

        public String getPrivatePropertyWithMutators() {
            return privatePropertyWithMutators;
        }

        public void setPrivatePropertyWithMutators(@SuppressWarnings("SameParameterValue") String privatePropertyWithMutators) {
            this.privatePropertyWithMutators = privatePropertyWithMutators;
        }

        public boolean isBooleanPrimitivePropertyWithMutators() {
            return booleanPrimitivePropertyWithMutators;
        }

        public void setBooleanPrimitivePropertyWithMutators(@SuppressWarnings("SameParameterValue") boolean booleanPrimitivePropertyWithMutators) {
            this.booleanPrimitivePropertyWithMutators = booleanPrimitivePropertyWithMutators;
        }

        public Boolean getBooleanPropertyWithMutators() {
            return booleanPropertyWithMutators;
        }

        public void setBooleanPropertyWithMutators(@SuppressWarnings("SameParameterValue") Boolean booleanPropertyWithMutators) {
            this.booleanPropertyWithMutators = booleanPropertyWithMutators;
        }

        public String getJacksonProperty() {
            return jacksonProperty;
        }

        public void setJacksonProperty(@SuppressWarnings("SameParameterValue") String jacksonProperty) {
            this.jacksonProperty = jacksonProperty;
        }
    }

    private static class ChildBean extends Bean {

    }

    private static class GetterTest {
        public String getProperty() {
            return "valueProperty";
        }

        @JsonProperty("jacksonProperty")
        public String getAnnotatedProperty() {
            return "valueJackson";
        }
    }

    public static class SetterTest {
        public String anotherProperty;

        public String getProperty() {
            return anotherProperty;
        }

        public void setProperty(String property) {
            anotherProperty = property;
        }
    }
}
