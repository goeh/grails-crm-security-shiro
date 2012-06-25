package grails.plugins.crm.security.shiro

import java.text.SimpleDateFormat
import groovy.json.JsonSlurper

/**
 * Tenant specific parameter.
 */
class ShiroCrmTenantOption<T> {
    String key
    String v
    static belongsTo = [tenant:ShiroCrmTenant]
    static constraints = {
        key(maxSize: 80, unique: 'tenant')
        v(maxSize: 2000, blank: true)
    }

    static mapping = {
        sort "key"
        key column: 'k' // key is reserved word in MySQL
        cache usage: "nonstrict-read-write"
    }

    static transients = ['value']

    ShiroCrmTenantOption() {}

    ShiroCrmTenantOption(String key, Object value) {
        this.key = key
        this.setValue(value)
    }

    ShiroCrmTenantOption(Long tenant, String key, Object value) {
        this.tenant = ShiroCrmTenant.load(tenant)
        this.key = key
        this.setValue(value)
    }

    String toString() {
        "$key=${this as String}"
    }

    /**
     * Returns the persisted value for this setting.
     *
     * Values are persisted as JSON strings.
     * This method returns the result of parsing the persisted value with JsonSlurper.
     * That means (almost always) the same value as was set with setValue()
     * Note: Dates are returned as String (yyyy-MM-dd'T'HH:mm:ssZ)
     * Use <code>userSetting as Date</code> to get a Date instance
     * @return the value of this setting
     */
    Object getValue() {
        v ? new JsonSlurper().parseText(v).v : null
    }

    /**
     * Set the value of this setting.
     * Values are serialized with <code>groovy.json.JsonOutput</code> and thus persisted as JSON strings.
     * @param arg the value to set
     */
    void setValue(Object arg) {
        v = groovy.json.JsonOutput.toJson([v:arg])
    }

    /**
     * Returns the Groovy Truth for the value.
     * @return true if value is true
     */
    boolean asBoolean() {
        this.getValue().asBoolean()
    }

    /**
     * Cast the value of this setting to a specific type.
     * @param clazz the type to cast to
     * @return the value
     */
    T asType(Class<T> clazz) {
        def value = this.getValue()
        if(value == null) {
            return value
        }
        if (clazz == Date) {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(value)
        }
        return value.asType(clazz)
    }
}
