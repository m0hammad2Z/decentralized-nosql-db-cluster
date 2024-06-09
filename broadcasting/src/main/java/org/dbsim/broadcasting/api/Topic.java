package org.dbsim.broadcasting.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Topic {
    private final String name;

    public Topic( String name) {
        this.name = name;
    }

    public Topic() {
        this.name = "";
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(name, topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}