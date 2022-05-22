package com.bence.mate.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private Integer year;

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", year=" + year +
                '}';
    }
}