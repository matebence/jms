package com.bence.mate.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Health implements Serializable {

    @JsonIgnore
    public static final String DOWN = "DOWN";

    @JsonIgnore
    public static final String UP = "UP";

    @Getter
    @Setter
    private String status;

    @Override
    public String toString() {
        return "Health{" +
                "status='" + status + '\'' +
                '}';
    }
}