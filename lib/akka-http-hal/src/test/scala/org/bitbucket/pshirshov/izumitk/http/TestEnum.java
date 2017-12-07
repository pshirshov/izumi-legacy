package org.bitbucket.pshirshov.izumitk.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TestEnum {
    @JsonProperty("abc--")
    abc__,
    @JsonProperty("c-b-a")
    c_b_a,
    @JsonProperty("xyz")
    xyz;

    @Override
    public String toString() {
        return super.toString().replace("_", "-");
    }

}
